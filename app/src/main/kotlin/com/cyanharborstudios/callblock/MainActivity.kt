package com.cyanharborstudios.callblock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cyanharborstudios.callblock.ui.AppNavigation
import com.cyanharborstudios.callblock.ui.AppViewModel
import com.cyanharborstudios.callblock.ui.Routes
import com.cyanharborstudios.callblock.ui.theme.CallBlockTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels {
        AppViewModel.Factory((application as CallBlockApp).container)
    }

    /** The screen a tapped notification asked for, until it has been opened. */
    private var openOnStart by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) openOnStart = destinationOf(intent)
        setContent {
            CallBlockTheme {
                AppNavigation(
                    viewModel = viewModel,
                    openOnStart = openOnStart,
                    onOpened = { openOnStart = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openOnStart = destinationOf(intent)
    }

    /** Only the two destinations a notification can name are accepted; anything else is ignored. */
    private fun destinationOf(intent: Intent?): String? = when (intent?.getStringExtra(EXTRA_OPEN)) {
        OPEN_HISTORY -> Routes.HISTORY
        OPEN_STATISTICS -> Routes.STATISTICS
        else -> null
    }

    companion object {
        const val EXTRA_OPEN = "open"
        const val OPEN_HISTORY = "history"
        const val OPEN_STATISTICS = "statistics"
    }
}
