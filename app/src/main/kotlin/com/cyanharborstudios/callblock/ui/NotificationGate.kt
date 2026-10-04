package com.cyanharborstudios.callblock.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** What Android reports about this app's notifications. */
enum class NotificationAccess {
    /** The app may post them. */
    ALLOWED,

    /** Android has not been asked yet (or will ask again): switching something on asks. */
    NOT_ASKED,

    /** Switched off in Android's settings; only the user can switch them back on, there. */
    BLOCKED,
}

/** Reads what Android reports, together with whether this app has asked before. */
@Composable
fun rememberNotificationAccess(viewModel: AppViewModel): NotificationAccess {
    val canNotify by viewModel.canNotify.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    return when {
        canNotify -> NotificationAccess.ALLOWED
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> NotificationAccess.BLOCKED
        settings?.notificationsAsked != true -> NotificationAccess.NOT_ASKED
        activity != null && activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> NotificationAccess.NOT_ASKED
        else -> NotificationAccess.BLOCKED
    }
}

/**
 * Returns a function that runs an action only once the app may post notifications.
 * If it already may, the action runs at once. If not, Android's permission prompt is
 * shown and the action runs on a yes. The permission is therefore only ever asked for
 * at the moment the user switches on something that notifies. On a no, the row that
 * was tapped says so itself (see [NotificationAccess.BLOCKED]).
 */
@Composable
fun rememberNotificationRequest(viewModel: AppViewModel): (onAllowed: () -> Unit) -> Unit {
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val permissionRequest = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.refreshSystemState()
        viewModel.markNotificationsAsked()
        val action = pending
        pending = null
        if (granted) action?.invoke()
    }
    return { onAllowed ->
        viewModel.refreshSystemState()
        when {
            viewModel.canNotify.value -> onAllowed()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                pending = onAllowed
                permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            else -> Unit
        }
    }
}

/** Android's notification settings for this app. What the user chooses there is read again on return. */
fun openNotificationSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .apply { if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
    )
}
