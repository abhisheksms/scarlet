package com.cyanharborstudios.callblock.core.time

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

class DayRelationTest {

    private val zone = ZoneId.of("Asia/Kolkata")
    private val timeText = TimeText(is24Hour = true, locale = Locale.UK, zone = zone)

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        LocalDateTime.of(year, month, day, hour, minute).atZone(zone).toInstant().toEpochMilli()

    private val now = at(2026, 10, 2, 19, 30)

    @Test
    fun `a moment later today is today`() {
        assertEquals(DayRelation.TODAY, timeText.dayRelation(at(2026, 10, 2, 20, 30), now))
    }

    @Test
    fun `a moment after midnight is tomorrow`() {
        assertEquals(DayRelation.TOMORROW, timeText.dayRelation(at(2026, 10, 3, 0, 5), now))
    }

    @Test
    fun `a moment in two days is some other day`() {
        assertEquals(DayRelation.OTHER, timeText.dayRelation(at(2026, 10, 4, 19, 30), now))
    }

    @Test
    fun `a moment earlier today is still today`() {
        assertEquals(DayRelation.TODAY, timeText.dayRelation(at(2026, 10, 2, 9, 0), now))
    }

    @Test
    fun `a twelve-hour time is one word`() {
        val twelveHour = TimeText(is24Hour = false, locale = Locale.US, zone = zone)
        val text = twelveHour.time(at(2026, 10, 2, 19, 4))
        assertEquals("7:04\u00A0PM", text)
        assertEquals(false, text.contains(' '))
    }

    @Test
    fun `the calendar day follows the zone, not UTC`() {
        // 23:30 in Kolkata on 2 October is 18:00 UTC the same day; 00:30 on 3 October is 19:00 UTC on the 2nd.
        assertEquals(DayRelation.TOMORROW, timeText.dayRelation(at(2026, 10, 3, 0, 30), now))
    }
}
