package com.cyanharborstudios.callblock.screening

import com.cyanharborstudios.callblock.core.numbers.PhoneNumbers
import com.cyanharborstudios.callblock.core.rules.ScreeningSettings
import com.cyanharborstudios.callblock.data.DialledNumberDao
import com.cyanharborstudios.callblock.data.DialledNumberEntity

/**
 * Remembers a number the user called, so that it can ring when it calls back.
 *
 * Android shows a screening app an outgoing call only when the number is not in the user's
 * contacts, which are exactly the numbers that would otherwise be stopped. A number is kept
 * for as long as the setting lets it ring back and no longer, and nothing is kept while the
 * setting is off.
 */
class DialledNumberRecorder(
    private val dialledNumbers: DialledNumberDao,
    private val settings: suspend () -> ScreeningSettings,
    /** The phone's own country, e.g. "IN": a number dialled without its country code is read as local. */
    private val homeRegion: () -> String,
) {
    suspend fun record(rawNumber: String?, dialledAtMillis: Long) {
        val current = settings()
        if (!current.callBacksRing) return
        val key = PhoneNumbers(homeRegion()).parse(rawNumber).key
        if (key.isEmpty()) return
        dialledNumbers.upsert(DialledNumberEntity(numberKey = key, atMillis = dialledAtMillis))
        dialledNumbers.deleteOlderThan(dialledAtMillis - current.callBackWindowMinutes * 60_000L)
    }
}
