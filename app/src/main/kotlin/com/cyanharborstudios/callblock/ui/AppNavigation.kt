package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/** The app's screens and how the user moves between them. */
object Routes {
    const val HOME = "home"
    const val OPTIONS = "options"
    const val HISTORY = "history"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val LICENCES = "licences"
}

@Composable
fun AppNavigation(
    viewModel: AppViewModel,
    /** A screen to open straight away (from a notification), or null. */
    openOnStart: String?,
    onOpened: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    LaunchedEffect(openOnStart) {
        if (openOnStart != null) {
            navController.navigate(openOnStart) { launchSingleTop = true }
            onOpened()
        }
    }
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onOpenOptions = { navController.navigate(Routes.OPTIONS) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenStatistics = { navController.navigate(Routes.STATISTICS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                bottomBar = {},
            )
        }
        composable(Routes.OPTIONS) { Placeholder("Options", back) }
        composable(Routes.HISTORY) { Placeholder("History", back) }
        composable(Routes.STATISTICS) { Placeholder("Statistics", back) }
        composable(Routes.SETTINGS) { Placeholder("Settings", back) }
        composable(Routes.LICENCES) { Placeholder("Licences", back) }
    }
}

@Composable
private fun Placeholder(title: String, onBack: () -> Unit) {
    AppScreen(title = title, onBack = onBack) { padding -> Text("…", Modifier.padding(padding)) }
}
