package com.cyanharborstudios.callblock.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/** Which screen a launch starts on: How It Works, until it has been closed once. */
class RoutesTest {

    @Test
    fun `a first launch opens how it works`() {
        assertEquals(Routes.HOW_IT_WORKS, Routes.first(howItWorksSeen = false, asked = null))
    }

    @Test
    fun `once it has been closed a launch opens home`() {
        assertEquals(Routes.HOME, Routes.first(howItWorksSeen = true, asked = null))
    }

    @Test
    fun `it never opens in the way of a screen a notification asked for`() {
        assertEquals(Routes.HOME, Routes.first(howItWorksSeen = false, asked = Routes.HISTORY))
        assertEquals(Routes.HOME, Routes.first(howItWorksSeen = true, asked = Routes.STATISTICS))
    }
}
