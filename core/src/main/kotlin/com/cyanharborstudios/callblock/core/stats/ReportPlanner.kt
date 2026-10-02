package com.cyanharborstudios.callblock.core.stats

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class ReportFrequency { OFF, WEEKLY, MONTHLY }

/** A finished stretch of days that a report covers. [key] names it, so a report is sent once. */
data class ReportPeriod(val key: String, val firstDay: LocalDate, val lastDay: LocalDate)

/** Works out which summary report, if any, should go out. */
object ReportPlanner {

    /**
     * The most recent period that has fully ended as of [today]: last Monday-to-Sunday
     * week, or last calendar month. Null when reports are off.
     */
    fun lastFinishedPeriod(frequency: ReportFrequency, today: LocalDate): ReportPeriod? = when (frequency) {
        ReportFrequency.OFF -> null
        ReportFrequency.WEEKLY -> {
            val thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val firstDay = thisMonday.minusWeeks(1)
            ReportPeriod("week-of-$firstDay", firstDay, thisMonday.minusDays(1))
        }
        ReportFrequency.MONTHLY -> {
            val firstDay = today.withDayOfMonth(1).minusMonths(1)
            ReportPeriod("month-of-$firstDay", firstDay, today.withDayOfMonth(1).minusDays(1))
        }
    }

    /** The period to report now, or null when it has already been reported (or reports are off). */
    fun dueReport(frequency: ReportFrequency, today: LocalDate, lastReportedKey: String?): ReportPeriod? {
        val period = lastFinishedPeriod(frequency, today) ?: return null
        return if (period.key == lastReportedKey) null else period
    }
}
