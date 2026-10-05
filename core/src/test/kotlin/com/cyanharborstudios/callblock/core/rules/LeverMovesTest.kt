package com.cyanharborstudios.callblock.core.rules

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

/** What moving the lever changes: its own stop, or, inside the schedule's hours, a hold until they end. */
class LeverMovesTest {

    private val zone = ZoneId.of("Asia/Kolkata")
    private val hour = 60 * 60_000L

    private fun monday(hourOfDay: Int): Long = ZonedDateTime.of(2026, 10, 5, hourOfDay, 0, 0, 0, zone).toInstant().toEpochMilli()

    private val mondayNight = WeekSchedule.EMPTY.with(listOf(DayOfWeek.MONDAY), 22..23, Mode.BLOCK)

    @Test
    fun `with no schedule a move sets the lever's own stop`() {
        val moved = LeverMoves.move(ScreeningSettings(mode = Mode.OFF), Mode.BLOCK, monday(12), zone)
        assertEquals(ScreeningSettings(mode = Mode.BLOCK), moved)
    }

    @Test
    fun `a move ends a running timer`() {
        val paused = ScreeningSettings(mode = Mode.BLOCK, timerMode = Mode.OFF, timerUntilMillis = monday(13))
        assertEquals(ScreeningSettings(mode = Mode.SILENCE), LeverMoves.move(paused, Mode.SILENCE, monday(12), zone))
        // Moving to where the lever already stands still ends the pause.
        assertEquals(ScreeningSettings(mode = Mode.BLOCK), LeverMoves.move(paused, Mode.BLOCK, monday(12), zone))
    }

    @Test
    fun `outside the schedule's hours a move sets the lever's own stop and leaves the schedule alone`() {
        val settings = ScreeningSettings(mode = Mode.OFF, schedule = mondayNight, scheduleOn = true)
        val moved = LeverMoves.move(settings, Mode.SILENCE, monday(12), zone)
        assertEquals(settings.copy(mode = Mode.SILENCE), moved)
    }

    @Test
    fun `inside the schedule's hours a move holds until they end and the lever's own stop is kept`() {
        val settings = ScreeningSettings(mode = Mode.SILENCE, schedule = mondayNight, scheduleOn = true)
        val moved = LeverMoves.move(settings, Mode.OFF, monday(23), zone)
        // Tuesday 0:00 is when the schedule's Block ends.
        assertEquals(settings.copy(timerMode = Mode.OFF, timerUntilMillis = monday(22) + 2 * hour), moved)
        // In effect: Off until midnight, then the lever's own Silence.
        assertEquals(ModeNow(Mode.OFF, ModeSource.TIMER, monday(22) + 2 * hour, Mode.SILENCE), ModeClock.at(moved, monday(23), zone))
    }

    @Test
    fun `moving back to what the schedule asks for goes back to the schedule`() {
        val held = ScreeningSettings(mode = Mode.SILENCE, schedule = mondayNight, scheduleOn = true, timerMode = Mode.OFF, timerUntilMillis = monday(22) + 2 * hour)
        val moved = LeverMoves.move(held, Mode.BLOCK, monday(23), zone)
        assertEquals(held.copy(timerMode = Mode.OFF, timerUntilMillis = 0), moved)
        assertEquals(ModeSource.SCHEDULE, ModeClock.at(moved, monday(23), zone).source)
    }

    @Test
    fun `against a schedule that asks for the same mode all week a move holds for a day`() {
        val allWeek = WeekSchedule.EMPTY.with(DayOfWeek.entries, 0..23, Mode.BLOCK)
        val settings = ScreeningSettings(mode = Mode.OFF, schedule = allWeek, scheduleOn = true)
        val moved = LeverMoves.move(settings, Mode.OFF, monday(12), zone)
        assertEquals(monday(12) + 24 * hour, moved.timerUntilMillis)
        assertEquals(Mode.OFF, moved.timerMode)
    }
}
