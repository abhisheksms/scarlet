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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.rules.Action
import com.cyanharborstudios.callblock.core.time.TimeText
import com.cyanharborstudios.callblock.data.AllowedNumberEntity
import com.cyanharborstudios.callblock.data.HandledCallEntity
import com.cyanharborstudios.callblock.ui.parts.CapsText
import com.cyanharborstudios.callblock.ui.parts.ConfirmDialog
import com.cyanharborstudios.callblock.ui.parts.DottedParts
import com.cyanharborstudios.callblock.ui.parts.Header
import com.cyanharborstudios.callblock.ui.parts.HeaderTextButton
import com.cyanharborstudios.callblock.ui.parts.Mark
import com.cyanharborstudios.callblock.ui.parts.pressTint
import com.cyanharborstudios.callblock.ui.parts.ruleBelow
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import java.time.LocalDate
import java.time.ZoneId

/** Every call the app blocked or silenced, newest first, grouped by day. A row opens the number's details. */
@Composable
fun HistoryScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val calls by viewModel.handledCalls.collectAsStateWithLifecycle()
    val allowed by viewModel.allowedNumbers.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val now = rememberNowMillis()
    val timeText = rememberTimeText()
    val numbers = rememberPhoneNumbers()
    val formatCount = rememberCountFormat()
    val colors = MaterialTheme.colorScheme

    var confirmingDeleteAll by rememberSaveable { mutableStateOf(false) }
    var openCall by remember { mutableStateOf<HandledCallEntity?>(null) }

    val liveAllowed = liveAllowEntries(allowed, settings?.screening?.allowListEnabled == true, now)

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Box(Modifier.padding(horizontal = 16.dp)) {
            Header(
                stringResource(R.string.history),
                onBack,
                action = if (!calls.isNullOrEmpty()) {
                    { HeaderTextButton(stringResource(R.string.delete_all), onClick = { confirmingDeleteAll = true }, tag = "delete-all") }
                } else {
                    null
                },
            )
        }
        val loaded = calls ?: return
        if (loaded.isEmpty()) {
            Text(
                stringResource(R.string.history_empty),
                style = SwitchboardType.empty,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
            )
            return
        }
        val today = timeText.dateOf(now)
        val byDay = remember(loaded, timeText) { loaded.groupBy { timeText.dateOf(it.atMillis) } }
        // Which call of the day each one is, for a number that calls more than once.
        val nth = remember(byDay) {
            val out = HashMap<Long, Int>()
            for ((_, callsThatDay) in byDay) {
                val seen = HashMap<String, Int>()
                for (call in callsThatDay.asReversed()) {
                    if (call.numberKey.isEmpty()) continue
                    val count = (seen[call.numberKey] ?: 0) + 1
                    seen[call.numberKey] = count
                    out[call.id] = count
                }
            }
            out
        }
        val timeWidth = rememberTimeColumnWidth(timeText)

        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("history-list"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        ) {
            byDay.entries.forEachIndexed { groupIndex, (day, callsThatDay) ->
                item(key = "day-$day") {
                    CapsText(
                        dayHeading(day, today, timeText),
                        SwitchboardType.caption,
                        Modifier
                            .fillMaxWidth()
                            .ruleBelow(colors.outlineVariant)
                            .padding(top = if (groupIndex == 0) 2.dp else 14.dp, bottom = 6.dp)
                            .semantics { heading() },
                        color = colors.onSurfaceVariant,
                    )
                }
                items(callsThatDay, key = { it.id }) { call ->
                    val number = numbers.parse(call.numberRaw)
                    HistoryRow(
                        call = call,
                        display = number.display.ifEmpty { stringResource(R.string.no_number) },
                        time = timeText.time(call.atMillis),
                        timeWidth = timeWidth,
                        nth = nth[call.id] ?: 1,
                        allowLine = liveAllowed[call.numberKey]?.let { allowLine(it, timeText, now) },
                        onOpen = { openCall = call },
                    )
                }
            }
        }
    }

    openCall?.let { call ->
        val number = numbers.parse(call.numberRaw)
        NumberDetailsSheet(
            number = number,
            calls = calls.orEmpty(),
            thisCall = call,
            allowEntry = liveAllowed[number.key],
            timeText = timeText,
            now = now,
            formatCount = formatCount,
            onAllow = { minutes -> viewModel.allow(number, minutes) },
            onRemoveAllowed = { viewModel.removeAllowed(number.key) },
            onDeleteCall = { viewModel.deleteHandledCall(call.id) },
            onDismiss = { openCall = null },
        )
    }

    if (confirmingDeleteAll) {
        ConfirmDialog(
            question = stringResource(R.string.delete_all_title),
            body = stringResource(R.string.delete_all_text),
            cancelLabel = stringResource(R.string.cancel),
            confirmLabel = stringResource(R.string.delete_all),
            onCancel = { confirmingDeleteAll = false },
            onConfirm = {
                viewModel.deleteAllHandledCalls()
                confirmingDeleteAll = false
            },
            confirmTag = "delete-all-confirm",
        )
    }
}

/** The allow list's live entries by number key, or nothing while the list is switched off. */
fun liveAllowEntries(allowed: List<AllowedNumberEntity>?, enabled: Boolean, now: Long): Map<String, AllowedNumberEntity> =
    if (!enabled) emptyMap() else allowed.orEmpty().filter { it.expiresAtMillis == null || it.expiresAtMillis > now }.associateBy { it.numberKey }

/** "On the allow list", or "Rings until 20:30". */
@Composable
fun allowLine(entry: AllowedNumberEntity, timeText: TimeText, now: Long): String =
    if (entry.expiresAtMillis == null) stringResource(R.string.on_allow_list) else stringResource(R.string.rings_until, untilText(timeText, entry.expiresAtMillis, now))

/** The widest a time can be in the phone's clock setting, so every row's number starts on the same line. */
@Composable
private fun rememberTimeColumnWidth(timeText: TimeText): androidx.compose.ui.unit.Dp {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val style = SwitchboardType.lead.copy(lineHeight = SwitchboardType.number.lineHeight)
    return remember(timeText, density) {
        val zone = ZoneId.systemDefault()
        val samples = listOf(10 to 48, 22 to 48).map { (hour, minute) ->
            LocalDate.now(zone).atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()
        }
        val widest = samples.maxOf { measurer.measure(timeText.time(it), style).size.width }
        with(density) { widest.toDp() }
    }
}

/** Time, number, then the outcome as a mark and a word. The whole row is the target. */
@Composable
private fun HistoryRow(
    call: HandledCallEntity,
    display: String,
    time: String,
    timeWidth: androidx.compose.ui.unit.Dp,
    nth: Int,
    allowLine: String?,
    onOpen: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val blocked = call.action != Action.SILENCE.name
    val outcome = stringResource(if (blocked) R.string.blocked else R.string.silenced)
    val parts = buildList {
        add(outcome)
        if (nth > 1) add(stringResource(R.string.nth_call, ordinal(nth)))
    }
    val spoken = buildString {
        append(time).append(", ").append(display).append(", ").append(outcome)
        if (nth > 1) append(", ").append(stringResource(R.string.nth_call_spoken, ordinal(nth)))
        if (allowLine != null) append(", ").append(allowLine.replaceFirstChar { it.lowercase() })
    }
    val interaction = remember { MutableInteractionSource() }
    Row(
        Modifier
            .testTag("history-row")
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onOpen)
            .pressTint(interaction)
            .semantics(mergeDescendants = true) { contentDescription = spoken }
            .ruleBelow(colors.outlineVariant)
            .defaultMinSize(minHeight = 56.dp)
            .padding(top = 9.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            time,
            style = SwitchboardType.lead.copy(lineHeight = SwitchboardType.number.lineHeight),
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.width(timeWidth),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(display, style = SwitchboardType.number, color = colors.onSurface, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Mark(blocked, color = colors.onSurfaceVariant)
                DottedParts(parts, SwitchboardType.body.copy(lineHeight = SwitchboardType.lead.lineHeight), color = colors.onSurfaceVariant)
            }
            if (allowLine != null) {
                Text(allowLine, style = SwitchboardType.body.copy(lineHeight = SwitchboardType.lead.lineHeight, fontWeight = androidx.compose.ui.text.font.FontWeight.W500), color = colors.onSurface)
            }
        }
    }
}

@Composable
fun dayHeading(day: LocalDate, today: LocalDate, timeText: TimeText): String = when (day) {
    today -> stringResource(R.string.today)
    today.minusDays(1) -> stringResource(R.string.yesterday)
    else -> timeText.date(day)
}
