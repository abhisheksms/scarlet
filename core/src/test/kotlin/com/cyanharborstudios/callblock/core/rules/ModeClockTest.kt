package com.cyanharborstudios.callblock.core.rules

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

/** The mode in effect at a moment: a timer first, then the schedule's hour, then the lever. */
class ModeClockTest {

    @Test
    fun `ending a pause brings back the lever's stop, or the hour the schedule has set`() {
        val at = ZonedDateTime.of(2026, 10, 5, 12, 30, 0, 0, ZoneId.of("Asia/Kolkata"))
        val zoneHere = at.zone
        val now = at.toInstant().toEpochMilli()
        val paused = ScreeningSettings(mode = Mode.BLOCK, timerMode = Mode.OFF, timerUntilMillis = now + 60_000)
        assertEquals(Mode.BLOCK, ModeClock.afterEndingTimer(paused, now, zoneHere))
        assertEquals(Mode.SILENCE, ModeClock.afterEndingTimer(paused.copy(mode = Mode.SILENCE), now, zoneHere))
        // A timer at Block over a lever at Off: ending it brings Off back.
        val held = ScreeningSettings(mode = Mode.OFF, timerMode = Mode.BLOCK, timerUntilMillis = now + 60_000)
        assertEquals(Mode.OFF, ModeClock.afterEndingTimer(held, now, zoneHere))
        // The schedule's hour outranks the lever's stop, in both directions.
        val mondayNoon = WeekSchedule.EMPTY.with(listOf(DayOfWeek.MONDAY), 12..12, Mode.BLOCK)
        val scheduled = ScreeningSettings(mode = Mode.OFF, schedule = mondayNoon, scheduleOn = true, timerMode = Mode.OFF, timerUntilMillis = now + 60_000)
        assertEquals(Mode.BLOCK, ModeClock.afterEndingTimer(scheduled, now, zoneHere))
        val offAtNoon = WeekSchedule.EMPTY.with(listOf(DayOfWeek.MONDAY), 12..12, Mode.OFF)
        val scheduledOff = paused.copy(schedule = offAtNoon, scheduleOn = true)
        assertEquals(Mode.OFF, ModeClock.afterEndingTimer(scheduledOff, now, zoneHere))
        // With the schedule switched off it is the lever again.
        assertEquals(Mode.BLOCK, ModeClock.afterEndingTimer(scheduledOff.copy(scheduleOn = false), now, zoneHere))
    }

    private val india = ZoneId.of("Asia/Kolkata")
    private val minute = 60_000L
    private val hour = 60 * minute

    /** A moment on Monday 5 October 2026, in [zone]. */
    private fun monday(hourOfDay: Int, minuteOfHour: Int = 0, zone: ZoneId = india): Long =
        ZonedDateTime.of(2026, 10, 5, hourOfDay, minuteOfHour, 0, 0, zone).toInstant().toEpochMilli()

    private fun at(settings: ScreeningSettings, millis: Long, zone: ZoneId = india) = ModeClock.at(settings, millis, zone)

    // --- the lever alone ---

    @Test
    fun `with no timer and no schedule the lever decides and nothing is due to change`() {
        for (mode in Mode.entries) {
            assertEquals(ModeNow(mode, ModeSource.LEVER, null, null), at(ScreeningSettings(mode = mode), monday(12)))
        }
    }

    // --- a timer ---

    @Test
    fun `a running timer decides, and says when it ends and what follows`() {
        val settings = ScreeningSettings(mode = Mode.SILENCE, timerMode = Mode.BLOCK, timerUntilMillis = monday(13))
        assertEquals(ModeNow(Mode.BLOCK, ModeSource.TIMER, monday(13), Mode.SILENCE), at(settings, monday(12, 59)))
    }

    @Test
    fun `a timer ends at its end time, not after it`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, timerMode = Mode.OFF, timerUntilMillis = monday(13))
        assertEquals(Mode.OFF, at(settings, monday(13) - 1).mode)
        assertEquals(ModeNow(Mode.BLOCK, ModeSource.LEVER, null, null), at(settings, monday(13)))
    }

    @Test
    fun `no timer is set when its end is zero`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, timerMode = Mode.SILENCE, timerUntilMillis = 0)
        assertEquals(ModeSource.LEVER, at(settings, monday(12)).source)
    }

    // --- the schedule ---

    private fun weeknights(lever: Mode) = ScreeningSettings(
        mode = lever,
        // Block from ten at night to seven in the morning, Monday night to Friday morning.
        schedule = WeekSchedule.EMPTY
            .with(listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY), 22..23, Mode.BLOCK)
            .with(listOf(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY), 0..6, Mode.BLOCK),
        scheduleOn = true,
    )

    @Test
    fun `inside its hours the schedule decides, until the hour that differs`() {
        val now = at(weeknights(lever = Mode.OFF), monday(22, 30))
        // Tuesday 7:00 is nine hours after Monday 22:00.
        assertEquals(ModeNow(Mode.BLOCK, ModeSource.SCHEDULE, monday(22) + 9 * hour, Mode.OFF), now)
    }

    @Test
    fun `outside its hours the lever decides, until the schedule's next hour`() {
        val now = at(weeknights(lever = Mode.SILENCE), monday(12))
        assertEquals(ModeNow(Mode.SILENCE, ModeSource.LEVER, monday(22), Mode.BLOCK), now)
    }

    @Test
    fun `an hour that asks for what the lever already says is not a change`() {
        // The lever is at Block all day, so the night's Block is no change; nothing is due.
        val now = at(weeknights(lever = Mode.BLOCK), monday(12))
        assertEquals(ModeNow(Mode.BLOCK, ModeSource.LEVER, null, null), now)
        assertEquals(ModeNow(Mode.BLOCK, ModeSource.SCHEDULE, null, null), at(weeknights(lever = Mode.BLOCK), monday(23)))
    }

    @Test
    fun `a schedule that is switched off or empty is as if it were not there`() {
        val off = weeknights(lever = Mode.OFF).copy(scheduleOn = false)
        assertEquals(ModeNow(Mode.OFF, ModeSource.LEVER, null, null), at(off, monday(23)))
        val empty = ScreeningSettings(mode = Mode.SILENCE, schedule = WeekSchedule.EMPTY, scheduleOn = true)
        assertEquals(ModeNow(Mode.SILENCE, ModeSource.LEVER, null, null), at(empty, monday(23)))
    }

    @Test
    fun `the week wraps from Sunday night into Monday morning`() {
        val settings = ScreeningSettings(
            mode = Mode.OFF,
            schedule = WeekSchedule.EMPTY.with(listOf(DayOfWeek.SUNDAY), 23..23, Mode.BLOCK).with(listOf(DayOfWeek.MONDAY), 0..0, Mode.BLOCK),
            scheduleOn = true,
        )
        // Sunday 4 October 2026, 23:30: the schedule's Block runs through midnight to Monday 1:00.
        val sundayNight = monday(0) - 30 * minute
        assertEquals(ModeNow(Mode.BLOCK, ModeSource.SCHEDULE, monday(1), Mode.OFF), at(settings, sundayNight))
    }

    @Test
    fun `the hours are the phone's own, whatever its time zone`() {
        // The same instant is Monday 12:00 in India and Monday 6:30 in London (summer time is +1).
        val london = ZoneId.of("Europe/London")
        val settings = ScreeningSettings(
            mode = Mode.OFF,
            schedule = WeekSchedule.EMPTY.with(listOf(DayOfWeek.MONDAY), 12..12, Mode.BLOCK),
            scheduleOn = true,
        )
        assertEquals(Mode.BLOCK, at(settings, monday(12), india).mode)
        assertEquals(Mode.OFF, at(settings, monday(12), london).mode)
        // India's hours start on the half hour of the world's clock; the schedule follows India's.
        assertEquals(monday(13), at(settings, monday(12, 59), india).untilMillis)
    }

    @Test
    fun `when the clocks go back the repeated hour is one hour of the schedule`() {
        // Europe/London, Sunday 25 October 2026: 1:00 to 2:00 happens twice.
        val london = ZoneId.of("Europe/London")
        val settings = ScreeningSettings(
            mode = Mode.OFF,
            schedule = WeekSchedule.EMPTY.with(listOf(DayOfWeek.SUNDAY), 1..1, Mode.BLOCK),
            scheduleOn = true,
        )
        val firstOneOClock = ZonedDateTime.of(2026, 10, 25, 1, 0, 0, 0, london).withEarlierOffsetAtOverlap().toInstant().toEpochMilli()
        val now = at(settings, firstOneOClock, london)
        // Two real hours pass before the clock reads 2:00.
        assertEquals(ModeNow(Mode.BLOCK, ModeSource.SCHEDULE, firstOneOClock + 2 * hour, Mode.OFF), now)
        assertEquals(Mode.BLOCK, at(settings, firstOneOClock + hour + 30 * minute, london).mode)
    }

    // --- a timer and the schedule together ---

    @Test
    fun `a timer outranks the schedule, and the schedule is what follows it`() {
        val paused = weeknights(lever = Mode.OFF).copy(timerMode = Mode.OFF, timerUntilMillis = monday(23))
        assertEquals(ModeNow(Mode.OFF, ModeSource.TIMER, monday(23), Mode.BLOCK), at(paused, monday(22, 30)))
        assertEquals(ModeSource.SCHEDULE, at(paused, monday(23)).source)
    }

    @Test
    fun `a timer that outlasts the schedule's hours is followed by the lever`() {
        val settings = weeknights(lever = Mode.SILENCE).copy(timerMode = Mode.OFF, timerUntilMillis = monday(22) + 10 * hour)
        assertEquals(Mode.SILENCE, at(settings, monday(23)).next)
    }
}
