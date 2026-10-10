package com.cyanharborstudios.callblock.screening

import com.cyanharborstudios.callblock.core.frequent.FrequentCallers
import com.cyanharborstudios.callblock.core.frequent.FrequentLimits
import com.cyanharborstudios.callblock.data.AllowedNumberDao
import com.cyanharborstudios.callblock.data.DialledNumberDao
import com.cyanharborstudios.callblock.data.SeenCallDao
import com.cyanharborstudios.callblock.data.SettingsStore
import com.cyanharborstudios.callblock.notify.Notifier
import java.time.ZoneId

/**
 * After each call the app was asked about: looks for frequent callers in the record, and
 * when a new one has appeared, says so once, in a quiet notification that opens the list.
 * With Auto-block on (Plus) the new one is blocked at the same moment. A caller already
 * announced is never announced again, whether or not the user did anything about it.
 */
class FrequentCallerWatch(
    private val seenCalls: SeenCallDao,
    private val settingsStore: SettingsStore,
    private val allowedNumbers: AllowedNumberDao,
    private val dialledNumbers: DialledNumberDao,
    private val notifier: Notifier,
    private val zone: () -> ZoneId,
    private val limits: FrequentLimits = FrequentLimits(),
) {
    suspend fun afterCall(nowMillis: Long) {
        val settings = settingsStore.current()
        val calls = seenCalls.since(nowMillis - limits.windowDays * DAY_MILLIS).map { it.toSeenCall() }
        val allowed = allowedNumbers.all().filter { it.expiresAtMillis == null || it.expiresAtMillis > nowMillis }.map { it.numberKey }.toSet()
        val dialled = dialledNumbers.keys().toSet()
        val found = FrequentCallers.classes(calls, nowMillis, zone(), settings.screening, allowed, dialled, limits)
        val new = found.map { it.start }.filter { it !in settings.frequentAnnounced }
        if (new.isEmpty()) return
        val blocking = settings.screening.frequentAutoBlock
        if (blocking) settingsStore.blockFrequentCallers(new)
        settingsStore.markFrequentAnnounced(new)
        notifier.frequentCallers(new.size, blocked = blocking)
    }

    private companion object {
        const val DAY_MILLIS = 24 * 60 * 60_000L
    }
}
