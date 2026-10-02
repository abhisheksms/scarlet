package com.cyanharborstudios.callblock.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Consent first, then ads.
 *
 * Nothing here runs until an activity calls [start]: the screening service never touches
 * ads, and a call never waits on them. The ads SDK is initialised only after Google's
 * consent platform (UMP) says ads may be requested.
 */
class AdsController(context: Context, private val scope: CoroutineScope) {

    private val appContext = context.applicationContext
    private val consent: ConsentInformation by lazy { UserMessagingPlatform.getConsentInformation(appContext) }
    private val sdkStarted = AtomicBoolean(false)

    private val readyState = MutableStateFlow(false)

    /** True once consent allows ads and the SDK is initialised. */
    val ready: StateFlow<Boolean> = readyState.asStateFlow()

    private val privacyOptionsRequiredState = MutableStateFlow(false)

    /** True when the user must be offered a way back to their privacy choices (shown in Settings). */
    val privacyOptionsRequired: StateFlow<Boolean> = privacyOptionsRequiredState.asStateFlow()

    private var fullScreenAd: InterstitialAd? = null
    private var fullScreenLastShownAt = System.currentTimeMillis()

    /** Call on every launch of the activity. Shows Google's consent form if one is required. */
    fun start(activity: Activity) {
        consent.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            { UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { startSdkIfAllowed() } },
            { startSdkIfAllowed() },
        )
        // Consent given on an earlier launch still stands; don't wait for the network to use it.
        startSdkIfAllowed()
    }

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { startSdkIfAllowed() }
    }

    private fun startSdkIfAllowed() {
        privacyOptionsRequiredState.value =
            consent.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        if (!consent.canRequestAds()) return
        if (sdkStarted.getAndSet(true)) return
        scope.launch(Dispatchers.IO) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_PG)
                    .build(),
            )
            MobileAds.initialize(appContext) {}
            withContext(Dispatchers.Main) {
                readyState.value = true
                loadFullScreenAd()
            }
        }
    }

    /**
     * Shows a full-screen ad if the move from screen [from] to screen [to] is a moment
     * for one (see [AdPlacements]) and one is loaded. Otherwise does nothing.
     */
    fun onScreenChanged(activity: Activity, from: String?, to: String?) {
        val now = System.currentTimeMillis()
        if (!AdPlacements.wantsFullScreenAd(from, to, now, fullScreenLastShownAt)) return
        val ad = fullScreenAd ?: return
        fullScreenAd = null
        fullScreenLastShownAt = now
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() = loadFullScreenAd()
            override fun onAdFailedToShowFullScreenContent(error: AdError) = loadFullScreenAd()
        }
        ad.show(activity)
    }

    private fun loadFullScreenAd() {
        if (AdPlacements.FULL_SCREEN_TRIGGER == AdPlacements.FullScreenTrigger.NEVER) return
        InterstitialAd.load(
            appContext,
            AdUnits.FULL_SCREEN,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    fullScreenAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    fullScreenAd = null
                }
            },
        )
    }
}
