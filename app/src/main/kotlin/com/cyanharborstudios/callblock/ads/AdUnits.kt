package com.cyanharborstudios.callblock.ads

import com.cyanharborstudios.callblock.BuildConfig

/**
 * Every ad id the app uses. These are Google's published TEST ids
 * (developers.google.com/admob/android/test-ads): they serve test ads on any device and
 * earn nothing. Live ids come only from the founder, and LaunchGateTest pins whatever is
 * here, so a change is always deliberate.
 */
object AdUnits {
    /** The AdMob app id. It also has to be in the manifest, so it is defined in app/build.gradle.kts. */
    const val APP_ID = BuildConfig.ADMOB_APP_ID

    /** Anchored adaptive banner. */
    const val BANNER = "ca-app-pub-3940256099942544/9214589741"

    /** Full-screen (interstitial). */
    const val FULL_SCREEN = "ca-app-pub-3940256099942544/1033173712"
}
