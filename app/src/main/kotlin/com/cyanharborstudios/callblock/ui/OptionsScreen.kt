package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.rules.Scope
import com.cyanharborstudios.callblock.data.AllowedNumberEntity
import com.cyanharborstudios.callblock.ui.parts.CapsText
import com.cyanharborstudios.callblock.ui.parts.ErrorLine
import com.cyanharborstudios.callblock.ui.parts.Header
import com.cyanharborstudios.callblock.ui.parts.KeyChoice
import com.cyanharborstudios.callblock.ui.parts.KeysBlock
import com.cyanharborstudios.callblock.ui.parts.LatchingKeys
import com.cyanharborstudios.callblock.ui.parts.RecessedField
import com.cyanharborstudios.callblock.ui.parts.Section
import com.cyanharborstudios.callblock.ui.parts.Sentence
import com.cyanharborstudios.callblock.ui.parts.Strip
import com.cyanharborstudios.callblock.ui.parts.SwitchboardIcons
import com.cyanharborstudios.callblock.ui.parts.Trail
import com.cyanharborstudios.callblock.ui.parts.pressTint
import com.cyanharborstudios.callblock.ui.parts.ruleBelow
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType

/** Who is filtered, and the ways a call can get through anyway. Pause lives on Home. */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun OptionsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allowed by viewModel.allowedNumbers.collectAsStateWithLifecycle()
    val now = rememberNowMillis()
    val timeText = rememberTimeText()
    val numbers = rememberPhoneNumbers()
    val colors = MaterialTheme.colorScheme
    val focus = LocalFocusManager.current
    var typed by rememberSaveable { mutableStateOf("") }
    var fieldOpen by rememberSaveable { mutableStateOf(false) }
    var invalid by rememberSaveable { mutableStateOf(false) }
    val keysIntoView = remember { BringIntoViewRequester() }
    // The keyboard's height, as it rises: the keys follow it up.
    val keyboardHeight = WindowInsets.ime.getBottom(LocalDensity.current)

    LaunchedEffect(Unit) { viewModel.removeExpiredAllowed() }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Box(Modifier.padding(horizontal = 16.dp)) { Header(stringResource(R.string.options), onBack) }
        val screening = settings?.screening ?: return
        Column(
            Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val international = screening.scope == Scope.INTERNATIONAL_ONLY
            Section(first = true) {
                CapsText(stringResource(R.string.scope_heading), SwitchboardType.strip, color = colors.onSurface)
                LatchingKeys(
                    choices = listOf(
                        KeyChoice(Scope.ALL_UNKNOWN, stringResource(R.string.scope_all_key)),
                        KeyChoice(Scope.INTERNATIONAL_ONLY, stringResource(R.string.scope_international_key)),
                    ),
                    selected = screening.scope,
                    onSelect = viewModel::setScope,
                    tag = { if (it == Scope.ALL_UNKNOWN) "scope-all" else "scope-international" },
                )
                Sentence(stringResource(if (international) R.string.scope_international else R.string.scope_all), SwitchboardType.body, color = colors.onSurfaceVariant)
            }

            // India's 140 series, the prototype's Later frame as drawn. Off as installed: the user
            // chooses to block promotional calls; the app never does it on its own (NOTES.md N-35).
            Section {
                Strip(
                    title = stringResource(R.string.india_140_title),
                    detail = stringResource(if (screening.promotionalSeriesBlocked) R.string.india_140_on else R.string.off),
                    trail = Trail.Switch,
                    checked = screening.promotionalSeriesBlocked,
                    onClick = { viewModel.setPromotionalSeriesBlocked(!screening.promotionalSeriesBlocked) },
                    head = true,
                    rule = false,
                    tag = "india-140",
                )
            }

            Section {
                CapsText(stringResource(R.string.repeat_callers), SwitchboardType.strip, color = colors.onSurface)
                Sentence(stringResource(R.string.repeat_callers_detail), SwitchboardType.body, color = colors.onSurfaceVariant)
                LatchingKeys(
                    choices = Durations.REPEAT_WINDOW.map { minutes ->
                        if (minutes == 0) KeyChoice(0, stringResource(R.string.key_off)) else KeyChoice(minutes, shortDurationLabel(minutes), durationLabel(minutes))
                    },
                    selected = if (screening.repeatCallsRing) screening.repeatWindowMinutes else 0,
                    onSelect = { minutes ->
                        if (minutes == 0) {
                            viewModel.setRepeatCallsRing(false)
                        } else {
                            viewModel.setRepeatWindowMinutes(minutes)
                            viewModel.setRepeatCallsRing(true)
                        }
                    },
                    tag = { "repeat-$it" },
                )
            }

            Section {
                Strip(
                    title = stringResource(R.string.allow_list),
                    detail = stringResource(if (screening.allowListEnabled) R.string.on else R.string.off),
                    trail = Trail.Switch,
                    checked = screening.allowListEnabled,
                    onClick = {
                        viewModel.setAllowListEnabled(!screening.allowListEnabled)
                        if (screening.allowListEnabled) {
                            typed = ""
                            invalid = false
                            fieldOpen = false
                        }
                    },
                    head = true,
                    rule = false,
                    tag = "allow-list",
                )
                if (screening.allowListEnabled) {
                    val entries = allowed.orEmpty().filter { it.expiresAtMillis == null || it.expiresAtMillis > now }
                    if (allowed != null && entries.isEmpty()) {
                        Text(stringResource(R.string.allow_list_empty), style = SwitchboardType.body, color = colors.onSurfaceVariant)
                    }
                    for (entry in entries) {
                        AllowedRow(entry, numbers.parse(entry.numberRaw).display, untilLine(entry, timeText, now)) { viewModel.removeAllowed(entry.numberKey) }
                    }
                    RecessedField(
                        value = typed,
                        onValueChange = {
                            typed = it
                            invalid = false
                            fieldOpen = true
                        },
                        placeholder = stringResource(R.string.phone_number),
                        spoken = stringResource(R.string.phone_number_to_allow),
                        error = invalid,
                        errorText = stringResource(R.string.phone_number_invalid),
                        tag = "number-field",
                        modifier = Modifier.onFocusChanged { if (it.isFocused) fieldOpen = true },
                    )
                    if (invalid) ErrorLine(stringResource(R.string.phone_number_invalid))
                    if (fieldOpen || typed.isNotEmpty() || invalid) {
                        // The keys sit above the keyboard, as the field does; the ad slot stays where it is.
                        LaunchedEffect(keyboardHeight) { if (keyboardHeight > 0) keysIntoView.bringIntoView() }
                        KeysBlock(
                            modifier = Modifier.bringIntoViewRequester(keysIntoView),
                            caption = stringResource(R.string.allow_caption),
                            choices = allowChoices(),
                            onChoose = { minutes ->
                                val number = numbers.parse(typed.trim())
                                // A real number has digits; anything else would never match a call.
                                if (number.key.none { it.isDigit() }) {
                                    invalid = true
                                } else {
                                    viewModel.allow(number, minutes)
                                    typed = ""
                                    invalid = false
                                    fieldOpen = false
                                    focus.clearFocus()
                                }
                            },
                            tag = { "allow-for-${it ?: "always"}" },
                        )
                    }
                }
            }
        }
    }
}

/** The three lengths an allow entry can have: an hour, a day, or always. */
@Composable
fun allowChoices(): List<KeyChoice<Int?>> = Durations.ALLOW.map { minutes ->
    if (minutes == null) KeyChoice(null, stringResource(R.string.allow_always)) else KeyChoice(minutes, shortDurationLabel(minutes), durationLabel(minutes))
}

/** "Always", or "Until 20:30" / "Until tomorrow, 10:05". */
@Composable
fun untilLine(entry: AllowedNumberEntity, timeText: com.cyanharborstudios.callblock.core.time.TimeText, now: Long): String =
    if (entry.expiresAtMillis == null) stringResource(R.string.allow_always) else stringResource(R.string.allow_until, untilText(timeText, entry.expiresAtMillis, now))

/** The number, then Always or Until and a time, and a 48 dp remove button. */
@Composable
private fun AllowedRow(entry: AllowedNumberEntity, display: String, detail: String, onRemove: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val removeLabel = stringResource(R.string.remove_number, display)
    Row(
        Modifier
            .fillMaxWidth()
            .ruleBelow(colors.outlineVariant)
            .defaultMinSize(minHeight = 56.dp)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(Modifier.weight(1f).semantics(mergeDescendants = true) { contentDescription = "$display, $detail" }) {
            Text(display, style = SwitchboardType.number, color = colors.onSurface, maxLines = 1)
            Text(detail, style = SwitchboardType.body, color = colors.onSurfaceVariant)
        }
        val interaction = remember { MutableInteractionSource() }
        Box(
            Modifier
                .offset(x = 12.dp)
                .size(48.dp)
                .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onRemove)
                .pressTint(interaction)
                .semantics { contentDescription = removeLabel },
            contentAlignment = Alignment.Center,
        ) {
            Icon(SwitchboardIcons.close, null, Modifier.size(20.dp), tint = colors.onSurfaceVariant)
        }
    }
}
