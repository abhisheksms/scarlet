package com.cyanharborstudios.callblock.core.numbers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumbersTest {

    private val india = PhoneNumbers("IN")
    private val unitedStates = PhoneNumbers("us")

    @Test
    fun `one number written three ways has one key`() {
        val keys = listOf("+918012345678", "+91 80 1234 5678", "080 1234 5678").map { india.parse(it).key }
        assertEquals(listOf("+918012345678", "+918012345678", "+918012345678"), keys)
    }

    @Test
    fun `a number from the phone's own country is not international`() {
        assertFalse(india.parse("+918012345678").isInternational)
        assertFalse(india.parse("08012345678").isInternational)
        assertFalse(unitedStates.parse("6505551234").isInternational)
    }

    @Test
    fun `a number from another country is international`() {
        assertTrue(india.parse("+16505551234").isInternational)
        assertTrue(unitedStates.parse("+918012345678").isInternational)
    }

    @Test
    fun `a number that arrived with a country code is shown with it`() {
        assertEquals("+91 80 4567 8901", india.parse("+918045678901").display)
        assertEquals("+1 650-555-1234", india.parse("+16505551234").display)
    }

    @Test
    fun `a number that arrived without a country code is shown the local way`() {
        assertEquals("(650) 555-1234", unitedStates.parse("6505551234").display)
        assertEquals("+16505551234", unitedStates.parse("6505551234").key)
    }

    @Test
    fun `no number gives an empty number`() {
        for (missing in listOf(null, "", "   ")) {
            assertEquals(PhoneNumber(raw = "", key = "", isInternational = false, display = ""), india.parse(missing))
        }
    }

    @Test
    fun `a short code keeps its digits and is not international`() {
        val shortCode = india.parse("121")
        assertEquals("121", shortCode.key)
        assertEquals("121", shortCode.display)
        assertFalse(shortCode.isInternational)
    }

    @Test
    fun `text that is not a number is kept as it came`() {
        val odd = india.parse("anonymous")
        assertEquals("anonymous", odd.display)
        assertEquals("", odd.key)
        assertFalse(odd.isInternational)
    }

    @Test
    fun `an unknown home country still reads numbers that carry a country code`() {
        val nowhere = PhoneNumbers("")
        assertEquals("+918012345678", nowhere.parse("+918012345678").key)
        assertFalse(nowhere.parse("+918012345678").isInternational)
        assertEquals("8012345678", nowhere.parse("8012345678").key)
    }
}
