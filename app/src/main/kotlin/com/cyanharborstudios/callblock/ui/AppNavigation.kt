package com.cyanharborstudios.callblock.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cyanharborstudios.callblock.ads.AdsController
import com.cyanharborstudios.callblock.ads.BannerSlot
import com.cyanharborstudios.callblock.ads.bannerSlotHeight
import androidx.compose.foundation.layout.PaddingValues

/** The app's screens and how the user moves between them. */
object Routes {
    const val HOME = "home"
    const val OPTIONS = "options"
    const val HISTORY = "history"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val LICENCES = "licences"
}

/** Every screen, with the one banner slot fixed along the bottom edge beneath them all. */
@Composable
fun AppNavigation(
    viewModel: AppViewModel,
    ads: AdsController,
    /** A screen to open straight away (from a notification), or null. */
    openOnStart: String?,
    onOpened: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    LaunchedEffect(openOnStart) {
        if (openOnStart != null) {
            navController.navigate(openOnStart) { launchSingleTop = true }
            onOpened()
        }
    }

    // Tell the ads controller about every move between screens, however it was made
    // (a row, the back arrow, the system back gesture). It decides whether that is a
    // moment for a full-screen ad.
    val activity = LocalActivity.current
    DisposableEffect(navController, activity) {
        var previousRoute: String? = null
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val route = destination.route
            if (activity != null && previousRoute != null) ads.onScreenChanged(activity, previousRoute, route)
            previousRoute = route
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    val privacyOptionsRequired by ads.privacyOptionsRequired.collectAsStateWithLifecycle()
    val back: () -> Unit = { navController.popBackStack() }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        // The banner slot below takes the navigation-bar inset and its own height, so the screens
        // must not: a keyboard inset read inside a screen is then only what covers the screen.
        Box(
            Modifier
                .weight(1f)
                .consumeWindowInsets(WindowInsets.navigationBars)
                .consumeWindowInsets(PaddingValues(bottom = bannerSlotHeight())),
        ) {
            // A screen change is a cut: every screen lands settled, with nothing arriving after the first frame.
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        viewModel = viewModel,
                        onOpenOptions = { navController.navigate(Routes.OPTIONS) },
                        onOpenHistory = { navController.navigate(Routes.HISTORY) },
                        onOpenStatistics = { navController.navigate(Routes.STATISTICS) },
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    )
                }
                composable(Routes.OPTIONS) { OptionsScreen(viewModel, back) }
                composable(Routes.HISTORY) { HistoryScreen(viewModel, back) }
                composable(Routes.STATISTICS) { StatisticsScreen(viewModel, back) }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = back,
                        onOpenLicences = { navController.navigate(Routes.LICENCES) },
                        onOpenPrivacyChoices = if (privacyOptionsRequired && activity != null) {
                            { ads.showPrivacyOptions(activity) }
                        } else {
                            null
                        },
                    )
                }
                composable(Routes.LICENCES) { LicencesScreen(back) }
            }
        }
        BannerSlot(ads)
    }
}
