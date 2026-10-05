package com.cyanharborstudios.callblock.core.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The user's list of number rules, and its one line of text in the settings file. */
class NumberRulesTest {

    private val blockRange = NumberRule("+91804567", Action.BLOCK)
    private val blockCountry = NumberRule("+92", Action.BLOCK)
    private val ringOffice = NumberRule("+914428", Action.ALLOW)

    @Test
    fun `a new rule goes first`() {
        assertEquals(listOf(ringOffice, blockRange), NumberRules.with(listOf(blockRange), ringOffice))
    }

    @Test
    fun `a rule for a start that already has one replaces it`() {
        val changed = NumberRule("+92", Action.ALLOW)
        assertEquals(listOf(changed, blockRange), NumberRules.with(listOf(blockRange, blockCountry), changed))
    }

    @Test
    fun `a rule is removed by its start`() {
        assertEquals(listOf(blockRange), NumberRules.without(listOf(blockRange, blockCountry), "+92"))
        assertEquals(listOf(blockRange), NumberRules.without(listOf(blockRange), "+93"))
    }

    @Test
    fun `the list survives being written to the settings file and read back, in order`() {
        val rules = listOf(ringOffice, blockCountry, blockRange)
        assertEquals("+914428=ALLOW;+92=BLOCK;+91804567=BLOCK", NumberRules.encode(rules))
        assertEquals(rules, NumberRules.decode(NumberRules.encode(rules)))
        assertEquals(emptyList<NumberRule>(), NumberRules.decode(NumberRules.encode(emptyList())))
    }

    @Test
    fun `text that is not a rule is left out when the file is read`() {
        assertEquals(emptyList<NumberRule>(), NumberRules.decode(null))
        assertEquals(emptyList<NumberRule>(), NumberRules.decode("nonsense"))
        // An action the list does not have, a start without its plus, letters in a start, an empty start.
        assertEquals(listOf(blockCountry), NumberRules.decode("+91=SILENCE;91804567=BLOCK;+9a=BLOCK;+=BLOCK;=BLOCK;+92=BLOCK"))
        // The same start twice: the first one stands.
        assertEquals(listOf(blockCountry), NumberRules.decode("+92=BLOCK;+92=ALLOW"))
    }

    @Test
    fun `a start is a plus and digits`() {
        assertTrue(NumberRules.isStart("+92"))
        assertTrue(NumberRules.isStart("+91804567"))
        for (notOne in listOf("", "+", "92", "+9 2", "+92x", "++92")) assertFalse(notOne, NumberRules.isStart(notOne))
    }
}
