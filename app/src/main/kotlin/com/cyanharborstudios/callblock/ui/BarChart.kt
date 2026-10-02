package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** One bar: its stacked parts from the bottom up, and what a screen reader says for it. */
data class Bar(val segments: List<Segment>, val description: String) {
    val total: Int get() = segments.sumOf { it.value }
}

data class Segment(val value: Int, val color: Color)

/**
 * A plain bar chart built from boxes, so every bar is a real element: it can be tapped
 * to select it and TalkBack can read it. Bars share one scale; an empty bar shows a
 * hairline so the axis is still visible.
 */
@Composable
fun BarChart(
    bars: List<Bar>,
    selected: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    gap: Dp = 4.dp,
) {
    val tallest = bars.maxOfOrNull { it.total } ?: 0
    Row(
        modifier = modifier.fillMaxWidth().height(height),
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        bars.forEachIndexed { index, bar ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .alpha(if (selected == null || selected == index) 1f else 0.45f)
                    .selectable(selected = selected == index, onClick = { onSelect(index) })
                    .clearAndSetSemantics { contentDescription = bar.description },
                verticalArrangement = Arrangement.Bottom,
            ) {
                if (bar.total == 0) {
                    Box(Modifier.fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.outlineVariant))
                } else {
                    if (tallest > bar.total) Spacer(Modifier.weight((tallest - bar.total).toFloat()))
                    // Listed bottom-up, drawn top-down.
                    for (segment in bar.segments.reversed()) {
                        if (segment.value > 0) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .weight(segment.value.toFloat())
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(segment.color),
                            )
                        }
                    }
                }
            }
        }
    }
}
