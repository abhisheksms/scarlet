package com.cyanharborstudios.callblock.core.rules

import java.time.ZoneId

/** What a move of the lever changes in the settings. */
object LeverMoves {

    /** How long a move holds against a schedule that asks for the same mode all week. */
    const val HOLD_WHEN_SCHEDULE_NEVER_CHANGES_MILLIS = 24 * 60 * 60_000L

    /**
     * The user moves the lever to [wanted] at [atMillis].
     *
     * Any running timer ends: the user has taken the lever back. Outside the schedule's
     * hours the move sets the lever's own stop. Inside an hour the schedule has set, the
     * lever's own stop is left alone and the move holds until the schedule next changes,
     * as a timer; after that the schedule and the lever carry on as before. Moving to
     * what the schedule already asks for simply goes back to the schedule.
     */
    fun move(settings: ScreeningSettings, wanted: Mode, atMillis: Long, zone: ZoneId): ScreeningSettings {
        val withoutTimer = settings.copy(timerUntilMillis = 0, timerMode = Mode.OFF)
        val scheduled = ModeClock.at(withoutTimer, atMillis, zone)
        if (scheduled.source != ModeSource.SCHEDULE) return withoutTimer.copy(mode = wanted)
        if (wanted == scheduled.mode) return withoutTimer
        val until = scheduled.untilMillis ?: (atMillis + HOLD_WHEN_SCHEDULE_NEVER_CHANGES_MILLIS)
        return withoutTimer.copy(timerUntilMillis = until, timerMode = wanted)
    }
}
