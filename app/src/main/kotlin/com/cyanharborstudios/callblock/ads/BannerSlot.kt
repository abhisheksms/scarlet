package com.cyanharborstudios.callblock.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * The one banner, along the bottom edge of the app, below every screen.
 *
 * The slot takes the banner's full height from the first frame, before any ad has loaded
 * and whether or not one ever does. So an ad arriving late cannot move anything, and a
 * tap aimed at the app's own controls cannot land on it.
 */
@Composable
fun BannerSlot(ads: AdsController) {
    val ready by ads.ready.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val widthDp = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp().value.toInt() }
    val adSize = remember(widthDp) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .navigationBarsPadding()
            .height(adSize.height.dp)
            .testTag("banner-slot"),
        contentAlignment = Alignment.Center,
    ) {
        if (ready) {
            AndroidView(
                factory = { viewContext ->
                    AdView(viewContext).apply {
                        setAdSize(adSize)
                        adUnitId = AdUnits.BANNER
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = { it.destroy() },
            )
        }
    }
}
