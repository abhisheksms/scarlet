package com.cyanharborstudios.callblock.ui.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.CollectionInfo
import com.cyanharborstudios.callblock.core.stats.Counts
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import kotlin.math.roundToInt

private val UnitShape = RoundedCornerShape(2.dp)

/**
 * Seven days, oldest first. Up to 8 calls a day each call is one unit: solid for
 * blocked, outlined for silenced. Past 8 the columns become bars to one scale with
 * the day's total above. A day is a 48 dp target; the selected day's label inverts.
 */
@Composable
fun DayChart(
    days: List<Counts>,
    labels: List<String>,
    descriptions: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    formatCount: (Int) -> String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val max = days.maxOf { it.total }
    val bricks = max <= 8
    val valueRow = with(LocalDensity.current) { if (bricks) 0.dp else (18 * fontScale).dp }
    Row(
        modifier
            .widenBy(4.dp)
            .semantics { collectionInfo = CollectionInfo(rowCount = 1, columnCount = days.size) }
            .drawBehind {
                val y = valueRow.toPx() + 101.dp.toPx()
                drawRect(colors.outline, Offset(4.dp.toPx(), y), Size(size.width - 8.dp.toPx(), 2.dp.toPx()))
            },
    ) {
        days.forEachIndexed { index, day ->
            val isSelected = index == selected
            Column(
                Modifier
                    .weight(1f)
                    .selectable(selected = isSelected, interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Button, onClick = { onSelect(index) })
                    .semantics { contentDescription = descriptions[index]; this.selected = isSelected }
                    .padding(start = 4.dp, end = 4.dp, bottom = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (bricks) {
                    Column(Modifier.fillMaxWidth().height(103.dp).padding(bottom = 2.dp), verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.Bottom)) {
                        repeat(day.silenced) { Box(Modifier.fillMaxWidth().height(10.dp).border(2.5.dp, colors.onSurface, UnitShape)) }
                        repeat(day.blocked) { Box(Modifier.fillMaxWidth().height(10.dp).background(colors.onSurface, UnitShape)) }
                    }
                } else {
                    Box(Modifier.height(valueRow).padding(bottom = 3.dp), contentAlignment = Alignment.BottomCenter) {
                        if (day.total > 0) Text(formatCount(day.total), style = SwitchboardType.chartValue, color = colors.onSurface)
                    }
                    val total = day.total
                    val height = if (max == 0) 0 else (total.toFloat() / max * 101).roundToInt()
                    val silencedHeight = if (day.silenced > 0) maxOf(5, (day.silenced.toFloat() / total * height).roundToInt()) else 0
                    val blockedHeight = maxOf(0, height - silencedHeight - if (day.silenced > 0) 1 else 0)
                    Column(Modifier.fillMaxWidth().height(103.dp).padding(bottom = 2.dp), verticalArrangement = Arrangement.Bottom) {
                        if (day.silenced > 0) {
                            Box(Modifier.fillMaxWidth().height(silencedHeight.dp).padding(bottom = 1.dp).border(2.5.dp, colors.onSurface, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)))
                        }
                        if (day.blocked > 0) Box(Modifier.fillMaxWidth().height(blockedHeight.dp).background(colors.onSurface, UnitShape))
                    }
                }
                Box(
                    Modifier.fillMaxWidth().padding(top = 6.dp).then(if (isSelected) Modifier.background(colors.primary, UnitShape) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    CapsText(
                        labels[index],
                        SwitchboardType.chartLabel,
                        color = if (isSelected) colors.onPrimary else colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

/** Seven bars from zero, each with its count above. Not selectable: the numbers are already written. */
@Composable
fun WeekdayChart(values: List<Int>, labels: List<String>, descriptions: List<String>, formatCount: (Int) -> String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val max = maxOf(4, values.max())
    val valueRow = with(LocalDensity.current) { (18 * fontScale).dp }
    Row(
        modifier
            .fillMaxWidth()
            .semantics { collectionInfo = CollectionInfo(rowCount = 1, columnCount = values.size) }
            .drawBehind {
                val y = valueRow.toPx() + 72.dp.toPx()
                drawRect(colors.outline, Offset(0f, y), Size(size.width, 2.dp.toPx()))
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        values.forEachIndexed { index, value ->
            Column(Modifier.weight(1f).semantics { contentDescription = descriptions[index] }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.height(valueRow).padding(bottom = 3.dp), contentAlignment = Alignment.BottomCenter) {
                    Text(formatCount(value), style = SwitchboardType.chartValue, color = colors.onSurface)
                }
                Column(Modifier.fillMaxWidth().height(74.dp).padding(bottom = 2.dp), verticalArrangement = Arrangement.Bottom) {
                    if (value > 0) {
                        val height = maxOf(2, (value.toFloat() / max * 72).roundToInt())
                        Box(Modifier.fillMaxWidth().height(height.dp).background(colors.onSurface, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)))
                    }
                }
                CapsText(labels[index], SwitchboardType.chartLabel, Modifier.padding(top = 6.dp), color = colors.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Visible, softWrap = false)
            }
        }
    }
}

/**
 * 24 bars from zero. Touch or drag anywhere on it to read an hour; a tap on the hour
 * already chosen lets it go.
 */
@Composable
fun HourChart(
    values: List<Int>,
    descriptions: List<String>,
    axisLabels: List<String>,
    selected: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val max = maxOf(4, values.max())
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(76.dp)
                .semantics { collectionInfo = CollectionInfo(rowCount = 1, columnCount = values.size) }
                .drawBehind { drawRect(colors.outline, Offset(0f, size.height - 2.dp.toPx()), Size(size.width, 2.dp.toPx())) }
                .pointerInput(selected) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val start = selected
                        fun hourAt(x: Float) = (x / size.width * 24).toInt().coerceIn(0, 23)
                        val first = hourAt(down.position.x)
                        var moved = false
                        onSelect(first)
                        drag(down.id) { change ->
                            val hour = hourAt(change.position.x)
                            if (hour != first) moved = true
                            onSelect(hour)
                            change.consume()
                        }
                        if (!moved && start == first) onSelect(null)
                    }
                }
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            values.forEachIndexed { index, value ->
                val isSelected = index == selected
                Column(
                    Modifier
                        .weight(1f)
                        .height(74.dp)
                        .then(if (isSelected) Modifier.background(colors.outlineVariant) else Modifier)
                        .semantics { contentDescription = descriptions[index]; this.selected = isSelected },
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    if (value > 0) {
                        val height = maxOf(2, (value.toFloat() / max * 72).roundToInt())
                        Box(Modifier.fillMaxWidth().height(height.dp).background(colors.onSurface, RoundedCornerShape(topStart = 1.dp, topEnd = 1.dp)))
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp).clearAndSetSemantics { }) {
            for (label in axisLabels) {
                CapsText(label, SwitchboardType.chartLabel, Modifier.weight(1f), color = colors.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}
