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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cyanharborstudios.callblock.ads.AdsController
import com.cyanharborstudios.callblock.ads.BannerSlot
import com.cyanharborstudios.callblock.ads.bannerSlotHeight
import com.cyanharborstudios.callblock.core.plans.Plans
import androidx.compose.foundation.layout.PaddingValues

/** The app's screens and how the user moves between them. */
object Routes {
    const val HOME = "home"
    const val OPTIONS = "options"
    const val HISTORY = "history"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val HOW_IT_WORKS = "how-it-works"
    const val SCHEDULE = "schedule"
    const val PLANS = "plans"
    const val ABOUT = "about"
    const val LICENCES = "licences"

    /**
     * The first screen of a launch. How It Works opens by itself until it has been closed
     * once, and never in the way of a screen a notification asked for.
     */
    fun first(howItWorksSeen: Boolean, asked: String?): String =
        if (!howItWorksSeen && asked == null) HOW_IT_WORKS else HOME
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
    // The first screen is chosen once, when the settings have been read. Until then only the ground is drawn.
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val howItWorksSeen = settings?.howItWorksSeen
    var first by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(howItWorksSeen != null) {
        if (first == null && howItWorksSeen != null) first = Routes.first(howItWorksSeen, openOnStart)
    }

    // The screens exist once the first one is chosen; a destination asked for earlier waits for that.
    LaunchedEffect(openOnStart, first) {
        if (openOnStart != null && first != null) {
            navController.navigate(openOnStart) { launchSingleTop = true }
            onOpened()
        }
    }

    // Tell the ads controller about every move between screens, however it was made
    // (a row, the back arrow, the system back gesture). It decides whether that is a
    // moment for a full-screen ad.
    val activity = LocalActivity.current

    // Ads are the free plan's. For anyone who has paid, nothing of them is started, shown or
    // given room. Null until the settings have been read: until then nothing is started either.
    val showsAds = settings?.tier?.let(Plans::showsAds)
    LaunchedEffect(showsAds, activity) {
        // Consent first, then the ads SDK (see AdsController). Never before the plan is known.
        if (showsAds == true && activity != null) ads.start(activity)
    }
    val adsOn by rememberUpdatedState(showsAds == true)

    DisposableEffect(navController, activity) {
        var previousRoute: String? = null
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val route = destination.route
            if (activity != null && previousRoute != null && adsOn) ads.onScreenChanged(activity, previousRoute, route)
            previousRoute = route
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    val privacyOptionsRequired by ads.privacyOptionsRequired.collectAsStateWithLifecycle()
    val back: () -> Unit = { navController.popBackStack() }

    val slotHeight = if (showsAds == true) bannerSlotHeight() else 0.dp
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        // The banner slot below takes the navigation-bar inset and its own height, so the screens
        // must not: a keyboard inset read inside a screen is then only what covers the screen.
        // With no ads there is no slot, and the screens stop at the gesture inset themselves.
        Box(
            Modifier
                .weight(1f)
                .then(
                    if (showsAds == true) {
                        Modifier.consumeWindowInsets(WindowInsets.navigationBars).consumeWindowInsets(PaddingValues(bottom = slotHeight))
                    } else {
                        Modifier.navigationBarsPadding()
                    },
                ),
        ) {
            val start = first ?: return@Box
            // A screen change is a cut: every screen lands settled, with nothing arriving after the first frame.
            NavHost(
                navController = navController,
                startDestination = start,
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
                        onOpenSchedule = { navController.navigate(Routes.SCHEDULE) },
                        onOpenPlans = { navController.navigate(Routes.PLANS) },
                    )
                }
                composable(Routes.SCHEDULE) { ScheduleScreen(viewModel, back) }
                composable(Routes.PLANS) { PlansScreen(viewModel, back) }
                composable(Routes.OPTIONS) { OptionsScreen(viewModel, back, onOpenPlans = { navController.navigate(Routes.PLANS) }) }
                composable(Routes.HISTORY) { HistoryScreen(viewModel, back) }
                composable(Routes.STATISTICS) { StatisticsScreen(viewModel, back) }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = back,
                        onOpenHowItWorks = { navController.navigate(Routes.HOW_IT_WORKS) },
                        onOpenPlans = { navController.navigate(Routes.PLANS) },
                        onOpenAbout = { navController.navigate(Routes.ABOUT) },
                        onOpenPrivacyChoices = if (privacyOptionsRequired && activity != null) {
                            { ads.showPrivacyOptions(activity) }
                        } else {
                            null
                        },
                    )
                }
                composable(Routes.HOW_IT_WORKS) {
                    HowItWorksScreen(
                        callBackMinutes = settings?.screening?.takeIf { it.callBacksRing }?.callBackWindowMinutes,
                        onDone = {
                            if (navController.currentDestination?.route == Routes.HOW_IT_WORKS) {
                                if (howItWorksSeen == false) viewModel.markHowItWorksSeen()
                                // Opened from Settings it goes back there; opened by itself it gives way to Home.
                                if (navController.previousBackStackEntry != null) {
                                    navController.popBackStack()
                                } else {
                                    navController.navigate(Routes.HOME) { popUpTo(Routes.HOW_IT_WORKS) { inclusive = true } }
                                }
                            }
                        },
                    )
                }
                composable(Routes.ABOUT) { AboutScreen(back, onOpenLicences = { navController.navigate(Routes.LICENCES) }) }
                composable(Routes.LICENCES) { LicencesScreen(back) }
            }
        }
        if (showsAds == true) BannerSlot(ads)
    }
}
