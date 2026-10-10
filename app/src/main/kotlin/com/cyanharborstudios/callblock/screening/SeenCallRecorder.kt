package com.cyanharborstudios.callblock.screening

import com.cyanharborstudios.callblock.core.frequent.FrequentLimits
import com.cyanharborstudios.callblock.core.rules.RuleBook
import com.cyanharborstudios.callblock.data.SeenCallDao
import com.cyanharborstudios.callblock.data.SeenCallEntity

/**
 * Notes each call the app was asked about while it was on, allowed or not: the record the
 * frequent callers are found in (ADR-010). A call that rang because the mode in effect was
 * Off, a pause included, says nothing about the caller and is not kept. The record is
 * trimmed to the window the finder reads, with every call written.
 */
class SeenCallRecorder(
    private val seenCalls: SeenCallDao,
    private val windowDays: Int = FrequentLimits().windowDays,
) {
    suspend fun note(call: ScreenedCall) {
        if (call.number.key.isEmpty() || call.decision.ruleId in OFF_RULES) return
        seenCalls.insert(SeenCallEntity(numberKey = call.number.key, atMillis = call.receivedAtMillis))
        seenCalls.deleteOlderThan(call.receivedAtMillis - windowDays * DAY_MILLIS)
    }

    companion object {
        val OFF_RULES = setOf(RuleBook.OFF, RuleBook.PAUSED, RuleBook.SCHEDULED_OFF)
        private const val DAY_MILLIS = 24 * 60 * 60_000L
    }
}
