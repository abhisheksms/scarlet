package com.cyanharborstudios.callblock.core.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

/** The week of hours: setting them, reading them, and writing them to the settings file. */
class WeekScheduleTest {

    @Test
    fun `an empty schedule asks for nothing at any hour`() {
        assertTrue(WeekSchedule.EMPTY.isEmpty)
        for (day in DayOfWeek.entries) {
            for (hour in 0..23) assertNull(WeekSchedule.EMPTY.modeAt(day, hour))
        }
    }

    @Test
    fun `hours are set for the days given and no others`() {
        val weeknights = WeekSchedule.EMPTY.with(listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), 22..23, Mode.BLOCK)
        assertFalse(weeknights.isEmpty)
        assertEquals(Mode.BLOCK, weeknights.modeAt(DayOfWeek.MONDAY, 22))
        assertEquals(Mode.BLOCK, weeknights.modeAt(DayOfWeek.TUESDAY, 23))
        assertNull(weeknights.modeAt(DayOfWeek.MONDAY, 21))
        assertNull(weeknights.modeAt(DayOfWeek.WEDNESDAY, 22))
        assertNull(weeknights.modeAt(DayOfWeek.SUNDAY, 23))
    }

    @Test
    fun `setting an hour again replaces it, and null clears it`() {
        val blocked = WeekSchedule.EMPTY.with(listOf(DayOfWeek.FRIDAY), 9..17, Mode.BLOCK)
        val lunchSilenced = blocked.with(listOf(DayOfWeek.FRIDAY), 13..13, Mode.SILENCE)
        assertEquals(Mode.SILENCE, lunchSilenced.modeAt(DayOfWeek.FRIDAY, 13))
        assertEquals(Mode.BLOCK, lunchSilenced.modeAt(DayOfWeek.FRIDAY, 12))
        val cleared = lunchSilenced.with(listOf(DayOfWeek.FRIDAY), 0..23, null)
        assertTrue(cleared.isEmpty)
    }

    @Test
    fun `hours outside the day are ignored`() {
        val schedule = WeekSchedule.EMPTY.with(listOf(DayOfWeek.SUNDAY), -3..30, Mode.OFF)
        assertEquals(Mode.OFF, schedule.modeAt(DayOfWeek.SUNDAY, 0))
        assertEquals(Mode.OFF, schedule.modeAt(DayOfWeek.SUNDAY, 23))
        // Sunday is the last day: nothing spilled into another day.
        assertNull(schedule.modeAt(DayOfWeek.SATURDAY, 23))
        assertNull(schedule.modeAt(DayOfWeek.MONDAY, 0))
    }

    @Test
    fun `what is written to the settings file reads back the same`() {
        val schedule = WeekSchedule.EMPTY
            .with(listOf(DayOfWeek.MONDAY), 0..6, Mode.BLOCK)
            .with(listOf(DayOfWeek.WEDNESDAY), 12..12, Mode.SILENCE)
            .with(listOf(DayOfWeek.SUNDAY), 23..23, Mode.OFF)
        val text = schedule.encode()
        assertEquals(WeekSchedule.HOURS_IN_WEEK, text.length)
        assertEquals(schedule, WeekSchedule.decode(text))
    }

    @Test
    fun `the settings file holds Monday's first hour first and Sunday's last hour last`() {
        // The layout is stored on people's phones: changing it would scramble their schedules.
        val mondayMidnight = WeekSchedule.EMPTY.with(listOf(DayOfWeek.MONDAY), 0..0, Mode.BLOCK).encode()
        assertEquals('B', mondayMidnight.first())
        assertEquals(1, mondayMidnight.count { it == 'B' })
        val sundayLastHour = WeekSchedule.EMPTY.with(listOf(DayOfWeek.SUNDAY), 23..23, Mode.SILENCE).encode()
        assertEquals('S', sundayLastHour.last())
        val tuesdayNoon = WeekSchedule.EMPTY.with(listOf(DayOfWeek.TUESDAY), 12..12, Mode.OFF).encode()
        assertEquals(24 + 12, tuesdayNoon.indexOf('O'))
    }

    @Test
    fun `anything unreadable is an empty schedule`() {
        assertEquals(WeekSchedule.EMPTY, WeekSchedule.decode(null))
        assertEquals(WeekSchedule.EMPTY, WeekSchedule.decode(""))
        assertEquals(WeekSchedule.EMPTY, WeekSchedule.decode("B".repeat(167)))
        assertEquals(WeekSchedule.EMPTY, WeekSchedule.decode("B".repeat(167) + "x"))
    }
}
