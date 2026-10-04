package com.cyanharborstudios.callblock.core.rules

/** The main switch. */
enum class Mode { OFF, SILENCE, BLOCK }

/** Which unknown callers the filter applies to. */
enum class Scope { ALL_UNKNOWN, INTERNATIONAL_ONLY }

/** The user's choices that shape the rule list. */
data class ScreeningSettings(
    val mode: Mode = Mode.OFF,
    val scope: Scope = Scope.ALL_UNKNOWN,
    /** Filtering is paused until this moment. 0 means not paused. */
    val pausedUntilMillis: Long = 0,
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
    companion object {
        const val DEFAULT_REPEAT_WINDOW_MINUTES = 15
    }
}
