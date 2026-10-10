package com.cyanharborstudios.callblock.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cyanharborstudios.callblock.AppContainer
import com.cyanharborstudios.callblock.BuildConfig
import com.cyanharborstudios.callblock.billing.StoreState
import com.cyanharborstudios.callblock.core.numbers.PhoneNumber
import com.cyanharborstudios.callblock.core.plans.Tier
import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.NumberRule
import com.cyanharborstudios.callblock.core.rules.Scope
import com.cyanharborstudios.callblock.core.rules.WeekSchedule
import com.cyanharborstudios.callblock.core.stats.ReportFrequency
import com.cyanharborstudios.callblock.core.stats.ReportPlanner
import com.cyanharborstudios.callblock.data.AllowedNumberEntity
import com.cyanharborstudios.callblock.data.AppSettings
import com.cyanharborstudios.callblock.core.frequent.FrequentLimits
import com.cyanharborstudios.callblock.data.HandledCallEntity
import com.cyanharborstudios.callblock.data.SeenCallEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * What the screens show and what the user can change. One view model for the whole app:
 * six small screens over the same three stores.
 *
 * The three lists start as null, meaning "not loaded yet", so a screen never flashes
 * default values before the real ones arrive.
 */
class AppViewModel(private val container: AppContainer) : ViewModel() {

    val settings: StateFlow<AppSettings?> = container.settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val handledCalls: StateFlow<List<HandledCallEntity>?> = container.handledCalls.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val allowedNumbers: StateFlow<List<AllowedNumberEntity>?> = container.allowedNumbers.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** The record the frequent callers are found in: every call the app was asked about in the finder's window. */
    val seenCalls: StateFlow<List<SeenCallEntity>?> = container.seenCalls.observeSince(System.currentTimeMillis() - FrequentLimits().windowDays * DAY_MILLIS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** What Google Play has on sale, and whether it could be asked. */
    val store: StateFlow<StoreState> get() = container.store.state

    private val roleHeldState = MutableStateFlow(container.screeningRole.isHeld())
    val roleHeld: StateFlow<Boolean> = roleHeldState.asStateFlow()

    val roleAvailable: Boolean get() = container.screeningRole.isAvailable()

    private val canNotifyState = MutableStateFlow(container.notifier.canNotify())
    val canNotify: StateFlow<Boolean> = canNotifyState.asStateFlow()

    init {
        // Re-assert the daily report check on each launch; scheduling it twice is harmless.
        viewModelScope.launch {
            container.reportScheduler.sync(container.settingsStore.current().reportFrequency)
        }
    }

    /** The user can change the role or the notification permission outside the app. Called on resume. */
    fun refreshSystemState() {
        roleHeldState.value = container.screeningRole.isHeld()
        canNotifyState.value = container.notifier.canNotify()
    }

    fun roleRequestIntent() = container.screeningRole.requestIntent()

    // --- the switch and its options ---

    /** The user moved the lever. Inside the schedule's hours that holds until they end (see LeverMoves). */
    fun setMode(mode: Mode) = change { moveLever(mode, System.currentTimeMillis(), ZoneId.systemDefault()) }

    fun setScope(scope: Scope) = change { setScope(scope) }

    fun pauseFor(minutes: Int) = startTimer(Mode.OFF, minutes)

    /** Holds [mode] for [minutes], then the schedule or the lever takes over again. */
    fun startTimer(mode: Mode, minutes: Int) = change { startTimer(mode, System.currentTimeMillis() + minutes * 60_000L) }

    /** Ends a pause or any other timer now. */
    fun resume() = change { endTimer() }

    /** Stores the week's hours. Setting an hour to a mode also switches the schedule on, as adding a number switches the allow list on. */
    fun setSchedule(schedule: WeekSchedule, switchOn: Boolean) {
        viewModelScope.launch {
            container.settingsStore.setSchedule(schedule)
            if (switchOn) container.settingsStore.setScheduleOn(true)
        }
    }

    fun setScheduleOn(on: Boolean) = change { setScheduleOn(on) }

    /** Opens Google Play's purchase screen for one of the plans' products; a subscription needs the offer chosen. */
    fun buy(activity: Activity, productId: String, offerToken: String? = null) {
        container.store.buy(activity, productId, offerToken)
    }

    fun restorePurchases() = container.store.restore()

    /** Debug builds only: try the app on another tier. Does nothing in a release build. */
    fun setDebugTier(tier: Tier) {
        if (BuildConfig.DEBUG) change { setDebugTier(tier) }
    }

    fun setRepeatCallsRing(enabled: Boolean) = change { setRepeatCallsRing(enabled) }

    fun setRepeatWindowMinutes(minutes: Int) = change { setRepeatWindowMinutes(minutes) }

    fun setAllowListEnabled(enabled: Boolean) = change { setAllowListEnabled(enabled) }

    /** Whether a number the user called may ring back. Switching it off also forgets the numbers kept for it. */
    fun setCallBacksRing(enabled: Boolean) {
        viewModelScope.launch {
            container.settingsStore.setCallBacksRing(enabled)
            if (!enabled) container.dialledNumbers.deleteAll()
        }
    }

    fun setPromotionalSeriesBlocked(enabled: Boolean) = change { setPromotionalSeriesBlocked(enabled) }

    fun setNotifyHandledCalls(enabled: Boolean) = change { setNotifyHandledCalls(enabled) }

    fun setStatsPeriodDays(days: Int) = change { setStatsPeriodDays(days) }

    /** Android's notification prompt has been shown once; Home can now tell a refusal from "not asked". */
    fun markNotificationsAsked() = change { setNotificationsAsked() }

    /** How It Works has been closed once; from now on it opens only when asked for. */
    fun markHowItWorksSeen() = change { setHowItWorksSeen() }

    /**
     * Switching reports on starts with the *next* period to finish: the one that has
     * already ended is marked as sent, so no report arrives for time before the user asked.
     */
    fun setReportFrequency(frequency: ReportFrequency) {
        viewModelScope.launch {
            val alreadyFinished = ReportPlanner.lastFinishedPeriod(frequency, LocalDate.now())?.key
            container.settingsStore.setReports(frequency, alreadyReportedKey = alreadyFinished)
            // Only after the setting is stored: the job reads it as soon as it is scheduled.
            container.reportScheduler.sync(frequency)
        }
    }

    // --- number rules ---

    /** Adds one of the user's own rules about how a number starts, or changes the one for the same start. */
    fun addNumberRule(rule: NumberRule) = change { addNumberRule(rule) }

    fun removeNumberRule(start: String) = change { removeNumberRule(start) }

    // --- frequent callers (Plus) ---

    fun blockFrequentCaller(start: String) = change { blockFrequentCallers(listOf(start)) }

    fun unblockFrequentCaller(start: String) = change { unblockFrequentCaller(start) }

    fun setFrequentAutoBlock(enabled: Boolean) = change { setFrequentAutoBlock(enabled) }

    // --- the allow list ---

    /** Adds [number] to the allow list, for good or for [minutes], and switches the list on. */
    fun allow(number: PhoneNumber, minutes: Int?) {
        if (number.key.isEmpty()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            container.allowedNumbers.upsert(
                AllowedNumberEntity(
                    numberKey = number.key,
                    numberRaw = number.raw,
                    addedAtMillis = now,
                    expiresAtMillis = minutes?.let { now + it * 60_000L },
                ),
            )
            container.settingsStore.setAllowListEnabled(true)
        }
    }

    fun removeAllowed(numberKey: String) {
        viewModelScope.launch { container.allowedNumbers.delete(numberKey) }
    }

    /** Entries whose time has passed no longer match anything; this just tidies them away. */
    fun removeExpiredAllowed() {
        viewModelScope.launch { container.allowedNumbers.deleteExpired(System.currentTimeMillis()) }
    }

    // --- the history ---

    fun deleteHandledCall(id: Long) {
        viewModelScope.launch { container.handledCalls.delete(id) }
    }

    /** Delete All in History empties the record the frequent callers are found in too: it is the same kind of memory. */
    fun deleteAllHandledCalls() {
        viewModelScope.launch {
            container.handledCalls.deleteAll()
            container.seenCalls.deleteAll()
        }
    }

    private fun change(block: suspend com.cyanharborstudios.callblock.data.SettingsStore.() -> Unit) {
        viewModelScope.launch { container.settingsStore.block() }
    }

    private companion object {
        const val DAY_MILLIS = 24 * 60 * 60_000L
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(container) as T
    }
}
