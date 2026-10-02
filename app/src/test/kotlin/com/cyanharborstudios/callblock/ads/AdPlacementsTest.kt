package com.cyanharborstudios.callblock.ads

import com.cyanharborstudios.callblock.ads.AdPlacements.FullScreenTrigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** When a full-screen ad may appear. The rule is small and worth pinning. */
class AdPlacementsTest {

    private val longAgo = 0L
    private val now = 10 * 60_000L

    private fun wants(from: String?, to: String?, lastShownAt: Long = longAgo, trigger: FullScreenTrigger = FullScreenTrigger.ON_LEAVING) =
        AdPlacements.wantsFullScreenAd(from, to, now, lastShownAt, trigger)

    @Test
    fun `the shipped setting is to show on leaving, never on opening`() {
        assertEquals(FullScreenTrigger.ON_LEAVING, AdPlacements.FULL_SCREEN_TRIGGER)
    }

    @Test
    fun `going back from history or statistics is a moment for an ad`() {
        assertTrue(wants(from = "history", to = "home"))
        assertTrue(wants(from = "statistics", to = "home"))
    }

    @Test
    fun `opening history or statistics is not`() {
        assertFalse(wants(from = "home", to = "history"))
        assertFalse(wants(from = "home", to = "statistics"))
    }

    @Test
    fun `other screens never get one`() {
        assertFalse(wants(from = "options", to = "home"))
        assertFalse(wants(from = "settings", to = "home"))
        assertFalse(wants(from = "home", to = "settings"))
        assertFalse(wants(from = "settings", to = "licences"))
    }

    @Test
    fun `not again within the minimum gap, and not straight after the app starts`() {
        val justShown = now - AdPlacements.FULL_SCREEN_MIN_GAP_MILLIS + 1
        assertFalse(wants(from = "history", to = "home", lastShownAt = justShown))
        val gapPassed = now - AdPlacements.FULL_SCREEN_MIN_GAP_MILLIS
        assertTrue(wants(from = "history", to = "home", lastShownAt = gapPassed))
    }

    @Test
    fun `the on-opening setting is the mirror image`() {
        assertTrue(wants(from = "home", to = "history", trigger = FullScreenTrigger.ON_OPENING))
        assertFalse(wants(from = "history", to = "home", trigger = FullScreenTrigger.ON_OPENING))
    }

    @Test
    fun `the never setting shows none`() {
        assertFalse(wants(from = "history", to = "home", trigger = FullScreenTrigger.NEVER))
        assertFalse(wants(from = "home", to = "history", trigger = FullScreenTrigger.NEVER))
    }
}
