package com.cyanharborstudios.callblock.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.Scope
import com.cyanharborstudios.callblock.core.stats.Statistics
import java.time.ZoneId

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onOpenOptions: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val calls by viewModel.handledCalls.collectAsStateWithLifecycle()
    val roleHeld by viewModel.roleHeld.collectAsStateWithLifecycle()
    val canNotify by viewModel.canNotify.collectAsStateWithLifecycle()
    val now = rememberNowMillis()
    val timeText = rememberTimeText()
    val snackbar = remember { SnackbarHostState() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshSystemState() }

    // Choosing Silence or Block without the role asks Android for it first. The mode the
    // user picked is applied only if they accept.
    var modeAwaitingRole by rememberSaveable { mutableStateOf<Mode?>(null) }
    val roleRequest = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.refreshSystemState()
        val wanted = modeAwaitingRole
        modeAwaitingRole = null
        if (wanted != null && viewModel.roleHeld.value) viewModel.setMode(wanted)
    }
    fun requestRole(thenMode: Mode?) {
        val intent = viewModel.roleRequestIntent() ?: return
        modeAwaitingRole = thenMode
        roleRequest.launch(intent)
    }

    val whenNotificationsAllowed = rememberNotificationGate(viewModel, snackbar)

    AppScreen(
        title = stringResource(R.string.app_name),
        actions = {
            IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("open-settings")) {
                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        val current = settings ?: return@AppScreen
        val screening = current.screening
        val paused = screening.mode != Mode.OFF && now < screening.pausedUntilMillis

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModeSelector(
                selected = screening.mode,
                scope = screening.scope,
                onSelect = { mode ->
                    when {
                        mode == Mode.OFF || roleHeld -> viewModel.setMode(mode)
                        else -> requestRole(thenMode = mode)
                    }
                },
            )

            if (screening.mode != Mode.OFF && !roleHeld) {
                Notice(
                    text = stringResource(
                        if (viewModel.roleAvailable) R.string.role_missing else R.string.role_unavailable,
                    ),
                    actionLabel = if (viewModel.roleAvailable) stringResource(R.string.role_request) else null,
                    onAction = { requestRole(thenMode = null) },
                )
            } else if (paused) {
                Notice(
                    text = stringResource(R.string.paused_until, timeText.time(screening.pausedUntilMillis)),
                    actionLabel = stringResource(R.string.resume),
                    onAction = viewModel::resume,
                )
            }

            Section {
                val notifying = current.notifyHandledCalls && canNotify
                SwitchRow(
                    title = stringResource(R.string.notifications),
                    detail = stringResource(R.string.notifications_detail),
                    checked = notifying,
                    tag = "notifications",
                    onChange = { wanted ->
                        if (wanted) {
                            whenNotificationsAllowed { viewModel.setNotifyHandledCalls(true) }
                        } else {
                            viewModel.setNotifyHandledCalls(false)
                        }
                    },
                )
                HorizontalDivider()
                LinkRow(
                    title = stringResource(R.string.options),
                    detail = stringResource(R.string.options_detail),
                    tag = "open-options",
                    onClick = onOpenOptions,
                )
            }

            Section {
                val loaded = calls
                val summary = remember(loaded, now / DAY_BUCKET_MILLIS) {
                    loaded?.let {
                        Statistics.summarize(it.map { call -> call.toHandledCall() }, now, ZoneId.systemDefault(), 30)
                    }
                }
                LinkRow(
                    title = stringResource(R.string.history),
                    detail = when {
                        summary == null -> ""
                        summary.today.total == 0 -> stringResource(R.string.history_none_today)
                        else -> pluralStringResource(R.plurals.history_today, summary.today.total, summary.today.total)
                    },
                    tag = "open-history",
                    onClick = onOpenHistory,
                )
                HorizontalDivider()
                LinkRow(
                    title = stringResource(R.string.statistics),
                    detail = summary?.let {
                        stringResource(R.string.statistics_week, it.lastSevenDays.blocked, it.lastSevenDays.silenced)
                    }.orEmpty(),
                    tag = "open-statistics",
                    onClick = onOpenStatistics,
                )
            }
        }
    }
}

/**
 * The main switch: three choices in one group. The chosen one is tinted and says, in a
 * line, what is happening to calls right now. The others stay one line tall, so the whole
 * switch and the state it is in fit on a small screen with large text.
 */
@Composable
private fun ModeSelector(selected: Mode, scope: Scope, onSelect: (Mode) -> Unit) {
    val international = scope == Scope.INTERNATIONAL_ONLY
    OutlinedCard(Modifier.fillMaxWidth().selectableGroup()) {
        ModeOption(Mode.OFF, selected, R.string.mode_off, R.string.mode_off_detail, onSelect)
        HorizontalDivider()
        ModeOption(
            Mode.SILENCE, selected, R.string.mode_silence,
            if (international) R.string.mode_silence_detail_international else R.string.mode_silence_detail,
            onSelect,
        )
        HorizontalDivider()
        ModeOption(
            Mode.BLOCK, selected, R.string.mode_block,
            if (international) R.string.mode_block_detail_international else R.string.mode_block_detail,
            onSelect,
        )
    }
}

@Composable
private fun ModeOption(mode: Mode, selected: Mode, title: Int, detail: Int, onSelect: (Mode) -> Unit) {
    val isSelected = mode == selected
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) colors.primaryContainer else Color.Transparent)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(mode) })
            .padding(horizontal = 12.dp, vertical = 14.dp)
            .testTag("mode-${mode.name}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = isSelected, onClick = null)
        Column(Modifier.padding(start = 12.dp)) {
            Text(
                stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) colors.onPrimaryContainer else colors.onSurface,
            )
            if (isSelected) {
                Text(
                    stringResource(detail),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onPrimaryContainer,
                )
            }
        }
    }
}

/** Something the user should know about right now, with at most one thing to do about it. */
@Composable
private fun Notice(text: String, actionLabel: String?, onAction: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
            if (actionLabel != null) {
                Button(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
fun SwitchRow(
    title: String,
    detail: String?,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    tag: String? = null,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = detail?.let { { Text(it) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .then(if (tag != null) Modifier.testTag(tag) else Modifier),
    )
}

@Composable
fun LinkRow(title: String, detail: String?, tag: String? = null, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = detail?.takeIf { it.isNotEmpty() }?.let { { Text(it) } },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .then(if (tag != null) Modifier.testTag(tag) else Modifier),
    )
}

/** Home's summary only needs recomputing when the calls change or the day rolls over. */
private const val DAY_BUCKET_MILLIS = 60_000L
