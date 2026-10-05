package com.cyanharborstudios.callblock.screening

import com.cyanharborstudios.callblock.core.rules.Action
import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.RuleBook
import com.cyanharborstudios.callblock.core.rules.Scope
import com.cyanharborstudios.callblock.core.rules.ScreeningSettings
import com.cyanharborstudios.callblock.core.rules.WeekSchedule
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId

/** The step between Android's call and the rule engine, with the stores replaced by fakes. */
class CallScreenerTest {

    private class FakeFacts(
        var settings: ScreeningSettings = ScreeningSettings(mode = Mode.BLOCK),
        var allowList: Map<String, Long?> = emptyMap(),
        var lastHandledAt: Map<String, Long> = emptyMap(),
        var lastDialledAt: Map<String, Long> = emptyMap(),
        var delayMillis: Long = 0,
        var failure: Exception? = null,
    ) : ScreeningFacts {
        val lookedUp = mutableListOf<String>()
        val dialledLookedUp = mutableListOf<String>()

        override suspend fun settings(): ScreeningSettings {
            delay(delayMillis)
            failure?.let { throw it }
            return settings
        }

        override suspend fun allowList(): Map<String, Long?> = allowList

        override suspend fun lastHandledAt(numberKey: String): Long? {
            lookedUp += numberKey
            return lastHandledAt[numberKey]
        }

        override suspend fun lastDialledAt(numberKey: String): Long? {
            dialledLookedUp += numberKey
            return lastDialledAt[numberKey]
        }
    }

    private val now = 1_000_000_000L

    private fun screener(facts: FakeFacts, homeRegion: String = "IN") =
        CallScreener(facts, homeRegion = { homeRegion }, zone = { ZoneId.of("Asia/Kolkata") })

    @Test
    fun `an unknown caller is blocked in block mode and the number is understood`() = runTest {
        val screened = screener(FakeFacts()).screen("+918045678901", now)
        assertEquals(Action.BLOCK, screened.decision.action)
        assertEquals(RuleBook.UNKNOWN_CALLER, screened.decision.ruleId)
        assertEquals("+918045678901", screened.number.key)
        assertEquals(now, screened.receivedAtMillis)
    }

    @Test
    fun `the schedule's hours are read in the phone's time zone`() = runTest {
        // The test's moment is a Monday evening in India (19:16) and a Monday afternoon in London (13:46).
        val mondaySevenPm = WeekSchedule.EMPTY.with(listOf(DayOfWeek.MONDAY), 19..19, Mode.BLOCK)
        val facts = FakeFacts(settings = ScreeningSettings(mode = Mode.OFF, schedule = mondaySevenPm, scheduleOn = true))
        val inIndia = CallScreener(facts, homeRegion = { "IN" }, zone = { ZoneId.of("Asia/Kolkata") })
        assertEquals(RuleBook.UNKNOWN_CALLER_ON_SCHEDULE, inIndia.screen("+918045678901", now).decision.ruleId)
        val inLondon = CallScreener(facts, homeRegion = { "IN" }, zone = { ZoneId.of("Europe/London") })
        assertEquals(Action.ALLOW, inLondon.screen("+918045678901", now).decision.action)
    }

    @Test
    fun `the allow list is read from the store`() = runTest {
        val facts = FakeFacts(
            settings = ScreeningSettings(mode = Mode.BLOCK, allowListEnabled = true),
            allowList = mapOf("+918045678901" to null),
        )
        assertEquals(Action.ALLOW, screener(facts).screen("080 4567 8901", now).decision.action)
    }

    @Test
    fun `a repeat call is recognised from the app's own log`() = runTest {
        val facts = FakeFacts(
            settings = ScreeningSettings(mode = Mode.BLOCK, repeatCallsRing = true, repeatWindowMinutes = 15),
            lastHandledAt = mapOf("+918045678901" to now - 60_000),
        )
        val screened = screener(facts).screen("+918045678901", now)
        assertEquals(RuleBook.REPEAT_CALL, screened.decision.ruleId)
        assertEquals(listOf("+918045678901"), facts.lookedUp)
    }

    @Test
    fun `a call back is recognised from the numbers the user dialled, however the number was written`() = runTest {
        // Dialled as a local number; the network reports the caller with the country code. One key for both.
        val facts = FakeFacts(lastDialledAt = mapOf("+918045678901" to now - 60_000))
        val screened = screener(facts).screen("+918045678901", now)
        assertEquals(Action.ALLOW, screened.decision.action)
        assertEquals(RuleBook.YOU_CALLED, screened.decision.ruleId)
        assertEquals(listOf("+918045678901"), facts.dialledLookedUp)
        // Another number is still stopped.
        assertEquals(Action.BLOCK, screener(facts).screen("+918045678902", now).decision.action)
    }

    @Test
    fun `a call with no number is screened without a log lookup`() = runTest {
        val facts = FakeFacts(settings = ScreeningSettings(mode = Mode.SILENCE, repeatCallsRing = true))
        val screened = screener(facts).screen(null, now)
        assertEquals(Action.SILENCE, screened.decision.action)
        assertEquals(emptyList<String>(), facts.lookedUp)
        assertEquals(emptyList<String>(), facts.dialledLookedUp)
    }

    @Test
    fun `whether a number is international depends on the phone's own country`() = runTest {
        val facts = FakeFacts(settings = ScreeningSettings(mode = Mode.BLOCK, scope = Scope.INTERNATIONAL_ONLY))
        assertEquals(Action.ALLOW, screener(facts, homeRegion = "IN").screen("+918045678901", now).decision.action)
        assertEquals(Action.BLOCK, screener(facts, homeRegion = "US").screen("+918045678901", now).decision.action)
    }

    @Test
    fun `off allows the call`() = runTest {
        val screened = screener(FakeFacts(settings = ScreeningSettings(mode = Mode.OFF))).screen("+918045678901", now)
        assertEquals(Action.ALLOW, screened.decision.action)
    }

    // --- the service treats "no answer in time" and "something broke" as: let it ring ---

    @Test
    fun `a decision that arrives in time is returned`() = runTest {
        val screened = screener(FakeFacts(delayMillis = 100)).screenOrGiveUp("+918045678901", now, timeoutMillis = 4_000)
        assertEquals(Action.BLOCK, screened?.decision?.action)
    }

    @Test
    fun `a decision that would arrive too late is given up on`() = runTest {
        val screened = screener(FakeFacts(delayMillis = 10_000)).screenOrGiveUp("+918045678901", now, timeoutMillis = 4_000)
        assertNull(screened)
    }

    @Test
    fun `a failure while gathering facts is given up on, not thrown`() = runTest {
        val facts = FakeFacts(failure = IllegalStateException("database is unreadable"))
        assertNull(screener(facts).screenOrGiveUp("+918045678901", now, timeoutMillis = 4_000))
    }
}
