package com.cyanharborstudios.callblock

import com.cyanharborstudios.callblock.ads.AdUnits
import com.cyanharborstudios.callblock.core.plans.Plans
import com.cyanharborstudios.callblock.ui.Links
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The facts that are dangerous to get wrong at release, held by a test instead of memory.
 *
 * The application id becomes permanent with the first Play upload, and so do the three
 * products' ids once they are made in Play Console. The ad ids are Google's
 * published TEST ids: this app must not carry live ids until the founder supplies them, and
 * when he does, this test is what gets changed, on purpose, in the same commit.
 */
class LaunchGateTest {

    /** Every id Google publishes for testing belongs to this publisher. */
    private val googleTestPublisher = "ca-app-pub-3940256099942544"

    @Test
    fun `the application id is the studio's package for this app`() {
        assertEquals("com.cyanharborstudios.callblock", BuildConfig.APPLICATION_ID)
    }

    /**
     * A product's id cannot be changed, or used again, once it exists in Play Console, and
     * every purchase is tied to it. These are the ids the founder's steps create there.
     */
    @Test
    fun `the three products have the ids agreed for Play Console`() {
        assertEquals("no_ads", Plans.NO_ADS_PRODUCT)
        assertEquals("pro", Plans.PRO_PRODUCT)
        assertEquals("pro_upgrade", Plans.PRO_UPGRADE_PRODUCT)
        assertEquals(listOf("no_ads", "pro", "pro_upgrade"), Plans.PRODUCTS)
    }

    /** The subscription's id is as permanent as the products'; its base plans and offer are made under it in Play Console. */
    @Test
    fun `the one subscription has the id agreed for Play Console`() {
        assertEquals("plus", Plans.PLUS_PRODUCT)
        assertEquals(listOf("plus"), Plans.SUBSCRIPTIONS)
        assertFalse(Plans.PLUS_PRODUCT in Plans.PRODUCTS)
    }

    @Test
    fun `the AdMob app id is Google's sample app id`() {
        assertEquals("ca-app-pub-3940256099942544~3347511713", AdUnits.APP_ID)
    }

    @Test
    fun `the banner unit is Google's test adaptive banner`() {
        assertEquals("ca-app-pub-3940256099942544/9214589741", AdUnits.BANNER)
    }

    @Test
    fun `the full-screen unit is Google's test interstitial`() {
        assertEquals("ca-app-pub-3940256099942544/1033173712", AdUnits.FULL_SCREEN)
    }

    @Test
    fun `no ad id belongs to a real publisher`() {
        for (id in listOf(AdUnits.APP_ID, AdUnits.BANNER, AdUnits.FULL_SCREEN)) {
            assertTrue("$id is not a Google test id", id.startsWith(googleTestPublisher))
        }
    }

    /**
     * The Privacy Policy row and the two store rows are hidden while their pages do not
     * exist (ui/Links.kt). A build with live ad ids is a build for the store, and Google
     * Play requires the privacy link inside the app: the rows must be back by then.
     */
    @Test
    fun `live ad ids never ship while the privacy row or the store rows are hidden`() {
        assertTrue(
            "live ad ids need Links.PRIVACY_PAGE_LIVE and Links.STORE_PAGE_LIVE switched on",
            rowsAreReadyFor(listOf(AdUnits.APP_ID, AdUnits.BANNER, AdUnits.FULL_SCREEN), Links.PRIVACY_PAGE_LIVE, Links.STORE_PAGE_LIVE),
        )
    }

    @Test
    fun `the rule about hidden rows bites once an ad id is live`() {
        val test = listOf("$googleTestPublisher/1")
        val live = listOf("ca-app-pub-1111111111111111/1")
        assertTrue(rowsAreReadyFor(test, privacyPageLive = false, storePageLive = false))
        assertTrue(rowsAreReadyFor(live, privacyPageLive = true, storePageLive = true))
        assertFalse(rowsAreReadyFor(live, privacyPageLive = false, storePageLive = true))
        assertFalse(rowsAreReadyFor(live, privacyPageLive = true, storePageLive = false))
    }

    private fun rowsAreReadyFor(adIds: List<String>, privacyPageLive: Boolean, storePageLive: Boolean): Boolean {
        val anyLive = adIds.any { !it.startsWith(googleTestPublisher) }
        return !anyLive || (privacyPageLive && storePageLive)
    }
}
