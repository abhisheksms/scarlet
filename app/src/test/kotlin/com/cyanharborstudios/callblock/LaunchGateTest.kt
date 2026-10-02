package com.cyanharborstudios.callblock

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The facts that are dangerous to get wrong at release, held by a test instead of memory.
 * The application id becomes permanent with the first Play upload.
 */
class LaunchGateTest {

    @Test
    fun `the application id is the studio's package for this app`() {
        assertEquals("com.cyanharborstudios.callblock", BuildConfig.APPLICATION_ID)
    }
}
