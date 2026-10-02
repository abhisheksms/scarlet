package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.numbers.PhoneNumber
import com.cyanharborstudios.callblock.core.rules.Action
import com.cyanharborstudios.callblock.core.stats.Statistics
import com.cyanharborstudios.callblock.core.time.TimeText
import com.cyanharborstudios.callblock.data.HandledCallEntity
import com.cyanharborstudios.callblock.ui.theme.OutcomeColors
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Every call the app blocked or silenced, newest first, grouped by day. */
@Composable
fun HistoryScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val calls by viewModel.handledCalls.collectAsStateWithLifecycle()
    val allowed by viewModel.allowedNumbers.collectAsStateWithLifecycle()
    val now = rememberNowMillis()
    val timeText = rememberTimeText()
    val numbers = rememberPhoneNumbers()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var confirmingDeleteAll by rememberSaveable { mutableStateOf(false) }
    var detailsFor by remember { mutableStateOf<PhoneNumber?>(null) }
    var allowing by remember { mutableStateOf<PhoneNumber?>(null) }

    val allowedKeys = allowed.orEmpty()
        .filter { it.expiresAtMillis == null || it.expiresAtMillis > now }
        .map { it.numberKey }
        .toSet()
    val addedMessage = stringResource(R.string.added_to_allow_list)

    AppScreen(
        title = stringResource(R.string.history),
        onBack = onBack,
        actions = {
            if (!calls.isNullOrEmpty()) {
                IconButton(onClick = { confirmingDeleteAll = true }, modifier = Modifier.testTag("delete-all")) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_all))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        val loaded = calls ?: return@AppScreen
        if (loaded.isEmpty()) {
            EmptyMessage(stringResource(R.string.history_empty), padding)
            return@AppScreen
        }
        val today = timeText.dateOf(now)
        val byDay = remember(loaded, timeText) { loaded.groupBy { timeText.dateOf(it.atMillis) } }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).testTag("history-list")) {
            for ((day, callsThatDay) in byDay) {
                item(key = "day-$day") {
                    Text(
                        dayHeading(day, today, timeText),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                    )
                }
                items(callsThatDay, key = { it.id }) { call ->
                    val number = numbers.parse(call.numberRaw)
                    HistoryRow(
                        call = call,
                        number = number,
                        time = timeText.time(call.atMillis),
                        canAllow = number.key.isNotEmpty() && number.key !in allowedKeys,
                        onOpen = { detailsFor = number },
                        onAllow = { allowing = number },
                        onDelete = { viewModel.deleteHandledCall(call.id) },
                    )
                }
            }
        }
    }

    detailsFor?.let { number ->
        val details = remember(calls, number) {
            Statistics.detailsFor(number.key, calls.orEmpty().map { it.toHandledCall() })
        }
        if (details == null) {
            detailsFor = null
        } else {
            NumberDetailsSheet(
                number = number,
                details = details,
                isAllowed = number.key in allowedKeys,
                timeText = timeText,
                onAllow = {
                    detailsFor = null
                    allowing = number
                },
                onRemoveAllowed = { viewModel.removeAllowed(number.key) },
                onDismiss = { detailsFor = null },
            )
        }
    }

    allowing?.let { number ->
        AllowNumberDialog(
            fixedNumber = number,
            parse = numbers::parse,
            onAllow = { chosen, minutes ->
                viewModel.allow(chosen, minutes)
                allowing = null
                scope.launch { snackbar.showSnackbar(addedMessage.format(chosen.display)) }
            },
            onDismiss = { allowing = null },
        )
    }

    if (confirmingDeleteAll) {
        AlertDialog(
            modifier = Modifier.exposeTestTags(),
            onDismissRequest = { confirmingDeleteAll = false },
            title = { Text(stringResource(R.string.delete_all_title)) },
            text = { Text(stringResource(R.string.delete_all_text)) },
            confirmButton = {
                TextButton(
                    modifier = Modifier.testTag("delete-all-confirm"),
                    onClick = {
                        viewModel.deleteAllHandledCalls()
                        confirmingDeleteAll = false
                    },
                ) { Text(stringResource(R.string.delete_all)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDeleteAll = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun HistoryRow(
    call: HandledCallEntity,
    number: PhoneNumber,
    time: String,
    canAllow: Boolean,
    onOpen: () -> Unit,
    onAllow: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val silenced = call.action == Action.SILENCE.name
    val display = number.display.ifEmpty { stringResource(R.string.no_number) }

    ListItem(
        headlineContent = { Text(display) },
        supportingContent = {
            // The outcome is a word as well as a colour, then the time.
            Text(
                "${stringResource(if (silenced) R.string.silenced else R.string.blocked)} · $time",
                color = if (silenced) OutcomeColors.silenced else OutcomeColors.blocked,
            )
        },
        trailingContent = {
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more_for_number, display))
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    if (canAllow) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.allow_this_number)) },
                            onClick = {
                                menuOpen = false
                                onAllow()
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        },
                    )
                }
            }
        },
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onOpen)
            .testTag("history-row"),
    )
}

@Composable
fun dayHeading(day: LocalDate, today: LocalDate, timeText: TimeText): String = when (day) {
    today -> stringResource(R.string.today)
    today.minusDays(1) -> stringResource(R.string.yesterday)
    else -> timeText.date(day)
}

@Composable
fun EmptyMessage(text: String, padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
