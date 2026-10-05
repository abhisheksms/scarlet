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
    fun `India's 140 and 160 series numbers have one key however they are written`() {
        for (written in listOf("1401234567", "01401234567", "+91 140 123 4567", "140-123-4567")) {
            assertEquals(written, "+911401234567", india.parse(written).key)
        }
        for (written in listOf("1600123456", "01600123456", "+91 1600 123 456")) {
            assertEquals(written, "+911600123456", india.parse(written).key)
        }
        // From a phone in another country the series still carry India's code.
        assertEquals("+911600123456", unitedStates.parse("+911600123456").key)
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

    // --- the start of a number, for the user's own number rules ---

    @Test
    fun `the start of a number typed two ways has one start key`() {
        assertEquals("+91804567", india.startKey("080 4567"))
        assertEquals("+91804567", india.startKey("+91 80 4567"))
        assertEquals("+91804567", india.startKey(" 80-4567 "))
        // A series, and a whole country.
        assertEquals("+91140", india.startKey("140"))
        assertEquals("+92", india.startKey("+92"))
        assertEquals("+9221", india.startKey("0092 21"))
        // The United States writes a local number with 1 in front, not 0.
        assertEquals("+1555111", unitedStates.startKey("555111"))
        assertEquals("+1555111", unitedStates.startKey("1 555 111"))
    }

    @Test
    fun `a start is the start of the keys of the numbers it is meant for`() {
        assertTrue(india.parse("080 4567 8901").key.startsWith(india.startKey("080 4567")!!))
        assertTrue(india.parse("+92 21 1234 5678").key.startsWith(india.startKey("+92")!!))
        assertTrue(india.parse("1401234567").key.startsWith(india.startKey("140")!!))
        assertFalse(india.parse("080 4568 8901").key.startsWith(india.startKey("080 4567")!!))
    }

    @Test
    fun `text with no digits to go by is not a start`() {
        for (typed in listOf(null, "", "  ", "abc", "+", "0", "00")) {
            assertEquals(typed, null, india.startKey(typed))
        }
    }

    @Test
    fun `without a known country a start needs its country code`() {
        val nowhere = PhoneNumbers("ZZ")
        assertEquals(null, nowhere.startKey("80 4567"))
        assertEquals("+91804567", nowhere.startKey("+91 80 4567"))
    }

    @Test
    fun `a start is shown spaced as a number is while it is typed`() {
        assertEquals("+91 80 4567", india.startDisplay("+91804567"))
        assertEquals("+92", india.startDisplay("+92"))
        assertEquals("+91 140", india.startDisplay("+91140"))
        assertEquals("+1 555-111", unitedStates.startDisplay("+1555111"))
    }
}
