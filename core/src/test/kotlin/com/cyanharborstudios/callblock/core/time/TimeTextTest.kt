package com.cyanharborstudios.callblock.core.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

class TimeTextTest {

    private val kolkata = ZoneId.of("Asia/Kolkata")
    private val quarterPastFive = LocalDateTime.of(2026, 10, 2, 17, 15).atZone(kolkata).toInstant().toEpochMilli()
    private val fiveToMidnight = LocalDateTime.of(2026, 10, 2, 23, 55).atZone(kolkata).toInstant().toEpochMilli()
    private val fivePastMidnight = LocalDateTime.of(2026, 10, 2, 0, 5).atZone(kolkata).toInstant().toEpochMilli()

    private val twentyFourHour = TimeText(is24Hour = true, locale = Locale.US, zone = kolkata)
    private val twelveHour = TimeText(is24Hour = false, locale = Locale.US, zone = kolkata)

    @Test
    fun `the 24-hour setting shows hours 0 to 23 and no am or pm`() {
        assertEquals("17:15", twentyFourHour.time(quarterPastFive))
        assertEquals("23:55", twentyFourHour.time(fiveToMidnight))
        assertEquals("00:05", twentyFourHour.time(fivePastMidnight))
    }

    @Test
    fun `the 12-hour setting shows hours 1 to 12 with am or pm`() {
        assertEquals("5:15 PM", twelveHour.time(quarterPastFive))
        assertEquals("11:55 PM", twelveHour.time(fiveToMidnight))
        assertEquals("12:05 AM", twelveHour.time(fivePastMidnight))
    }

    @Test
    fun `hour labels follow the same setting`() {
        assertEquals("20:00", twentyFourHour.hour(20))
        assertEquals("00:00", twentyFourHour.hour(0))
        assertEquals("8 PM", twelveHour.hour(20))
        assertEquals("12 AM", twelveHour.hour(0))
        assertEquals("12 PM", twelveHour.hour(12))
    }

    @Test
    fun `date and time together follow the setting too`() {
        assertTrue(twentyFourHour.dateAndTime(quarterPastFive).endsWith("17:15"))
        assertTrue(twelveHour.dateAndTime(quarterPastFive).endsWith("5:15 PM"))
        assertFalse(twelveHour.dateAndTime(quarterPastFive).contains("17:15"))
    }

    @Test
    fun `the time is read in the phone's time zone`() {
        val inLondon = TimeText(is24Hour = true, locale = Locale.UK, zone = ZoneId.of("Europe/London"))
        assertEquals("12:45", inLondon.time(quarterPastFive)) // 17:15 IST is 12:45 BST
        assertEquals(LocalDate.of(2026, 10, 1), inLondon.dateOf(fivePastMidnight)) // still the 1st in London
    }

    @Test
    fun `dates are written the way the locale writes them`() {
        val date = LocalDate.of(2026, 10, 2)
        assertEquals("Oct 2, 2026", TimeText(true, Locale.US, kolkata).date(date))
        assertEquals("2 Oct 2026", TimeText(true, Locale.UK, kolkata).date(date))
        assertEquals("Fri", twentyFourHour.weekday(date))
    }
}
