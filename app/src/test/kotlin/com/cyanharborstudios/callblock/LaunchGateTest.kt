package com.cyanharborstudios.callblock

import com.cyanharborstudios.callblock.ads.AdUnits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The facts that are dangerous to get wrong at release, held by a test instead of memory.
 *
 * The application id becomes permanent with the first Play upload. The ad ids are Google's
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
}
