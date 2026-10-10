package com.cyanharborstudios.callblock.core.rules

/** The main switch. */
enum class Mode { OFF, SILENCE, BLOCK }

/**
 * Which unknown callers the lever's stop applies to: all of them, only those from abroad,
 * or only the frequent callers the user has blocked (Plus), with every other unknown
 * number ringing.
 */
enum class Scope { ALL_UNKNOWN, INTERNATIONAL_ONLY, FREQUENT_ONLY }

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
     * Let a number ring when the user called it themselves within [callBackWindowMinutes]:
     * the clinic, the courier or the support line ringing back. On as installed, because a
     * call the user asked for is the last one a blocker should stop.
     */
    val callBacksRing: Boolean = true,
    val callBackWindowMinutes: Int = DEFAULT_CALL_BACK_WINDOW_MINUTES,
    /** The user's own rules about how a number starts: always ring it, or always block it. */
    val numberRules: List<NumberRule> = emptyList(),
    /**
     * Block India's 140 series, which TRAI reserves for promotional calls from registered
     * telemarketers. Off until the user asks: TRAI's rules of September 2026 bar a
     * call-management app from blocking the series on its own, while the user may block
     * whatever they choose. (The 160 series is not a setting: it always rings.)
     */
    val promotionalSeriesBlocked: Boolean = false,
    /**
     * The frequent callers the user has blocked (Plus): each is the start the numbers of a
     * class share, or one number's whole key. Each gets the lever's action, and no automatic
     * pass lets it through. Newest first.
     */
    val frequentCallers: List<String> = emptyList(),
    /** Block a newly found frequent caller without asking (Plus). Off as installed. */
    val frequentAutoBlock: Boolean = false,
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
        const val DEFAULT_CALL_BACK_WINDOW_MINUTES = 24 * 60
    }
}
