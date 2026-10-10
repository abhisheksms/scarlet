package com.cyanharborstudios.callblock.core.frequent

import com.cyanharborstudios.callblock.core.rules.RuleBook
import com.cyanharborstudios.callblock.core.rules.ScreeningSettings
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** One call the app was asked about while it was on, allowed or not: the number's key and the moment. */
data class SeenCall(val numberKey: String, val atMillis: Long)

/**
 * Callers that keep coming. Either a set of numbers that differ only in their last digits,
 * where [start] is what they share and [numbers] is more than one, or one number, where
 * [start] is its whole key. [calls] and [days] count the window the figures were read over.
 */
data class CallerClass(val start: String, val numbers: Int, val calls: Int, val days: Int, val lastAtMillis: Long) {
    val single: Boolean get() = numbers == 1
}

/** What it takes to be listed. The defaults are the product's; a test may narrow them. */
data class FrequentLimits(
    val windowDays: Int = 60,
    /** A class: this many different numbers, this many calls, on this many different days. */
    val classNumbers: Int = 3,
    val classCalls: Int = 6,
    val classDays: Int = 3,
    /** One number on its own: this many calls, on this many different days. */
    val numberCalls: Int = 3,
    val numberDays: Int = 3,
    /** How many last digits the numbers of a class may differ in: the narrow class, then the wide one. */
    val narrow: Int = 3,
    val wide: Int = 4,
    val most: Int = 20,
)

/**
 * Finds the frequent callers in the app's own record of calls. Pure: the record, the
 * moment and the settings come in as arguments, and the answer is a list.
 *
 * A class is a set of numbers that are the same except for their last three digits (a
 * thousand numbers, the block a call centre is given), or their last four where the callers
 * spread wider than that. A number that calls day after day is listed on its own. What the
 * user has already dealt with is left out: a number they called or allowed, anything one of
 * their number rules covers, a class they have blocked already. India's two reserved
 * series are left out too: the 140 series has its own switch, and the 160 series always
 * rings (knowledge-base/docs/02-rules-engine.md).
 */
object FrequentCallers {

    fun classes(
        calls: List<SeenCall>,
        nowMillis: Long,
        zone: ZoneId,
        settings: ScreeningSettings,
        allowedKeys: Set<String> = emptySet(),
        dialledKeys: Set<String> = emptySet(),
        limits: FrequentLimits = FrequentLimits(),
    ): List<CallerClass> {
        val recent = inWindow(calls, nowMillis, limits).filter { !dealtWith(it.numberKey, settings, allowedKeys, dialledKeys) }
        val byKey = recent.groupBy { it.numberKey }

        val narrow = classesOfWidth(byKey, limits.narrow, zone, limits)
        // A wide class earns its place with the callers outside the narrow classes under it: those
        // alone must be a class, so three numbers that called once never widen a block of a
        // thousand to ten thousand. Its figures, once it is listed, count everything in it.
        val outsideNarrow = byKey.filterKeys { key -> narrow.none { key.startsWith(it.start) } }
        val wide = classesOfWidth(outsideNarrow, limits.wide, zone, limits).map { it.copy() }.map { own ->
            val all = byKey.filterKeys { it.startsWith(own.start) }
            val its = all.values.flatten()
            CallerClass(own.start, numbers = all.size, calls = its.size, days = days(its, zone), lastAtMillis = its.maxOf { it.atMillis })
        }
        val found = wide + narrow.filter { n -> wide.none { w -> n.start.startsWith(w.start) } }

        val singles = byKey.mapNotNull { (key, its) ->
            val days = days(its, zone)
            if (its.size >= limits.numberCalls && days >= limits.numberDays && found.none { key.startsWith(it.start) }) {
                CallerClass(key, numbers = 1, calls = its.size, days = days, lastAtMillis = its.maxOf { it.atMillis })
            } else {
                null
            }
        }
        return (found + singles).sortedWith(compareByDescending<CallerClass> { it.calls }.thenByDescending { it.lastAtMillis }).take(limits.most)
    }

    /** The figures for a start the user has blocked, however few its calls now are. */
    fun figures(calls: List<SeenCall>, start: String, nowMillis: Long, zone: ZoneId, limits: FrequentLimits = FrequentLimits()): CallerClass {
        val its = inWindow(calls, nowMillis, limits).filter { it.numberKey.startsWith(start) }
        return CallerClass(
            start = start,
            numbers = its.map { it.numberKey }.distinct().size,
            calls = its.size,
            days = days(its, zone),
            lastAtMillis = its.maxOfOrNull { it.atMillis } ?: 0L,
        )
    }

    /** One of India's two reserved series, which are never listed. */
    fun inReservedSeries(key: String): Boolean =
        key.startsWith("+${RuleBook.INDIA}${RuleBook.PROMOTIONAL_SERIES}") || key.startsWith("+${RuleBook.INDIA}${RuleBook.SERVICE_SERIES}")

    private fun inWindow(calls: List<SeenCall>, nowMillis: Long, limits: FrequentLimits): List<SeenCall> {
        val from = nowMillis - limits.windowDays * DAY_MILLIS
        return calls.filter { it.numberKey.isNotEmpty() && it.atMillis > from && it.atMillis <= nowMillis }
    }

    private fun dealtWith(key: String, settings: ScreeningSettings, allowedKeys: Set<String>, dialledKeys: Set<String>): Boolean =
        key in allowedKeys ||
            key in dialledKeys ||
            inReservedSeries(key) ||
            settings.numberRules.any { key.startsWith(it.start) } ||
            settings.frequentCallers.any { key.startsWith(it) }

    /** The classes whose numbers differ only in their last [width] digits. */
    private fun classesOfWidth(byKey: Map<String, List<SeenCall>>, width: Int, zone: ZoneId, limits: FrequentLimits): List<CallerClass> {
        // A start keeps "+", the country code and at least one digit more, so a class is never a whole country.
        val groups = byKey.keys.filter { it.startsWith("+") && it.length - width >= SHORTEST_START }.groupBy { it.dropLast(width) }
        return groups.mapNotNull { (start, keys) ->
            val its = keys.flatMap { byKey.getValue(it) }
            val days = days(its, zone)
            if (keys.size >= limits.classNumbers && its.size >= limits.classCalls && days >= limits.classDays) {
                CallerClass(start, numbers = keys.size, calls = its.size, days = days, lastAtMillis = its.maxOf { it.atMillis })
            } else {
                null
            }
        }
    }

    private fun days(calls: List<SeenCall>, zone: ZoneId): Int = calls.map { dayOf(it.atMillis, zone) }.distinct().size

    private fun dayOf(atMillis: Long, zone: ZoneId): LocalDate = Instant.ofEpochMilli(atMillis).atZone(zone).toLocalDate()

    private const val DAY_MILLIS = 24 * 60 * 60_000L
    private const val SHORTEST_START = 5
}
