package com.cyanharborstudios.callblock.screening

import com.cyanharborstudios.callblock.core.numbers.PhoneNumber
import com.cyanharborstudios.callblock.core.numbers.PhoneNumbers
import com.cyanharborstudios.callblock.core.rules.Decision
import com.cyanharborstudios.callblock.core.rules.IncomingCall
import com.cyanharborstudios.callblock.core.rules.RuleBook
import com.cyanharborstudios.callblock.core.rules.RuleEngine
import com.cyanharborstudios.callblock.core.rules.ScreeningSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import java.time.ZoneId

/** What the screener needs to read before it can decide. Implemented over the app's stores. */
interface ScreeningFacts {
    suspend fun settings(): ScreeningSettings

    /** The allow list: number key to expiry time (null for an entry that never ends). */
    suspend fun allowList(): Map<String, Long?>

    /** When the app last blocked or silenced this number, or null. */
    suspend fun lastHandledAt(numberKey: String): Long?

    /** When the user last called this number themselves, or null. */
    suspend fun lastDialledAt(numberKey: String): Long?
}

/** A call's number as the app understood it, and what was decided. */
data class ScreenedCall(val number: PhoneNumber, val decision: Decision, val receivedAtMillis: Long)

/**
 * Gathers the facts about a call and asks the rule engine for a decision.
 * No Android classes here, so this is unit-tested with a fake [ScreeningFacts].
 */
class CallScreener(
    private val facts: ScreeningFacts,
    /** The phone's own country, e.g. "IN". Read per call: a SIM can change. */
    private val homeRegion: () -> String,
    /** The phone's time zone, for the schedule's hours. Read per call: the user can travel. */
    private val zone: () -> ZoneId,
) {
    /**
     * [screen], but never late and never failing: returns null if the decision is not
     * ready within [timeoutMillis] or anything goes wrong. The caller treats null as
     * "let the call ring".
     */
    suspend fun screenOrGiveUp(rawNumber: String?, receivedAtMillis: Long, timeoutMillis: Long): ScreenedCall? =
        try {
            withTimeoutOrNull(timeoutMillis) { screen(rawNumber, receivedAtMillis) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    suspend fun screen(rawNumber: String?, receivedAtMillis: Long): ScreenedCall {
        val number = PhoneNumbers(homeRegion()).parse(rawNumber)
        val rules = RuleBook.build(facts.settings(), facts.allowList(), receivedAtMillis, zone())
        val call = IncomingCall(
            number = number,
            receivedAtMillis = receivedAtMillis,
            // Android only hands a screening app calls from numbers outside the user's
            // contacts (the app holds no contacts permission), so every call seen here
            // is from a non-contact. See knowledge-base/adr/ADR-002.
            callerIsContact = false,
            lastHandledAtMillis = if (number.key.isEmpty()) null else facts.lastHandledAt(number.key),
            lastDialledAtMillis = if (number.key.isEmpty()) null else facts.lastDialledAt(number.key),
        )
        return ScreenedCall(number, RuleEngine.decide(rules, call), receivedAtMillis)
    }
}
