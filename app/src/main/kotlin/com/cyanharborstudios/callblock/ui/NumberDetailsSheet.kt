package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.numbers.PhoneNumber
import com.cyanharborstudios.callblock.core.rules.Action
import com.cyanharborstudios.callblock.core.stats.Statistics
import com.cyanharborstudios.callblock.core.time.TimeText
import com.cyanharborstudios.callblock.data.AllowedNumberEntity
import com.cyanharborstudios.callblock.data.HandledCallEntity
import com.cyanharborstudios.callblock.ui.parts.CapsText
import com.cyanharborstudios.callblock.ui.parts.DottedParts
import com.cyanharborstudios.callblock.ui.parts.Key
import com.cyanharborstudios.callblock.ui.parts.KeyStrip
import com.cyanharborstudios.callblock.ui.parts.KeysBlock
import com.cyanharborstudios.callblock.ui.parts.MarkLine
import com.cyanharborstudios.callblock.ui.parts.Sheet
import com.cyanharborstudios.callblock.ui.parts.engravedTop
import com.cyanharborstudios.callblock.ui.parts.ruleBelow
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType

/**
 * Everything the app knows about one number, on a sheet: the counts, first and last
 * time, and the choice to let it through. Opened from a history row it also names
 * that call, with the one way to delete it, away from the allow keys.
 */
@Composable
fun NumberDetailsSheet(
    number: PhoneNumber,
    calls: List<HandledCallEntity>,
    thisCall: HandledCallEntity?,
    allowEntry: AllowedNumberEntity?,
    timeText: TimeText,
    now: Long,
    formatCount: (Int) -> String,
    onAllow: (minutes: Int?) -> Unit,
    onRemoveAllowed: () -> Unit,
    onDeleteCall: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val display = number.display.ifEmpty { stringResource(R.string.no_number) }
    val details = remember(calls, number.key, thisCall) {
        if (number.key.isNotEmpty()) {
            Statistics.detailsFor(number.key, calls.map { it.toHandledCall() })
        } else {
            null
        }
    }
    val total = details?.counts?.total ?: 1
    val blocked = details?.counts?.blocked ?: if (thisCall?.action == Action.BLOCK.name) 1 else 0
    val silenced = details?.counts?.silenced ?: if (thisCall?.action == Action.SILENCE.name) 1 else 0

    Sheet(label = display, onDismiss = onDismiss, tag = "number-details") {
        Text(display, style = SwitchboardType.display, color = colors.onSurface, maxLines = 1, modifier = Modifier.semantics { heading() })

        if (thisCall != null && onDeleteCall != null) {
            val day = dayHeading(timeText.dateOf(thisCall.atMillis), timeText.dateOf(now), timeText)
            val outcome = stringResource(if (thisCall.action == Action.SILENCE.name) R.string.silenced else R.string.blocked)
            val when_ = "$day, ${timeText.time(thisCall.atMillis)}"
            val caption = stringResource(R.string.this_call)
            Row(
                Modifier
                    .fillMaxWidth()
                    .engravedTop(colors.outlineVariant, colors.surfaceContainerHigh)
                    .ruleBelow(colors.outlineVariant)
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .semantics(mergeDescendants = true) {
                            testTag = "this-call"
                            contentDescription = "$caption: $when_, $outcome"
                        },
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    CapsText(caption, SwitchboardType.caption, color = colors.onSurfaceVariant)
                    DottedParts(listOf(when_, outcome), SwitchboardType.body, color = colors.onSurface)
                }
                Key(
                    stringResource(R.string.delete),
                    onClick = {
                        onDeleteCall()
                        close()
                    },
                    spoken = stringResource(R.string.delete_this_call),
                    onPlate = true,
                    fillWidth = false,
                    horizontalPadding = 14.dp,
                    tag = "delete-call",
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val stopped = pluralStringResource(R.plurals.calls_stopped, total, formatCount(total))
            val blockedText = stringResource(R.string.n_blocked, formatCount(blocked))
            val silencedText = stringResource(R.string.n_silenced, formatCount(silenced))
            Column(Modifier.semantics(mergeDescendants = true) { contentDescription = "$stopped. $blockedText, $silencedText." }, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stopped, style = SwitchboardType.leadStrong, color = colors.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    MarkLine(true, blockedText, SwitchboardType.lead, color = colors.onSurface)
                    MarkLine(false, silencedText, SwitchboardType.lead, color = colors.onSurface)
                }
            }
            if (details != null) {
                if (total > 1) {
                    FactRow(stringResource(R.string.first_handled), timeText.dateAndTime(details.firstHandledAtMillis), "first-handled")
                    FactRow(stringResource(R.string.last_handled), timeText.dateAndTime(details.lastHandledAtMillis), "last-handled")
                } else if (thisCall == null) {
                    FactRow(stringResource(R.string.last_handled), timeText.dateAndTime(details.lastHandledAtMillis), "last-handled")
                }
            }
        }

        if (number.key.isNotEmpty()) {
            if (allowEntry != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val caption = stringResource(R.string.on_allow_list)
                    val length = untilLine(allowEntry, timeText, now)
                    Column(Modifier.semantics(mergeDescendants = true) { contentDescription = "$caption, $length" }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CapsText(caption, SwitchboardType.caption, color = colors.onSurfaceVariant)
                        Text(length, style = SwitchboardType.leadStrong, color = colors.onSurface)
                    }
                    KeyStrip {
                        Key(
                            stringResource(R.string.remove_from_allow_list),
                            onClick = {
                                onRemoveAllowed()
                                close()
                            },
                            onPlate = true,
                            tag = "remove-from-allow-list",
                        )
                    }
                }
            } else {
                KeysBlock(
                    caption = stringResource(R.string.allow_caption),
                    choices = allowChoices(),
                    onChoose = { minutes ->
                        onAllow(minutes)
                        close()
                    },
                    onPlate = true,
                    tag = { "allow-for-${it ?: "always"}" },
                )
            }
        }
    }
}

/** A label on the left, its value on the right, read as one phrase. */
@Composable
private fun FactRow(label: String, value: String, tag: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().clearAndSetSemantics {
            testTag = tag
            contentDescription = "$label: $value."
        },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(label, style = SwitchboardType.body.copy(lineHeight = SwitchboardType.lead.lineHeight), color = colors.onSurfaceVariant)
        Text(value, style = SwitchboardType.body.copy(lineHeight = SwitchboardType.lead.lineHeight), color = colors.onSurface, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}
