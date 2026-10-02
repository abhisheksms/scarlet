package com.cyanharborstudios.callblock.core.stats

import com.cyanharborstudios.callblock.core.rules.Action
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class StatisticsTest {

    private val kolkata = ZoneId.of("Asia/Kolkata")
    private val today = LocalDate.of(2026, 10, 2) // a Friday
    private val now = millis(today, 18, 0)

    private fun millis(date: LocalDate, hour: Int, minute: Int = 0, zone: ZoneId = kolkata): Long =
        LocalDateTime.of(date, java.time.LocalTime.of(hour, minute)).atZone(zone).toInstant().toEpochMilli()

    private fun blocked(number: String, daysAgo: Long, hour: Int = 12, minute: Int = 0) =
        HandledCall(number, number, millis(today.minusDays(daysAgo), hour, minute), Action.BLOCK)

    private fun silenced(number: String, daysAgo: Long, hour: Int = 12) =
        HandledCall(number, number, millis(today.minusDays(daysAgo), hour), Action.SILENCE)

    private fun summarize(calls: List<HandledCall>, periodDays: Int = 30) =
        Statistics.summarize(calls, now, kolkata, periodDays)

    @Test
    fun `no calls gives zeros and nothing to compare`() {
        val summary = summarize(emptyList())
        assertEquals(Counts(0, 0), summary.allTime)
        assertEquals(Counts(0, 0), summary.today)
        assertNull(summary.changeVsPreviousSevenDays)
        assertNull(summary.busiestHourLastSevenDays)
        assertEquals(List(7) { 0 }, summary.byWeekday)
        assertEquals(List(24) { 0 }, summary.byHour)
        assertEquals(emptyList<NumberCounts>(), summary.topNumbers)
        assertEquals(0L, summary.secondsSaved)
        assertEquals(7, summary.dailyLastSevenDays.size)
    }

    @Test
    fun `totals split blocked from silenced`() {
        val summary = summarize(listOf(blocked("+911", 0), blocked("+912", 40), silenced("+913", 100)))
        assertEquals(Counts(blocked = 2, silenced = 1), summary.allTime)
        assertEquals(3, summary.allTime.total)
    }

    @Test
    fun `today is the calendar day in the phone's time zone`() {
        val justAfterMidnight = HandledCall("+911", "+911", millis(today, 0, 1), Action.BLOCK)
        val justBeforeMidnight = HandledCall("+912", "+912", millis(today.minusDays(1), 23, 59), Action.BLOCK)
        val summary = summarize(listOf(justAfterMidnight, justBeforeMidnight))
        assertEquals(Counts(1, 0), summary.today)
    }

    @Test
    fun `the same moment falls on different days in different zones`() {
        // 01:00 on the 2nd in Kolkata is still the 1st in New York.
        val call = HandledCall("+911", "+911", millis(today, 1, 0), Action.BLOCK)
        val inKolkata = Statistics.summarize(listOf(call), now, kolkata, 30)
        val inNewYork = Statistics.summarize(listOf(call), now, ZoneId.of("America/New_York"), 30)
        assertEquals(today, inKolkata.dailyLastSevenDays.single { it.counts.total == 1 }.date)
        assertEquals(today.minusDays(1), inNewYork.dailyLastSevenDays.single { it.counts.total == 1 }.date)
    }

    @Test
    fun `the last seven days are today and the six days before it`() {
        val summary = summarize(listOf(blocked("+911", 0), silenced("+912", 6), blocked("+913", 7)))
        assertEquals(Counts(blocked = 1, silenced = 1), summary.lastSevenDays)
        assertEquals(today.minusDays(6), summary.dailyLastSevenDays.first().date)
        assertEquals(today, summary.dailyLastSevenDays.last().date)
        assertEquals(Counts(0, 1), summary.dailyLastSevenDays.first().counts)
        assertEquals(Counts(1, 0), summary.dailyLastSevenDays.last().counts)
    }

    @Test
    fun `change compares this week with the seven days before it`() {
        val thisWeek = List(3) { blocked("+911", it.toLong()) }
        val weekBefore = List(4) { blocked("+912", 7 + it.toLong()) }
        assertEquals(-25, summarize(thisWeek + weekBefore).changeVsPreviousSevenDays)
        assertEquals(100, summarize(thisWeek + thisWeek + List(3) { blocked("+912", 13) }).changeVsPreviousSevenDays)
    }

    @Test
    fun `change is absent when the earlier stretch had no calls`() {
        assertNull(summarize(listOf(blocked("+911", 1))).changeVsPreviousSevenDays)
        assertNull(summarize(listOf(blocked("+911", 1))).changeVsPreviousThirtyDays)
    }

    @Test
    fun `thirty-day change compares with the thirty days before`() {
        val recent = List(2) { blocked("+911", 29) }
        val earlier = List(4) { blocked("+912", 30 + it.toLong()) } + blocked("+913", 60)
        val summary = summarize(recent + earlier)
        assertEquals(2, summary.lastThirtyDays.total)
        assertEquals(-50, summary.changeVsPreviousThirtyDays) // 2 now against 4 before; day 60 is outside
    }

    @Test
    fun `the busiest hour is the one with the most calls this week, the earlier one on a tie`() {
        val calls = listOf(blocked("+911", 1, 20), blocked("+912", 2, 20), blocked("+913", 3, 9), blocked("+914", 4, 9))
        assertEquals(9, summarize(calls).busiestHourLastSevenDays)
        assertEquals(20, summarize(calls + blocked("+915", 5, 20, 59)).busiestHourLastSevenDays)
    }

    @Test
    fun `weekday and hour charts cover only the chosen period`() {
        val friday = blocked("+911", 0, 17)          // today, a Friday
        val monday = blocked("+912", 4, 9)           // Monday of this week
        val longAgo = blocked("+913", 45, 9)
        val week = summarize(listOf(friday, monday, longAgo), periodDays = 7)
        assertEquals(listOf(1, 0, 0, 0, 1, 0, 0), week.byWeekday) // Monday first
        assertEquals(1, week.byHour[17])
        assertEquals(1, week.byHour[9])

        val ninetyDays = summarize(listOf(friday, monday, longAgo), periodDays = 90)
        assertEquals(2, ninetyDays.byHour[9])
        assertEquals(3, ninetyDays.byWeekday.sum())
    }

    @Test
    fun `top numbers are ranked by calls, then by who called most recently`() {
        val calls = listOf(
            blocked("+911", 1), blocked("+911", 2), blocked("+911", 3),
            blocked("+912", 5), silenced("+912", 6),
            blocked("+913", 1), blocked("+913", 4),
        )
        val top = summarize(calls).topNumbers
        assertEquals(listOf("+911", "+913", "+912"), top.map { it.numberKey })
        assertEquals(Counts(blocked = 1, silenced = 1), top.last().counts)
    }

    @Test
    fun `top numbers stop at ten and skip calls that had no number`() {
        val calls = (1..12).map { blocked("+91$it", 1) } + blocked("", 1) + blocked("", 1)
        val top = summarize(calls).topNumbers
        assertEquals(Statistics.TOP_NUMBERS_SHOWN, top.size)
        assertEquals(false, top.any { it.numberKey.isEmpty() })
    }

    @Test
    fun `time saved counts every handled call`() {
        val summary = summarize(List(156) { blocked("+911", 100) })
        assertEquals(156 * Statistics.SECONDS_SAVED_PER_CALL, summary.secondsSaved)
    }

    @Test
    fun `details for a number give its counts and its first and last times`() {
        val calls = listOf(blocked("+911", 9, 10, 39), silenced("+911", 4, 13), blocked("+911", 2, 8), blocked("+912", 1))
        val details = Statistics.detailsFor("+911", calls)!!
        assertEquals(Counts(blocked = 2, silenced = 1), details.counts)
        assertEquals(millis(today.minusDays(9), 10, 39), details.firstHandledAtMillis)
        assertEquals(millis(today.minusDays(2), 8), details.lastHandledAtMillis)
        assertNull(Statistics.detailsFor("+919", calls))
    }

    @Test
    fun `a report period counts its first and last day and nothing outside them`() {
        val calls = listOf(
            blocked("+911", 12, 0, 0),   // first day, at midnight
            silenced("+912", 9),
            blocked("+913", 6, 23, 59),  // last day, a minute before midnight
            blocked("+914", 13, 23, 59), // the day before the period
            blocked("+915", 5, 0, 0),    // the day after the period
        )
        val counts = Statistics.countBetween(calls, today.minusDays(12), today.minusDays(6), kolkata)
        assertEquals(Counts(blocked = 2, silenced = 1), counts)
    }
}
