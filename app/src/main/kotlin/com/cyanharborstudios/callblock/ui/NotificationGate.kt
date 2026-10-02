package com.cyanharborstudios.callblock.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.cyanharborstudios.callblock.R
import kotlinx.coroutines.launch

/**
 * Returns a function that runs an action only once the app may post notifications.
 *
 * If it already may, the action runs at once. If not, Android's permission prompt is
 * shown and the action runs on a yes. On a no (or when Android will not ask again), a
 * snackbar offers the system's notification settings. The permission is therefore only
 * ever asked for at the moment the user switches on something that notifies.
 */
@Composable
fun rememberNotificationGate(viewModel: AppViewModel, snackbar: SnackbarHostState): (onAllowed: () -> Unit) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val deniedText = stringResource(R.string.notifications_denied)
    val openSettingsLabel = stringResource(R.string.open_settings)
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun refused() {
        scope.launch {
            val result = snackbar.showSnackbar(message = deniedText, actionLabel = openSettingsLabel)
            if (result == SnackbarResult.ActionPerformed) {
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                )
            }
        }
    }

    val permissionRequest = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.refreshSystemState()
        val action = pending
        pending = null
        if (granted) action?.invoke() else refused()
    }

    return { onAllowed ->
        viewModel.refreshSystemState()
        when {
            viewModel.canNotify.value -> onAllowed()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                pending = onAllowed
                permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            else -> refused()
        }
    }
}
