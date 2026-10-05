package com.cyanharborstudios.callblock.core.rules

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/** What put the app in the mode it is in. */
enum class ModeSource {
    /** The lever's own stop. */
    LEVER,

    /** A timer the user started: a pause, or Silence or Block for a while. */
    TIMER,

    /** An hour of the week the schedule has set. */
    SCHEDULE,
}

/** The mode in effect at one moment, why, and what follows. */
data class ModeNow(
    val mode: Mode,
    val source: ModeSource,
    /** When the mode next changes by itself, or null if it holds until the user changes something. */
    val untilMillis: Long?,
    /** The mode from [untilMillis] on. Null when [untilMillis] is. */
    val next: Mode?,
)

/**
 * Works out the mode in effect at a moment. A running timer comes first, then the
 * schedule's hour, then the lever. No clock of its own: the moment and the time zone are
 * arguments, like everything else the rules look at.
 */
object ModeClock {

    fun at(settings: ScreeningSettings, atMillis: Long, zone: ZoneId): ModeNow {
        if (atMillis < settings.timerUntilMillis) {
            val afterTimer = withoutTimer(settings, settings.timerUntilMillis, zone)
            return ModeNow(settings.timerMode, ModeSource.TIMER, settings.timerUntilMillis, afterTimer.mode)
        }
        return withoutTimer(settings, atMillis, zone)
    }

    /** The schedule's hour when it asks for a mode, else the lever; and the next hour that differs. */
    private fun withoutTimer(settings: ScreeningSettings, atMillis: Long, zone: ZoneId): ModeNow {
        if (!settings.scheduleOn || settings.schedule.isEmpty) {
            return ModeNow(settings.mode, ModeSource.LEVER, null, null)
        }
        val thisHour = ZonedDateTime.ofInstant(Instant.ofEpochMilli(atMillis), zone).truncatedTo(ChronoUnit.HOURS)
        val asked = settings.schedule.modeAt(thisHour.dayOfWeek, thisHour.hour)
        val mode = asked ?: settings.mode
        val source = if (asked != null) ModeSource.SCHEDULE else ModeSource.LEVER
        // Walk forward an hour at a time. A week later the schedule repeats, so if nothing
        // differs within a week, nothing ever will.
        var hour = thisHour
        repeat(WeekSchedule.HOURS_IN_WEEK) {
            hour = hour.plusHours(1)
            val then = settings.schedule.modeAt(hour.dayOfWeek, hour.hour) ?: settings.mode
            if (then != mode) return ModeNow(mode, source, hour.toInstant().toEpochMilli(), then)
        }
        return ModeNow(mode, source, null, null)
    }
}
