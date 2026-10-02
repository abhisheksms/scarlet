package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.numbers.PhoneNumber
import com.cyanharborstudios.callblock.core.rules.Scope
import com.cyanharborstudios.callblock.core.time.TimeText

/** Who is filtered, and the three ways a call can get through anyway. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptionsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allowed by viewModel.allowedNumbers.collectAsStateWithLifecycle()
    val now = rememberNowMillis()
    val timeText = rememberTimeText()
    val numbers = rememberPhoneNumbers()
    var choosingPause by rememberSaveable { mutableStateOf(false) }
    var addingNumber by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.removeExpiredAllowed() }

    AppScreen(title = stringResource(R.string.options), onBack = onBack) { padding ->
        val screening = settings?.screening ?: return@AppScreen
        val paused = now < screening.pausedUntilMillis

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionHeading(stringResource(R.string.scope_heading))
            Section(Modifier.selectableGroup()) {
                RadioRow(stringResource(R.string.scope_all), screening.scope == Scope.ALL_UNKNOWN, "scope-all") {
                    viewModel.setScope(Scope.ALL_UNKNOWN)
                }
                HorizontalDivider()
                RadioRow(stringResource(R.string.scope_international), screening.scope == Scope.INTERNATIONAL_ONLY, "scope-international") {
                    viewModel.setScope(Scope.INTERNATIONAL_ONLY)
                }
            }

            SectionHeading(stringResource(R.string.exceptions_heading))
            Section {
                SwitchRow(
                    title = stringResource(R.string.pause),
                    detail = if (paused) {
                        stringResource(R.string.pause_active, timeText.time(screening.pausedUntilMillis))
                    } else {
                        stringResource(R.string.pause_detail)
                    },
                    checked = paused,
                    tag = "pause",
                    onChange = { wanted -> if (wanted) choosingPause = true else viewModel.resume() },
                )
                HorizontalDivider()
                SwitchRow(
                    title = stringResource(R.string.repeat_callers),
                    detail = stringResource(R.string.repeat_callers_detail),
                    checked = screening.repeatCallsRing,
                    tag = "repeat-callers",
                    onChange = viewModel::setRepeatCallsRing,
                )
                if (screening.repeatCallsRing) {
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                        Text(
                            stringResource(R.string.repeat_within),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (minutes in Durations.REPEAT_WINDOW) {
                                FilterChip(
                                    selected = screening.repeatWindowMinutes == minutes,
                                    onClick = { viewModel.setRepeatWindowMinutes(minutes) },
                                    label = { Text(durationLabel(minutes)) },
                                )
                            }
                        }
                    }
                }
            }

            Section {
                SwitchRow(
                    title = stringResource(R.string.allow_list),
                    detail = stringResource(R.string.allow_list_detail),
                    checked = screening.allowListEnabled,
                    tag = "allow-list",
                    onChange = viewModel::setAllowListEnabled,
                )
                if (screening.allowListEnabled) {
                    val entries = allowed.orEmpty().filter { it.expiresAtMillis == null || it.expiresAtMillis > now }
                    if (allowed != null && entries.isEmpty()) {
                        Text(
                            stringResource(R.string.allow_list_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                    for (entry in entries) {
                        val display = numbers.parse(entry.numberRaw).display
                        ListItem(
                            headlineContent = { Text(display) },
                            supportingContent = {
                                Text(
                                    if (entry.expiresAtMillis == null) {
                                        stringResource(R.string.allow_always)
                                    } else {
                                        stringResource(R.string.allow_until, untilText(timeText, entry.expiresAtMillis, now))
                                    },
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.removeAllowed(entry.numberKey) }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = stringResource(R.string.remove_number, display),
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                    OutlinedButton(
                        onClick = { addingNumber = true },
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 12.dp).testTag("add-number"),
                    ) { Text(stringResource(R.string.add_number)) }
                }
            }
        }
    }

    if (choosingPause) {
        ChoiceDialog(
            title = stringResource(R.string.pause_for),
            choices = Durations.PAUSE,
            label = { durationLabel(it) },
            onChoose = { minutes ->
                viewModel.pauseFor(minutes)
                choosingPause = false
            },
            onDismiss = { choosingPause = false },
        )
    }
    if (addingNumber) {
        AllowNumberDialog(
            fixedNumber = null,
            parse = numbers::parse,
            onAllow = { number, minutes ->
                viewModel.allow(number, minutes)
                addingNumber = false
            },
            onDismiss = { addingNumber = false },
        )
    }
}

/** The time alone when the moment is today, the date and time otherwise. */
fun untilText(timeText: TimeText, atMillis: Long, nowMillis: Long): String =
    if (timeText.dateOf(atMillis) == timeText.dateOf(nowMillis)) timeText.time(atMillis) else timeText.dateAndTime(atMillis)

@Composable
fun RadioRow(text: String, selected: Boolean, tag: String? = null, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .then(if (tag != null) Modifier.testTag(tag) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
    }
}

/** A short list to pick one thing from. Picking closes it. */
@Composable
fun <T> ChoiceDialog(
    title: String,
    choices: List<T>,
    label: @Composable (T) -> String,
    onChoose: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.exposeTestTags(),
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                for (choice in choices) {
                    val text = label(choice)
                    Text(
                        text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button, onClick = { onChoose(choice) })
                            .padding(vertical = 14.dp)
                            .testTag("choice-$choice"),
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

/**
 * Adds a number to the allow list, for good or for a while. With [fixedNumber] the number
 * is already chosen (from the history) and only the duration is asked.
 */
@Composable
fun AllowNumberDialog(
    fixedNumber: PhoneNumber?,
    parse: (String) -> PhoneNumber,
    onAllow: (PhoneNumber, Int?) -> Unit,
    onDismiss: () -> Unit,
) {
    var typed by rememberSaveable { mutableStateOf("") }
    var minutes by rememberSaveable { mutableStateOf<Int?>(null) }
    var showError by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        modifier = Modifier.exposeTestTags(),
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.allow_number_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (fixedNumber != null) {
                    Text(fixedNumber.display, style = MaterialTheme.typography.titleMedium)
                } else {
                    OutlinedTextField(
                        value = typed,
                        onValueChange = {
                            typed = it
                            showError = false
                        },
                        label = { Text(stringResource(R.string.phone_number)) },
                        singleLine = true,
                        isError = showError,
                        supportingText = if (showError) {
                            { Text(stringResource(R.string.phone_number_invalid)) }
                        } else {
                            null
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("number-field"),
                    )
                }
                Column(Modifier.selectableGroup()) {
                    for (choice in Durations.ALLOW) {
                        val text = if (choice == null) {
                            stringResource(R.string.allow_always)
                        } else {
                            stringResource(R.string.allow_for, durationLabel(choice))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = minutes == choice, role = Role.RadioButton, onClick = { minutes = choice })
                                .padding(vertical = 10.dp)
                                .testTag("allow-for-${choice ?: "always"}"),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = minutes == choice, onClick = null)
                            Text(text, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTag("allow-confirm"),
                onClick = {
                    val number = fixedNumber ?: parse(typed)
                    // A real number has digits; anything else would never match a call.
                    if (number.key.none { it.isDigit() }) showError = true else onAllow(number, minutes)
                },
            ) { Text(stringResource(R.string.allow)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
