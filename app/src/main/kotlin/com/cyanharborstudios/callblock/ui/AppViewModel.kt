package com.cyanharborstudios.callblock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cyanharborstudios.callblock.AppContainer
import com.cyanharborstudios.callblock.core.numbers.PhoneNumber
import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.Scope
import com.cyanharborstudios.callblock.core.stats.ReportFrequency
import com.cyanharborstudios.callblock.core.stats.ReportPlanner
import com.cyanharborstudios.callblock.data.AllowedNumberEntity
import com.cyanharborstudios.callblock.data.AppSettings
import com.cyanharborstudios.callblock.data.HandledCallEntity
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

    fun setRepeatCallsRing(enabled: Boolean) = change { setRepeatCallsRing(enabled) }

    fun setRepeatWindowMinutes(minutes: Int) = change { setRepeatWindowMinutes(minutes) }

    fun setAllowListEnabled(enabled: Boolean) = change { setAllowListEnabled(enabled) }

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

    fun deleteAllHandledCalls() {
        viewModelScope.launch { container.handledCalls.deleteAll() }
    }

    private fun change(block: suspend com.cyanharborstudios.callblock.data.SettingsStore.() -> Unit) {
        viewModelScope.launch { container.settingsStore.block() }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(container) as T
    }
}
