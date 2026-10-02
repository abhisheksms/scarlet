package com.cyanharborstudios.callblock.core.stats

import com.cyanharborstudios.callblock.core.rules.Action
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Blocked and silenced counts for one thing (a day, a number, a period). */
data class Counts(val blocked: Int, val silenced: Int) {
    val total: Int get() = blocked + silenced
}

data class DayCounts(val date: LocalDate, val counts: Counts)

data class NumberCounts(val numberKey: String, val numberRaw: String, val counts: Counts)

data class NumberDetails(
    val numberKey: String,
    val numberRaw: String,
    val counts: Counts,
    val firstHandledAtMillis: Long,
    val lastHandledAtMillis: Long,
)

/** Everything the statistics screen and the home summary show. */
data class StatsSummary(
    val allTime: Counts,
    val today: Counts,
    val lastSevenDays: Counts,
    /** Change against the seven days before, in percent. Null when those days had no calls. */
    val changeVsPreviousSevenDays: Int?,
    /** The hour of day (0 to 23) with the most calls in the last seven days. Null with no calls. */
    val busiestHourLastSevenDays: Int?,
    val lastThirtyDays: Counts,
    val changeVsPreviousThirtyDays: Int?,
    /** Seven entries, oldest first, ending today. */
    val dailyLastSevenDays: List<DayCounts>,
    val periodDays: Int,
    /** Calls in the period by day of week. Index 0 is Monday, index 6 is Sunday. */
    val byWeekday: List<Int>,
    /** Calls in the period by hour of day. Index 0 is midnight to 1 am. */
    val byHour: List<Int>,
    /** The numbers handled most often in the period, most first. */
    val topNumbers: List<NumberCounts>,
    val milestone: MilestoneProgress,
    val secondsSaved: Long,
)

object Statistics {

    /** What one avoided call is counted as saving. An estimate, and shown as one. */
    const val SECONDS_SAVED_PER_CALL = 30L

    const val TOP_NUMBERS_SHOWN = 10

    /**
     * "The last N days" always means whole calendar days in [zone], ending with today.
     * [periodDays] governs the weekday chart, the hour chart and the top numbers.
     */
    fun summarize(calls: List<HandledCall>, nowMillis: Long, zone: ZoneId, periodDays: Int): StatsSummary {
        val today = dateOf(nowMillis, zone)
        val dated = calls.map { Dated(it, Instant.ofEpochMilli(it.atMillis).atZone(zone)) }

        fun inLastDays(days: Int, endingBefore: LocalDate = today.plusDays(1)): List<Dated> {
            val from = endingBefore.minusDays(days.toLong())
            return dated.filter { !it.date.isBefore(from) && it.date.isBefore(endingBefore) }
        }

        val lastSeven = inLastDays(7)
        val previousSeven = inLastDays(7, endingBefore = today.minusDays(6))
        val lastThirty = inLastDays(30)
        val previousThirty = inLastDays(30, endingBefore = today.minusDays(29))
        val inPeriod = inLastDays(periodDays)

        val daily = (6 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            DayCounts(date, count(lastSeven.filter { it.date == date }))
        }

        val byWeekday = MutableList(7) { 0 }
        val byHour = MutableList(24) { 0 }
        for (call in inPeriod) {
            byWeekday[call.at.dayOfWeek.value - 1]++
            byHour[call.at.hour]++
        }

        val hoursLastSeven = MutableList(24) { 0 }
        for (call in lastSeven) hoursLastSeven[call.at.hour]++

        val allTime = count(dated)
        return StatsSummary(
            allTime = allTime,
            today = count(dated.filter { it.date == today }),
            lastSevenDays = count(lastSeven),
            changeVsPreviousSevenDays = percentChange(lastSeven.size, previousSeven.size),
            busiestHourLastSevenDays = busiestIndex(hoursLastSeven),
            lastThirtyDays = count(lastThirty),
            changeVsPreviousThirtyDays = percentChange(lastThirty.size, previousThirty.size),
            dailyLastSevenDays = daily,
            periodDays = periodDays,
            byWeekday = byWeekday,
            byHour = byHour,
            topNumbers = topNumbers(inPeriod),
            milestone = Milestones.progress(allTime.total),
            secondsSaved = allTime.total * SECONDS_SAVED_PER_CALL,
        )
    }

    /** Totals and first and last times for one number, or null if it was never handled. */
    fun detailsFor(numberKey: String, calls: List<HandledCall>): NumberDetails? {
        val forNumber = calls.filter { it.numberKey == numberKey }
        if (forNumber.isEmpty()) return null
        val latest = forNumber.maxBy { it.atMillis }
        return NumberDetails(
            numberKey = numberKey,
            numberRaw = latest.numberRaw,
            counts = Counts(
                blocked = forNumber.count { it.action == Action.BLOCK },
                silenced = forNumber.count { it.action == Action.SILENCE },
            ),
            firstHandledAtMillis = forNumber.minOf { it.atMillis },
            lastHandledAtMillis = latest.atMillis,
        )
    }

    /** The index of the largest value, the earliest one on a tie. Null when all are zero. */
    fun busiestIndex(values: List<Int>): Int? {
        val max = values.maxOrNull() ?: return null
        return if (max == 0) null else values.indexOf(max)
    }

    /** Whole-percent change from [previous] to [current]. Null when there is nothing to compare with. */
    fun percentChange(current: Int, previous: Int): Int? {
        if (previous == 0) return null
        return Math.round((current - previous) * 100.0 / previous).toInt()
    }

    private fun topNumbers(calls: List<Dated>): List<NumberCounts> =
        calls.filter { it.call.numberKey.isNotEmpty() }
            .groupBy { it.call.numberKey }
            .map { (key, group) ->
                val latest = group.maxBy { it.call.atMillis }
                Ranked(NumberCounts(key, latest.call.numberRaw, count(group)), latest.call.atMillis)
            }
            // Most calls first; between equals, the one that called most recently.
            .sortedWith(compareByDescending<Ranked> { it.entry.counts.total }.thenByDescending { it.latestAtMillis })
            .take(TOP_NUMBERS_SHOWN)
            .map { it.entry }

    private fun count(calls: List<Dated>) = Counts(
        blocked = calls.count { it.call.action == Action.BLOCK },
        silenced = calls.count { it.call.action == Action.SILENCE },
    )

    private fun dateOf(millis: Long, zone: ZoneId): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    private class Dated(val call: HandledCall, val at: java.time.ZonedDateTime) {
        val date: LocalDate get() = at.toLocalDate()
    }

    private class Ranked(val entry: NumberCounts, val latestAtMillis: Long)
}
