package com.cyanharborstudios.callblock.core.rules

/** The main switch. */
enum class Mode { OFF, SILENCE, BLOCK }

/** Which unknown callers the filter applies to. */
enum class Scope { ALL_UNKNOWN, INTERNATIONAL_ONLY }

/** The user's choices that shape the rule list. */
data class ScreeningSettings(
    /** The lever's own stop: what applies when no timer is running and the schedule asks for nothing. */
    val mode: Mode = Mode.OFF,
    val scope: Scope = Scope.ALL_UNKNOWN,
    /**
     * A timer holds [timerMode] until this moment. 0 means no timer. A pause is a timer at
     * Off, which is what [timerMode] is unless the user chose Silence or Block for a while.
     */
    val timerUntilMillis: Long = 0,
    val timerMode: Mode = Mode.OFF,
    /** The hours of the week that set the mode by themselves, while [scheduleOn]. */
    val schedule: WeekSchedule = WeekSchedule.EMPTY,
    val scheduleOn: Boolean = false,
    /** Let a number ring when it calls again soon after being blocked or silenced. */
    val repeatCallsRing: Boolean = false,
    val repeatWindowMinutes: Int = DEFAULT_REPEAT_WINDOW_MINUTES,
    val allowListEnabled: Boolean = false,
    /**
     * Block India's 140 series, which TRAI reserves for promotional calls from registered
     * telemarketers. Off until the user asks: TRAI's rules of September 2026 bar a
     * call-management app from blocking the series on its own, while the user may block
     * whatever they choose. (The 160 series is not a setting: it always rings.)
     */
    val promotionalSeriesBlocked: Boolean = false,
) {
    /**
     * True when these settings can ever stop a call from [atMillis] on: the lever is not at
     * Off, or a running timer or an hour of the schedule asks for Silence or Block. When
     * false, the app has no need of the call-screening role.
     */
    fun asksToFilter(atMillis: Long): Boolean =
        mode != Mode.OFF ||
            (atMillis < timerUntilMillis && timerMode != Mode.OFF) ||
            (scheduleOn && schedule.hours.any { it == Mode.SILENCE || it == Mode.BLOCK })

    companion object {
        const val DEFAULT_REPEAT_WINDOW_MINUTES = 15
    }
}
