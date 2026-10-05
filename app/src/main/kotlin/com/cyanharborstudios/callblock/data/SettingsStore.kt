package com.cyanharborstudios.callblock.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cyanharborstudios.callblock.core.rules.LeverMoves
import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.Scope
import com.cyanharborstudios.callblock.core.rules.ScreeningSettings
import com.cyanharborstudios.callblock.core.rules.WeekSchedule
import com.cyanharborstudios.callblock.core.stats.ReportFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.ZoneId

/** Everything the user has chosen, plus the bookkeeping values the app keeps for itself. */
data class AppSettings(
    val screening: ScreeningSettings = ScreeningSettings(),
    /** Post a notification each time a call is blocked or silenced. */
    val notifyHandledCalls: Boolean = false,
    val reportFrequency: ReportFrequency = ReportFrequency.OFF,
    /** The key of the last report period already sent (see ReportPlanner). */
    val lastReportedPeriodKey: String? = null,
    /** The highest milestone already announced, so one is never announced twice. */
    val highestMilestoneAnnounced: Int = 0,
    /** The period the statistics charts cover: 7, 30 or 90 days. */
    val statsPeriodDays: Int = 30,
    /** True once Android's notification prompt has been shown, so a refusal can be told from "not asked yet". */
    val notificationsAsked: Boolean = false,
    /** True once How It Works has been closed, so it opens by itself only until then. */
    val howItWorksSeen: Boolean = false,
)

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Reads and writes the settings file. An unreadable or unknown value falls back to its default. */
class SettingsStore(context: Context) {

    private val dataStore = context.applicationContext.settingsDataStore

    val settings: Flow<AppSettings> = dataStore.data.map(::fromPreferences)

    suspend fun current(): AppSettings = settings.first()

    /**
     * The user moved the lever to [wanted]. What that changes depends on the timer and the
     * schedule (see [LeverMoves]); it is read and written in one step so two quick moves
     * cannot cross.
     */
    suspend fun moveLever(wanted: Mode, atMillis: Long, zone: ZoneId) = edit { prefs ->
        val moved = LeverMoves.move(fromPreferences(prefs).screening, wanted, atMillis, zone)
        prefs[MODE] = moved.mode.name
        prefs[TIMER_UNTIL] = moved.timerUntilMillis
        prefs[TIMER_MODE] = moved.timerMode.name
    }

    suspend fun setScope(scope: Scope) = edit { it[SCOPE] = scope.name }

    /** Holds [mode] until [untilMillis]. A pause is a timer at Off. */
    suspend fun startTimer(mode: Mode, untilMillis: Long) = edit {
        it[TIMER_UNTIL] = untilMillis
        it[TIMER_MODE] = mode.name
    }

    suspend fun endTimer() = edit {
        it[TIMER_UNTIL] = 0
        it[TIMER_MODE] = Mode.OFF.name
    }

    suspend fun setSchedule(schedule: WeekSchedule) = edit { it[SCHEDULE_HOURS] = schedule.encode() }

    suspend fun setScheduleOn(on: Boolean) = edit { it[SCHEDULE_ON] = on }

    suspend fun setRepeatCallsRing(enabled: Boolean) = edit { it[REPEAT_CALLS_RING] = enabled }

    suspend fun setRepeatWindowMinutes(minutes: Int) = edit { it[REPEAT_WINDOW_MINUTES] = minutes }

    suspend fun setAllowListEnabled(enabled: Boolean) = edit { it[ALLOW_LIST_ENABLED] = enabled }

    suspend fun setPromotionalSeriesBlocked(enabled: Boolean) = edit { it[PROMOTIONAL_SERIES_BLOCKED] = enabled }

    suspend fun setNotifyHandledCalls(enabled: Boolean) = edit { it[NOTIFY_HANDLED_CALLS] = enabled }

    /**
     * Changes how often reports come, and records [alreadyReportedKey] as the last period
     * reported, in one write. Done together so the report job can never see the new
     * frequency without the key, and send a report for a period the user did not ask about.
     */
    suspend fun setReports(frequency: ReportFrequency, alreadyReportedKey: String?) = edit {
        it[REPORT_FREQUENCY] = frequency.name
        if (alreadyReportedKey == null) it.remove(LAST_REPORTED_PERIOD) else it[LAST_REPORTED_PERIOD] = alreadyReportedKey
    }

    suspend fun setLastReportedPeriodKey(key: String) = edit { it[LAST_REPORTED_PERIOD] = key }

    suspend fun setHighestMilestoneAnnounced(milestone: Int) = edit { it[HIGHEST_MILESTONE] = milestone }

    suspend fun setStatsPeriodDays(days: Int) = edit { it[STATS_PERIOD_DAYS] = days }

    suspend fun setNotificationsAsked() = edit { it[NOTIFICATIONS_ASKED] = true }

    suspend fun setHowItWorksSeen() = edit { it[HOW_IT_WORKS_SEEN] = true }

    private suspend fun edit(change: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit(change)
    }

    private fun fromPreferences(prefs: Preferences): AppSettings {
        val defaults = ScreeningSettings()
        return AppSettings(
            screening = ScreeningSettings(
                mode = enumOr(prefs[MODE], defaults.mode),
                scope = enumOr(prefs[SCOPE], defaults.scope),
                timerUntilMillis = prefs[TIMER_UNTIL] ?: 0,
                timerMode = enumOr(prefs[TIMER_MODE], Mode.OFF),
                schedule = WeekSchedule.decode(prefs[SCHEDULE_HOURS]),
                scheduleOn = prefs[SCHEDULE_ON] ?: false,
                repeatCallsRing = prefs[REPEAT_CALLS_RING] ?: false,
                repeatWindowMinutes = prefs[REPEAT_WINDOW_MINUTES] ?: defaults.repeatWindowMinutes,
                allowListEnabled = prefs[ALLOW_LIST_ENABLED] ?: false,
                promotionalSeriesBlocked = prefs[PROMOTIONAL_SERIES_BLOCKED] ?: false,
            ),
            notifyHandledCalls = prefs[NOTIFY_HANDLED_CALLS] ?: false,
            reportFrequency = enumOr(prefs[REPORT_FREQUENCY], ReportFrequency.OFF),
            lastReportedPeriodKey = prefs[LAST_REPORTED_PERIOD],
            highestMilestoneAnnounced = prefs[HIGHEST_MILESTONE] ?: 0,
            statsPeriodDays = prefs[STATS_PERIOD_DAYS] ?: 30,
            notificationsAsked = prefs[NOTIFICATIONS_ASKED] ?: false,
            howItWorksSeen = prefs[HOW_IT_WORKS_SEEN] ?: false,
        )
    }

    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: fallback

    private companion object {
        val MODE = stringPreferencesKey("mode")
        val SCOPE = stringPreferencesKey("scope")
        // The key keeps its first name: a pause was the only timer there was, and installs have it stored.
        val TIMER_UNTIL = longPreferencesKey("paused_until_millis")
        val TIMER_MODE = stringPreferencesKey("timer_mode")
        val SCHEDULE_HOURS = stringPreferencesKey("schedule_hours")
        val SCHEDULE_ON = booleanPreferencesKey("schedule_on")
        val REPEAT_CALLS_RING = booleanPreferencesKey("repeat_calls_ring")
        val REPEAT_WINDOW_MINUTES = intPreferencesKey("repeat_window_minutes")
        val ALLOW_LIST_ENABLED = booleanPreferencesKey("allow_list_enabled")
        val PROMOTIONAL_SERIES_BLOCKED = booleanPreferencesKey("promotional_series_blocked")
        val NOTIFY_HANDLED_CALLS = booleanPreferencesKey("notify_handled_calls")
        val REPORT_FREQUENCY = stringPreferencesKey("report_frequency")
        val LAST_REPORTED_PERIOD = stringPreferencesKey("last_reported_period")
        val HIGHEST_MILESTONE = intPreferencesKey("highest_milestone_announced")
        val STATS_PERIOD_DAYS = intPreferencesKey("stats_period_days")
        val NOTIFICATIONS_ASKED = booleanPreferencesKey("notifications_asked")
        val HOW_IT_WORKS_SEEN = booleanPreferencesKey("how_it_works_seen")
    }
}
