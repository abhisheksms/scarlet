package com.cyanharborstudios.callblock.ui.parts

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import com.cyanharborstudios.callblock.ui.theme.switchboard
import java.time.DayOfWeek

/** What one hour of a row shows. */
sealed interface HourCell {
    /** Nothing is set: the lever decides. */
    data object Empty : HourCell

    data class Set(val mode: Mode) : HourCell

    /** In the every-day row: the days differ at this hour. */
    data object Mixed : HourCell
}

/** One row of the week grid: the days it stands for and what each of its 24 hours shows. */
data class WeekRow(
    val tag: String,
    val label: String,
    /** What a screen reader says for the row: the day and its set hours. */
    val spoken: String,
    val days: List<DayOfWeek>,
    val cells: List<HourCell>,
)

private val LabelWidth = 44.dp
private val LabelGap = 8.dp
private val TrackHeight = 44.dp
private val TrackInset = 3.dp

/**
 * The week as rows of 24 hours, like the hour chart turned into something you set. Drag
 * across a row to mark a span of hours, tap for one hour. A sideways drag only: an up or
 * down drag is the page scrolling. The grid reports what was touched; the screen decides
 * what that sets.
 *
 * A blocked hour is a solid unit, a silenced one an outlined unit, an hour set to Off a
 * small ring, as the lever's Off stop is. No hour is told by colour.
 */
@Composable
fun WeekGrid(
    rows: List<WeekRow>,
    axisLabels: List<String>,
    /** The two things a screen reader can do to a whole row, named for the brush in hand. */
    setWholeDayLabel: String,
    clearDayLabel: String,
    /** A drag in progress covers [hours] of [row]. */
    onDrag: (row: WeekRow, hours: IntRange) -> Unit,
    /** The drag ended. False when it was cancelled and nothing should be kept. */
    onDragEnd: (keep: Boolean) -> Unit,
    onTap: (row: WeekRow, hour: Int) -> Unit,
    onSetWholeDay: (row: WeekRow) -> Unit,
    onClearDay: (row: WeekRow) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extra = switchboard
    // A row's gesture code starts at its first touch and then runs for as long as the grid
    // is on screen. It must call the callbacks as they are now, not as they were then: they
    // carry the schedule as it stands, and an old one would undo what was set since.
    val onDragNow by rememberUpdatedState(onDrag)
    val onDragEndNow by rememberUpdatedState(onDragEnd)
    val onTapNow by rememberUpdatedState(onTap)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth().clearAndSetSemantics { }) {
            Spacer(Modifier.width(LabelWidth + LabelGap + TrackInset))
            for (label in axisLabels) {
                CapsText(label, SwitchboardType.chartLabel, Modifier.weight(1f), color = colors.onSurfaceVariant, maxLines = 1)
            }
        }
        for (row in rows) {
            val currentRow by rememberUpdatedState(row)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CapsText(row.label, SwitchboardType.caption, Modifier.width(LabelWidth).clearAndSetSemantics { }, color = colors.onSurface, maxLines = 1)
                Spacer(Modifier.width(LabelGap))
                Box(
                    Modifier
                        .weight(1f)
                        .height(TrackHeight)
                        .testTag(row.tag)
                        .semantics {
                            contentDescription = row.spoken
                            customActions = listOf(
                                CustomAccessibilityAction(setWholeDayLabel) { onSetWholeDay(currentRow); true },
                                CustomAccessibilityAction(clearDayLabel) { onClearDay(currentRow); true },
                            )
                        }
                        .pointerInput(row.tag) {
                            val inset = TrackInset.toPx()
                            fun hourAt(x: Float): Int = ((x - inset) / (size.width - 2 * inset) * 24).toInt().coerceIn(0, 23)
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val first = hourAt(down.position.x)
                                val drag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
                                if (drag != null) {
                                    fun report(x: Float) {
                                        val hour = hourAt(x)
                                        onDragNow(currentRow, minOf(first, hour)..maxOf(first, hour))
                                    }
                                    report(drag.position.x)
                                    val lifted = horizontalDrag(drag.id) { change ->
                                        report(change.position.x)
                                        change.consume()
                                    }
                                    onDragEndNow(lifted)
                                } else {
                                    // No sideways drag. A finger that simply lifted was a tap; one still down is the page scrolling.
                                    val last = currentEvent.changes.firstOrNull { it.id == down.id }
                                    if (last != null && !last.pressed) onTapNow(currentRow, first)
                                }
                            }
                        }
                        .drawBehind {
                            val corner = CornerRadius(4.dp.toPx())
                            // A recess, as the switch's slot is: a highlight under it and a dark line along its top.
                            drawRoundRect(colors.surfaceContainerHighest, Offset(0f, 1.dp.toPx()), size, corner)
                            drawRoundRect(colors.inverseSurface, Offset.Zero, size, corner)
                            drawRect(Color.Black.copy(alpha = 0.6f), Offset(2.dp.toPx(), 0f), Size(size.width - 4.dp.toPx(), 2.dp.toPx()))

                            val inset = TrackInset.toPx()
                            val cellWidth = (size.width - 2 * inset) / 24
                            val gap = 1.dp.toPx()
                            val top = 8.dp.toPx()
                            val unitHeight = size.height - 2 * top
                            val line = 1.5.dp.toPx()
                            row.cells.forEachIndexed { hour, cell ->
                                val left = inset + hour * cellWidth + gap / 2
                                val unitWidth = cellWidth - gap
                                val centre = Offset(left + unitWidth / 2, size.height / 2)
                                when (cell) {
                                    HourCell.Empty -> Unit
                                    HourCell.Mixed -> drawCircle(extra.inverseOnSurfaceVariant, 1.5.dp.toPx(), centre)
                                    is HourCell.Set -> when (cell.mode) {
                                        Mode.BLOCK -> drawRect(colors.inverseOnSurface, Offset(left, top), Size(unitWidth, unitHeight))
                                        Mode.SILENCE -> drawRect(
                                            colors.inverseOnSurface,
                                            Offset(left + line / 2, top + line / 2),
                                            Size(unitWidth - line, unitHeight - line),
                                            style = Stroke(line),
                                        )
                                        Mode.OFF -> drawCircle(colors.inverseOnSurface, (unitWidth - line) / 2, centre, style = Stroke(line))
                                    }
                                }
                            }
                            // A faint line between hours, so the row reads as twenty-four of them.
                            for (hour in 1..23) {
                                drawRect(extra.inverseOutlineVariant, Offset(inset + hour * cellWidth - gap / 2, top), Size(gap, unitHeight))
                            }
                            // A mark at six, noon and six again, under the labels.
                            for (hour in listOf(6, 12, 18)) {
                                drawRect(extra.inverseOnSurfaceVariant, Offset(inset + hour * cellWidth - gap / 2, size.height - 5.dp.toPx()), Size(gap, 3.dp.toPx()))
                            }
                        },
                )
            }
        }
    }
}
