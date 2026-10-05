package com.cyanharborstudios.callblock.screening

import com.cyanharborstudios.callblock.core.rules.ScreeningSettings
import com.cyanharborstudios.callblock.data.AllowedNumberDao
import com.cyanharborstudios.callblock.data.DialledNumberDao
import com.cyanharborstudios.callblock.data.HandledCallDao
import com.cyanharborstudios.callblock.data.SettingsStore

/** [ScreeningFacts] read from the settings file and the database. */
class StoredScreeningFacts(
    private val settingsStore: SettingsStore,
    private val allowedNumbers: AllowedNumberDao,
    private val handledCalls: HandledCallDao,
    private val dialledNumbers: DialledNumberDao,
) : ScreeningFacts {

    override suspend fun settings(): ScreeningSettings = settingsStore.current().screening

    override suspend fun allowList(): Map<String, Long?> =
        allowedNumbers.all().associate { it.numberKey to it.expiresAtMillis }

    override suspend fun lastHandledAt(numberKey: String): Long? = handledCalls.lastHandledAt(numberKey)

    override suspend fun lastDialledAt(numberKey: String): Long? = dialledNumbers.lastDialledAt(numberKey)
}
