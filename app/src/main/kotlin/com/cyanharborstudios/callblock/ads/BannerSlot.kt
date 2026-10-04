package com.cyanharborstudios.callblock.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.ui.parts.CapsText
import com.cyanharborstudios.callblock.ui.parts.engravedTop
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/** The anchored adaptive banner size for this screen's width. */
@Composable
fun rememberBannerAdSize(): AdSize {
    val context = LocalContext.current
    val widthDp = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp().value.toInt() }
    return remember(widthDp) { AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp) }
}

/** The sill above the banner: as tall as its label's line. */
@Composable
fun bannerSillHeight(): Dp = (20 * LocalDensity.current.fontScale).dp

/** The slot's height above the gesture inset: the sill and the banner. The screens above it are measured without it. */
@Composable
fun bannerSlotHeight(): Dp = bannerSillHeight() + rememberBannerAdSize().height.dp

/**
 * The one banner, along the bottom edge of the app, below every screen: a recessed
 * tray with a sill that names what it holds once an ad has loaded.
 *
 * The tray takes the banner's full height from the first frame, before any ad has
 * loaded and whether or not one ever does. So an ad arriving late cannot move
 * anything, and a tap aimed at the app's own controls cannot land on it.
 */
@Composable
fun BannerSlot(ads: AdsController) {
    val ready by ads.ready.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val adSize = rememberBannerAdSize()
    var loaded by remember { mutableStateOf(false) }
    val sillHeight = bannerSillHeight()

    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow)
            .engravedTop(colors.outlineVariant, colors.surfaceContainerHighest)
            .padding(top = 1.dp)
            .navigationBarsPadding()
            .testTag("banner-slot"),
    ) {
        Box(Modifier.fillMaxWidth().height(sillHeight).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
            if (loaded) CapsText(stringResource(R.string.advertisement), SwitchboardType.slot, color = colors.onSurfaceVariant, maxLines = 1)
        }
        Box(Modifier.fillMaxWidth().height(adSize.height.dp), contentAlignment = Alignment.Center) {
            if (ready) {
                AndroidView(
                    factory = { viewContext ->
                        AdView(viewContext).apply {
                            setAdSize(adSize)
                            adUnitId = AdUnits.BANNER
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    loaded = true
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    loaded = false
                                }
                            }
                            loadAd(AdRequest.Builder().build())
                        }
                    },
                    onRelease = { it.destroy() },
                )
            }
        }
    }
}
