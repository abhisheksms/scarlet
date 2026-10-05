package com.cyanharborstudios.callblock

import android.content.Context
import com.cyanharborstudios.callblock.ads.AdsController
import com.cyanharborstudios.callblock.billing.PlayStore
import com.cyanharborstudios.callblock.data.AllowedNumberDao
import com.cyanharborstudios.callblock.data.AppDatabase
import com.cyanharborstudios.callblock.data.DialledNumberDao
import com.cyanharborstudios.callblock.data.HandledCallDao
import com.cyanharborstudios.callblock.data.SettingsStore
import com.cyanharborstudios.callblock.notify.Notifier
import com.cyanharborstudios.callblock.reports.ReportScheduler
import com.cyanharborstudios.callblock.screening.CallScreener
import com.cyanharborstudios.callblock.screening.DialledNumberRecorder
import com.cyanharborstudios.callblock.screening.EmergencyCallPause
import com.cyanharborstudios.callblock.screening.HandledCallRecorder
import com.cyanharborstudios.callblock.screening.ScreeningRole
import com.cyanharborstudios.callblock.screening.StoredScreeningFacts
import com.cyanharborstudios.callblock.screening.homeRegion
import com.cyanharborstudios.callblock.screening.isEmergencyNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.time.ZoneId

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
    val dialledNumbers: DialledNumberDao get() = database.dialledNumbers()

    val notifier: Notifier by lazy { Notifier(appContext) }

    val screeningRole: ScreeningRole by lazy { ScreeningRole(appContext) }

    val reportScheduler: ReportScheduler by lazy { ReportScheduler(appContext) }

    /** Only ever touched from the activity. The screening path must not start the ads SDK. */
    val ads: AdsController by lazy { AdsController(appContext, applicationScope) }

    /** Only ever touched from the activity, like the ads. */
    val store: PlayStore by lazy { PlayStore(appContext, applicationScope, settingsStore) }

    val callScreener: CallScreener by lazy {
        CallScreener(
            facts = StoredScreeningFacts(settingsStore, allowedNumbers, handledCalls, dialledNumbers),
            homeRegion = { homeRegion(appContext) },
            zone = { ZoneId.systemDefault() },
        )
    }

    val handledCallRecorder: HandledCallRecorder by lazy {
        HandledCallRecorder(handledCalls, settingsStore, notifier)
    }

    val emergencyCallPause: EmergencyCallPause by lazy {
        EmergencyCallPause(
            isEmergencyNumber = { number -> isEmergencyNumber(appContext, number) },
            pause = { atMillis -> settingsStore.pauseAfterEmergencyCall(atMillis) },
        )
    }

    val dialledNumberRecorder: DialledNumberRecorder by lazy {
        DialledNumberRecorder(
            dialledNumbers = dialledNumbers,
            settings = { settingsStore.current().screening },
            homeRegion = { homeRegion(appContext) },
        )
    }
}
