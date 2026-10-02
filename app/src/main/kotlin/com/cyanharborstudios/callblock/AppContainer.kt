package com.cyanharborstudios.callblock

import android.content.Context
import com.cyanharborstudios.callblock.data.AllowedNumberDao
import com.cyanharborstudios.callblock.data.AppDatabase
import com.cyanharborstudios.callblock.data.HandledCallDao
import com.cyanharborstudios.callblock.data.SettingsStore
import com.cyanharborstudios.callblock.notify.Notifier
import com.cyanharborstudios.callblock.screening.CallScreener
import com.cyanharborstudios.callblock.screening.HandledCallRecorder
import com.cyanharborstudios.callblock.screening.ScreeningRole
import com.cyanharborstudios.callblock.screening.StoredScreeningFacts
import com.cyanharborstudios.callblock.screening.homeRegion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * The app's objects, created once and wired by hand. Everything is lazy: the screening
 * service can start this process cold for an incoming call, and should only pay for what
 * that call needs.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    /** Outlives any screen or service. Used for work that must finish, like writing the log. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settingsStore: SettingsStore by lazy { SettingsStore(appContext) }

    private val database: AppDatabase by lazy { AppDatabase.open(appContext) }
    val handledCalls: HandledCallDao get() = database.handledCalls()
    val allowedNumbers: AllowedNumberDao get() = database.allowedNumbers()

    val notifier: Notifier by lazy { Notifier(appContext) }

    val screeningRole: ScreeningRole by lazy { ScreeningRole(appContext) }

    val callScreener: CallScreener by lazy {
        CallScreener(
            facts = StoredScreeningFacts(settingsStore, allowedNumbers, handledCalls),
            homeRegion = { homeRegion(appContext) },
        )
    }

    val handledCallRecorder: HandledCallRecorder by lazy {
        HandledCallRecorder(handledCalls, settingsStore, notifier)
    }
}
