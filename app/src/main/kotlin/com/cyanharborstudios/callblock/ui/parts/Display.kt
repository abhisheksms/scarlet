package com.cyanharborstudios.callblock.ui.parts

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.ui.theme.LocalReducedMotion
import com.cyanharborstudios.callblock.ui.theme.Motion
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import com.cyanharborstudios.callblock.ui.theme.duration
import com.cyanharborstudios.callblock.ui.theme.switchboard

/** What the display window says, chosen in the order: cannot screen, role missing, paused, the mode. */
sealed interface HomeStatus {
    /** Off, and nothing handled yet: the two modes are explained under the sentence. */
    data class First(val international: Boolean) : HomeStatus

    /**
     * The mode in effect. [note] is the second line, there while a timer or the schedule is
     * in charge: until when, and what follows.
     */
    data class Off(val note: String? = null) : HomeStatus
    data class Silence(val international: Boolean, val note: String? = null) : HomeStatus
    data class Block(val international: Boolean, val note: String? = null) : HomeStatus

    /** A timer at Off. [note] says what follows it. */
    data class Paused(val until: String, val note: String? = null) : HomeStatus
    data object RoleMissing : HomeStatus
    data object Cannot : HomeStatus
}

/** The display's first line for a status: the state in effect, as a word or two, not a sentence. */
@Composable
fun statusSentence(status: HomeStatus): String = when (status) {
    is HomeStatus.First, is HomeStatus.Off -> stringResource(R.string.status_off)
    is HomeStatus.Silence -> stringResource(if (status.international) R.string.status_silence_international else R.string.status_silence)
    is HomeStatus.Block -> stringResource(if (status.international) R.string.status_block_international else R.string.status_block)
    is HomeStatus.Paused -> stringResource(R.string.paused_until, status.until)
    HomeStatus.RoleMissing -> stringResource(R.string.role_missing)
    HomeStatus.Cannot -> stringResource(R.string.role_unavailable)
}

/** The live counts the two entry tiles carry. */
data class TileCounts(val today: Int, val weekBlocked: Int, val weekSilenced: Int, val total: Int)

/**
 * The dark display window at the top of Home: one sentence, the gear, and two entry
 * tiles. Its height is the tallest of its possible states, so nothing below it moves.
 */
@Composable
fun DisplayWindow(
    status: HomeStatus,
    candidates: List<HomeStatus>,
    counts: TileCounts,
    swapDelay: Int,
    onOpenHistory: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenSettings: () -> Unit,
    formatCount: (Int) -> String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val highlight = colors.surfaceContainerHighest
    val reduced = LocalReducedMotion.current
    Box(
        modifier
            .fillMaxWidth()
            .drawBehind { drawRoundRect(highlight, Offset(0f, 1.dp.toPx()), size, androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())) }
            .background(colors.inverseSurface, PlateShape)
            .drawBehind { drawRect(Color.Black, Offset(6.dp.toPx(), 0f), Size(size.width - 12.dp.toPx(), 2.dp.toPx())) },
    ) {
        TallestOf(
            candidates = candidates.map { candidate -> { Column { StatusContent(candidate); EntryTiles(counts, {}, {}, formatCount) } } },
        ) {
            Column(Modifier.fillMaxWidth()) {
                AnimatedContent(
                    targetState = status,
                    transitionSpec = {
                        val swap = duration(Motion.DISPLAY_SWAP, reduced)
                        val delay = duration(swapDelay, reduced)
                        fadeIn(tween(swap, delayMillis = delay, easing = Motion.linear)) togetherWith fadeOut(tween(swap, delayMillis = delay, easing = Motion.linear))
                    },
                    contentAlignment = Alignment.TopStart,
                    label = "status",
                ) { shown -> StatusContent(shown, Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
                Spacer(Modifier.weight(1f))
                EntryTiles(counts, onOpenHistory, onOpenStatistics, formatCount)
            }
        }
        val gearLabel = stringResource(R.string.settings)
        val interaction = remember { MutableInteractionSource() }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 2.dp, end = 2.dp)
                .size(48.dp)
                .testTag("open-settings")
                .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onOpenSettings),
            contentAlignment = Alignment.Center,
        ) {
            Icon(SwitchboardIcons.gear, gearLabel, Modifier.size(22.dp), tint = switchboard.inverseOnSurfaceVariant)
        }
    }
}

@Composable
private fun StatusContent(status: HomeStatus, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val extra = switchboard
    val sentence = statusSentence(status)
    val secondLine = when (status) {
        HomeStatus.RoleMissing -> stringResource(R.string.role_missing_cause)
        is HomeStatus.Off -> status.note
        is HomeStatus.Silence -> status.note
        is HomeStatus.Block -> status.note
        is HomeStatus.Paused -> status.note
        else -> null
    }
    Column(
        modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 18.dp, end = 16.dp, bottom = if (status is HomeStatus.First) 12.dp else 16.dp),
    ) {
        Row(Modifier.padding(end = 40.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            when (status) {
                is HomeStatus.Paused -> Row(Modifier.padding(top = (6 * LocalDensity.current.fontScale).dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(2) { Box(Modifier.size(5.dp, (15 * LocalDensity.current.fontScale).dp).background(colors.inverseOnSurface)) }
                }
                HomeStatus.RoleMissing -> Box(Modifier.padding(top = (7 * LocalDensity.current.fontScale).dp).size(14.dp).background(extra.attention))
                else -> Unit
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(sentence, style = SwitchboardType.display, color = colors.inverseOnSurface)
                if (secondLine != null) {
                    Text(secondLine, style = SwitchboardType.lead.copy(lineBreak = SwitchboardType.display.lineBreak), color = extra.inverseOnSurfaceVariant)
                }
            }
        }
        if (status is HomeStatus.First) {
            Column(
                Modifier
                    .padding(top = 10.dp)
                    .drawBehind { drawRect(extra.inverseOutlineVariant, Offset.Zero, Size(size.width, 1.dp.toPx())) }
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LegendLine(stringResource(R.string.mode_silence), stringResource(if (status.international) R.string.legend_silence_international else R.string.legend_silence))
                LegendLine(stringResource(R.string.mode_block), stringResource(if (status.international) R.string.legend_block_international else R.string.legend_block))
            }
        }
    }
}

/** "SILENCE  Unknown numbers don’t ring.": the mode's name in capitals, then what it does. */
@Composable
private fun LegendLine(label: String, text: String) {
    val colors = MaterialTheme.colorScheme
    val extra = switchboard
    val line = buildAnnotatedString {
        withStyle(SwitchboardType.caption.toSpanStyle().copy(color = colors.inverseOnSurface)) { append(label.uppercase()) }
        append("  ")
        append(tieWidows(text))
    }
    Text(line, style = SwitchboardType.body, color = extra.inverseOnSurfaceVariant)
}

/** Two ways into History and Statistics that carry live counts, side by side under the sentence. */
@Composable
private fun EntryTiles(counts: TileCounts, onOpenHistory: () -> Unit, onOpenStatistics: () -> Unit, formatCount: (Int) -> String) {
    val colors = MaterialTheme.colorScheme
    val extra = switchboard
    val empty = counts.total == 0 && counts.today == 0
    val historyDetail = if (counts.today == 0) {
        stringResource(R.string.history_none_today)
    } else {
        androidx.compose.ui.res.pluralStringResource(R.plurals.history_today, counts.today, formatCount(counts.today))
    }
    val weekTotal = counts.weekBlocked + counts.weekSilenced
    val statisticsDetail = when {
        counts.total == 0 -> stringResource(R.string.statistics_nothing_yet)
        weekTotal == 0 -> stringResource(R.string.statistics_nothing_in_week)
        else -> stringResource(R.string.statistics_week, formatCount(counts.weekBlocked), formatCount(counts.weekSilenced))
    }
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .drawBehind { drawRect(extra.inverseOutlineVariant, Offset.Zero, Size(size.width, 1.dp.toPx())) },
    ) {
        Tile(SwitchboardIcons.clock, stringResource(R.string.history), historyDetail, empty, onOpenHistory, "open-history", Modifier.weight(1f)) {
            if (counts.today == 0) {
                Text(stringResource(R.string.history_none_today), style = SwitchboardType.lead, color = colors.inverseOnSurface)
            } else {
                Text(formatCount(counts.today), style = SwitchboardType.numeral, color = colors.inverseOnSurface)
                Text(
                    androidx.compose.ui.res.pluralStringResource(R.plurals.calls_today_unit, counts.today),
                    style = SwitchboardType.body.copy(lineHeight = androidx.compose.ui.unit.TextUnit(18f, androidx.compose.ui.unit.TextUnitType.Sp)),
                    color = extra.inverseOnSurfaceVariant,
                )
            }
        }
        Box(Modifier.width(1.dp).fillMaxHeight().background(extra.inverseOutlineVariant))
        Tile(SwitchboardIcons.bars, stringResource(R.string.statistics), statisticsDetail, empty, onOpenStatistics, "open-statistics", Modifier.weight(1f)) {
            when {
                counts.total == 0 -> Text(stringResource(R.string.statistics_nothing_yet), style = SwitchboardType.lead, color = colors.inverseOnSurface)
                weekTotal == 0 -> Text(stringResource(R.string.statistics_nothing_in_week), style = SwitchboardType.lead, color = colors.inverseOnSurface)
                else -> {
                    MarkLine(true, stringResource(R.string.n_blocked, formatCount(counts.weekBlocked)), SwitchboardType.lead, color = colors.inverseOnSurface)
                    MarkLine(false, stringResource(R.string.n_silenced, formatCount(counts.weekSilenced)), SwitchboardType.lead, color = colors.inverseOnSurface)
                    Text(
                        stringResource(R.string.in_seven_days),
                        style = SwitchboardType.body.copy(lineHeight = androidx.compose.ui.unit.TextUnit(18f, androidx.compose.ui.unit.TextUnitType.Sp)),
                        color = extra.inverseOnSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun Tile(
    icon: ImageVector,
    caption: String,
    spokenDetail: String,
    empty: Boolean,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val extra = switchboard
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Column(
        modifier
            .testTag(tag)
            .fillMaxHeight()
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = "$caption, $spokenDetail" }
            .then(if (pressed) Modifier.background(Color(0x14F0EDE4)) else Modifier)
            .defaultMinSize(minHeight = if (empty) 60.dp else 96.dp)
            .padding(start = 16.dp, top = if (empty) 10.dp else 12.dp, end = 8.dp, bottom = if (empty) 12.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(if (empty) 3.dp else 2.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(bottom = if (empty) 0.dp else 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, Modifier.size(15.dp), tint = extra.inverseOnSurfaceVariant)
                CapsText(caption, SwitchboardType.caption, color = extra.inverseOnSurfaceVariant)
            }
            Icon(SwitchboardIcons.chevron, null, Modifier.size(16.dp), tint = extra.inverseOnSurfaceVariant)
        }
        content()
    }
}
