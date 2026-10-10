package com.cyanharborstudios.callblock.core.frequent

import com.cyanharborstudios.callblock.core.rules.Action
import com.cyanharborstudios.callblock.core.rules.NumberRule
import com.cyanharborstudios.callblock.core.rules.ScreeningSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

/** Which callers the app lists as frequent, from its own record, and which it leaves alone. */
class FrequentCallersTest {

    private val zone = ZoneId.of("Asia/Kolkata")

    /** Day 0 is Thursday 1 October 2026; the record is read on day 10 at noon. */
    private fun at(day: Int, hour: Int = 10, minute: Int = 0): Long =
        ZonedDateTime.of(2026, 10, 1, 0, 0, 0, 0, zone).plusDays(day.toLong()).withHour(hour).withMinute(minute).toInstant().toEpochMilli()

    private val now = at(10, 12)

    private fun call(key: String, day: Int, hour: Int = 10) = SeenCall(key, at(day, hour))

    /** Three numbers of one block of a thousand, two calls each, over three days. */
    private val callCentre = listOf(
        call("+918046512001", 0), call("+918046512002", 0),
        call("+918046512003", 1), call("+918046512001", 1),
        call("+918046512002", 2), call("+918046512003", 2),
    )

    private fun classes(calls: List<SeenCall>, settings: ScreeningSettings = ScreeningSettings(), allowed: Set<String> = emptySet(), dialled: Set<String> = emptySet()) =
        FrequentCallers.classes(calls, now, zone, settings, allowed, dialled)

    @Test
    fun `numbers that differ only in their last three digits, calling on three days, are one class named by what they share`() {
        val found = classes(callCentre)
        assertEquals(1, found.size)
        val it = found.single()
        assertEquals("+918046512", it.start)
        assertEquals(3, it.numbers)
        assertEquals(6, it.calls)
        assertEquals(3, it.days)
        assertEquals(at(2), it.lastAtMillis)
        assertTrue(!it.single)
    }

    @Test
    fun `two days, five calls or two numbers are not enough`() {
        val twoDays = callCentre.map { if (it.atMillis == at(2)) it.copy(atMillis = at(1, 15)) else it }
        assertEquals(emptyList<CallerClass>(), classes(twoDays))
        assertEquals(emptyList<CallerClass>(), classes(callCentre.dropLast(1)))
        // Two numbers are no class. The one that then calls four times on three days stands on its own.
        val twoNumbers = callCentre.map { if (it.numberKey == "+918046512003") it.copy(numberKey = "+918046512001") else it }
        assertEquals(listOf("+918046512001"), classes(twoNumbers).map { it.start })
        assertTrue(classes(twoNumbers).single().single)
    }

    @Test
    fun `one number that calls on three days is listed on its own, but not when it is inside a class`() {
        val pest = listOf(call("+919876543210", 3), call("+919876543210", 4, 18), call("+919876543210", 6))
        assertEquals(listOf(CallerClass("+919876543210", numbers = 1, calls = 3, days = 3, lastAtMillis = at(6))), classes(pest))
        assertTrue(classes(pest).single().single)
        // Three calls on two days is a busy afternoon, not a frequent caller.
        assertEquals(emptyList<CallerClass>(), classes(pest.map { if (it.atMillis == at(6)) it.copy(atMillis = at(4, 20)) else it }))
        // Inside the call centre's block the same number is part of the class, not a second row.
        val inside = callCentre + listOf(call("+918046512001", 3), call("+918046512001", 4))
        assertEquals(listOf("+918046512"), classes(inside).map { it.start })
    }

    @Test
    fun `what the user has dealt with is left out, and so are India's reserved series`() {
        assertEquals(1, classes(callCentre).size)
        // A number they called, or allowed, takes its calls out of the count.
        assertEquals(emptyList<CallerClass>(), classes(callCentre, dialled = setOf("+918046512001")))
        assertEquals(emptyList<CallerClass>(), classes(callCentre, allowed = setOf("+918046512002")))
        // A number rule that covers the block, either way, means they have decided already.
        for (action in Action.entries) {
            assertEquals(emptyList<CallerClass>(), classes(callCentre, ScreeningSettings(numberRules = listOf(NumberRule("+918046", action)))))
        }
        // A class already blocked is not offered again.
        assertEquals(emptyList<CallerClass>(), classes(callCentre, ScreeningSettings(frequentCallers = listOf("+918046512"))))
        // The 140 series has its own switch, and the 160 series always rings.
        val promotional = callCentre.map { it.copy(numberKey = it.numberKey.replaceRange(3, 6, "140")) }
        val service = callCentre.map { it.copy(numberKey = it.numberKey.replaceRange(3, 6, "160")) }
        assertEquals(emptyList<CallerClass>(), classes(promotional))
        assertEquals(emptyList<CallerClass>(), classes(service))
    }

    @Test
    fun `calls older than the window do not count, and neither do calls from the future`() {
        val old = callCentre.map { it.copy(atMillis = it.atMillis - 61L * 24 * 60 * 60_000) }
        assertEquals(emptyList<CallerClass>(), classes(old))
        val future = callCentre.map { it.copy(atMillis = it.atMillis + 20L * 24 * 60 * 60_000) }
        assertEquals(emptyList<CallerClass>(), classes(future))
    }

    @Test
    fun `a wider class is named only when its numbers spread beyond one block, and then it absorbs the blocks`() {
        // Three numbers in three different blocks of a thousand, all in one block of ten thousand.
        val spread = listOf(
            call("+918046511001", 0), call("+918046512002", 0),
            call("+918046513003", 1), call("+918046511001", 1),
            call("+918046512002", 2), call("+918046513003", 2),
        )
        assertEquals(listOf("+91804651"), classes(spread).map { it.start })
        assertEquals(3, classes(spread).single().numbers)
        // Nine numbers in three full blocks: three narrow classes, and no wide one over them.
        val threeBlocks = (1..3).flatMap { block ->
            (1..3).flatMap { n -> listOf(call("+91804651${block}00$n", 0), call("+91804651${block}00$n", 1), call("+91804651${block}00$n", 2)) }
        }
        assertEquals(setOf("+918046511", "+918046512", "+918046513"), classes(threeBlocks).map { it.start }.toSet())
        // One full block, and three numbers of the wider block that each called once today: the
        // block stays a thousand numbers wide. The three on their own are no class.
        val onceToday = listOf(call("+918046513001", 10), call("+918046514002", 10), call("+918046515003", 10))
        assertEquals(listOf("+918046512"), classes(callCentre + onceToday).map { it.start })
        // The same three calling on three days: now the callers spread beyond the block, and the wide class takes it all.
        val onThreeDays = onceToday.flatMap { listOf(it, it.copy(atMillis = at(8)), it.copy(atMillis = at(9))) }
        val widened = classes(callCentre + onThreeDays).single()
        assertEquals("+91804651", widened.start)
        assertEquals(6, widened.numbers)
        assertEquals(15, widened.calls)
    }

    @Test
    fun `the busiest come first, and the list has an end`() {
        val quiet = listOf(call("+919876543210", 3), call("+919876543210", 4), call("+919876543210", 6))
        val found = classes(callCentre + quiet)
        assertEquals(listOf("+918046512", "+919876543210"), found.map { it.start })
        // Thirty numbers that differ well before their last digits: thirty single callers, of which twenty are listed.
        val many = (1..30).flatMap { n -> (0..2).map { day -> call("+9190${"%03d".format(n)}00000", day) } }
        assertEquals(20, FrequentCallers.classes(many, now, zone, ScreeningSettings(), limits = FrequentLimits(most = 20)).size)
        assertTrue(FrequentCallers.classes(many, now, zone, ScreeningSettings()).all { it.single })
    }

    @Test
    fun `the figures for a blocked start are counted however few its calls are`() {
        val figures = FrequentCallers.figures(callCentre.take(2), "+918046512", now, zone)
        assertEquals(CallerClass("+918046512", numbers = 2, calls = 2, days = 1, lastAtMillis = at(0)), figures)
        assertEquals(CallerClass("+91999", numbers = 0, calls = 0, days = 0, lastAtMillis = 0), FrequentCallers.figures(callCentre, "+91999", now, zone))
    }

    @Test
    fun `a short code that keeps calling is listed as one number, and never as a class`() {
        val code = listOf(call("121", 0), call("121", 1), call("121", 2), call("122", 0), call("122", 1), call("122", 2))
        assertEquals(setOf("121", "122"), classes(code).map { it.start }.toSet())
        assertTrue(classes(code).all { it.single })
    }

    @Test
    fun `a class is never as wide as a country`() {
        // Four-digit keys would leave a start of one character; they can only be single numbers.
        val tiny = listOf(call("+1201", 0), call("+1202", 1), call("+1203", 2), call("+1201", 1), call("+1202", 2), call("+1203", 0))
        assertTrue(classes(tiny).all { it.single })
    }
}
