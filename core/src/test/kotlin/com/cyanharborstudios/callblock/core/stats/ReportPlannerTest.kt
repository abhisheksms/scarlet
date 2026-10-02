package com.cyanharborstudios.callblock.core.stats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ReportPlannerTest {

    private val friday = LocalDate.of(2026, 10, 2)

    @Test
    fun `off never has a report due`() {
        assertNull(ReportPlanner.dueReport(ReportFrequency.OFF, friday, lastReportedKey = null))
    }

    @Test
    fun `the weekly report covers the last finished Monday to Sunday`() {
        val period = ReportPlanner.lastFinishedPeriod(ReportFrequency.WEEKLY, friday)!!
        assertEquals(LocalDate.of(2026, 9, 21), period.firstDay)
        assertEquals(LocalDate.of(2026, 9, 27), period.lastDay)
    }

    @Test
    fun `on a Monday the week that ended yesterday is the one reported`() {
        val monday = LocalDate.of(2026, 10, 5)
        val period = ReportPlanner.lastFinishedPeriod(ReportFrequency.WEEKLY, monday)!!
        assertEquals(LocalDate.of(2026, 9, 28), period.firstDay)
        assertEquals(LocalDate.of(2026, 10, 4), period.lastDay)
    }

    @Test
    fun `the monthly report covers the last finished calendar month`() {
        val period = ReportPlanner.lastFinishedPeriod(ReportFrequency.MONTHLY, friday)!!
        assertEquals(LocalDate.of(2026, 9, 1), period.firstDay)
        assertEquals(LocalDate.of(2026, 9, 30), period.lastDay)

        val january = ReportPlanner.lastFinishedPeriod(ReportFrequency.MONTHLY, LocalDate.of(2027, 1, 15))!!
        assertEquals(LocalDate.of(2026, 12, 1), january.firstDay)
        assertEquals(LocalDate.of(2026, 12, 31), january.lastDay)
    }

    @Test
    fun `a period is reported once`() {
        val first = ReportPlanner.dueReport(ReportFrequency.WEEKLY, friday, lastReportedKey = null)!!
        assertNull(ReportPlanner.dueReport(ReportFrequency.WEEKLY, friday, lastReportedKey = first.key))
        assertNull(ReportPlanner.dueReport(ReportFrequency.WEEKLY, friday.plusDays(2), lastReportedKey = first.key))
    }

    @Test
    fun `the next period becomes due when it ends`() {
        val first = ReportPlanner.dueReport(ReportFrequency.WEEKLY, friday, lastReportedKey = null)!!
        val nextMonday = LocalDate.of(2026, 10, 5)
        val second = ReportPlanner.dueReport(ReportFrequency.WEEKLY, nextMonday, lastReportedKey = first.key)!!
        assertNotEquals(first.key, second.key)
        assertEquals(LocalDate.of(2026, 9, 28), second.firstDay)
    }

    @Test
    fun `weekly and monthly periods never share a key`() {
        val firstOfMonth = LocalDate.of(2026, 6, 1) // a Monday: both periods start on the same date
        val week = ReportPlanner.lastFinishedPeriod(ReportFrequency.WEEKLY, firstOfMonth.plusDays(7))!!
        val month = ReportPlanner.lastFinishedPeriod(ReportFrequency.MONTHLY, firstOfMonth.plusMonths(1))!!
        assertEquals(week.firstDay, month.firstDay)
        assertNotEquals(week.key, month.key)
    }
}
