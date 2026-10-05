package com.cyanharborstudios.callblock.core.rules

import com.cyanharborstudios.callblock.core.numbers.PhoneNumbers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

/** What a call to an emergency number does to the settings, and to the calls that follow. */
class EmergencyPauseTest {

    private val zone = ZoneId.of("Asia/Kolkata")

    /** Monday 5 October 2026, 12:00 in India. */
    private val noon = ZonedDateTime.of(2026, 10, 5, 12, 0, 0, 0, zone).toInstant().toEpochMilli()
    private val minute = 60_000L
    private val hour = 60 * minute
    private val day = 24 * hour

    private fun decisionFor(settings: ScreeningSettings, at: Long): Decision {
        val stranger = IncomingCall(PhoneNumbers("IN").parse("+918012345678"), at)
        return RuleEngine.decide(RuleBook.build(settings, emptyMap(), at, zone), stranger)
    }

    @Test
    fun `with the lever at block or silence, every call rings for a day and then the lever is back`() {
        for (mode in listOf(Mode.BLOCK, Mode.SILENCE)) {
            val before = ScreeningSettings(mode = mode)
            val after = EmergencyPause.after(before, noon)
            assertEquals(before.copy(timerMode = Mode.OFF, timerUntilMillis = noon + day), after)
            assertEquals(Decision(Action.ALLOW, RuleBook.PAUSED), decisionFor(after, noon + minute))
            assertEquals(Decision(Action.ALLOW, RuleBook.PAUSED), decisionFor(after, noon + day - 1))
            assertEquals(RuleBook.UNKNOWN_CALLER, decisionFor(after, noon + day).ruleId)
        }
    }

    @Test
    fun `settings that stop no call are left as they are`() {
        val off = ScreeningSettings(mode = Mode.OFF)
        assertSame(off, EmergencyPause.after(off, noon))
        // A schedule that is stored but switched off stops nothing either.
        val scheduleOff = ScreeningSettings(schedule = WeekSchedule.EMPTY.with(DayOfWeek.values().toList(), 0..23, Mode.BLOCK))
        assertSame(scheduleOff, EmergencyPause.after(scheduleOff, noon))
    }

    @Test
    fun `a schedule that blocks is paused too, whatever the lever says`() {
        val everyHourBlocked = WeekSchedule.EMPTY.with(DayOfWeek.values().toList(), 0..23, Mode.BLOCK)
        val before = ScreeningSettings(mode = Mode.OFF, schedule = everyHourBlocked, scheduleOn = true)
        assertEquals(RuleBook.UNKNOWN_CALLER_ON_SCHEDULE, decisionFor(before, noon + minute).ruleId)
        val after = EmergencyPause.after(before, noon)
        assertEquals(Decision(Action.ALLOW, RuleBook.PAUSED), decisionFor(after, noon + minute))
        assertEquals(RuleBook.UNKNOWN_CALLER_ON_SCHEDULE, decisionFor(after, noon + day).ruleId)
    }

    @Test
    fun `a running timer at block gives way to the pause`() {
        val before = ScreeningSettings(mode = Mode.OFF, timerMode = Mode.BLOCK, timerUntilMillis = noon + 2 * hour)
        val after = EmergencyPause.after(before, noon)
        assertEquals(Mode.OFF, after.timerMode)
        assertEquals(noon + day, after.timerUntilMillis)
        assertEquals(Decision(Action.ALLOW, RuleBook.PAUSED), decisionFor(after, noon + hour))
    }

    @Test
    fun `a pause that was already running is made to last the full day`() {
        val before = ScreeningSettings(mode = Mode.BLOCK, timerMode = Mode.OFF, timerUntilMillis = noon + 15 * minute)
        assertEquals(noon + day, EmergencyPause.after(before, noon).timerUntilMillis)
    }
}
