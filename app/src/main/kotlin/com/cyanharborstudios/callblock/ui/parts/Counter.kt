package com.cyanharborstudios.callblock.ui.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import com.cyanharborstudios.callblock.ui.theme.switchboard

/**
 * The all-time count on drums, like a message register. At least three drums;
 * leading zeros are dimmer. A fourth drum appears at 1,000.
 */
@Composable
fun Drums(total: Int, spoken: String, modifier: Modifier = Modifier) {
    val extra = switchboard
    val colors = MaterialTheme.colorScheme
    val digits = total.toString().padStart(3, '0')
    val lead = digits.length - total.toString().length
    val scale = LocalDensity.current.fontScale
    Row(modifier.clearAndSetSemantics { contentDescription = spoken }, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        digits.forEachIndexed { index, digit ->
            Box(
                Modifier
                    .size((34 * scale).dp, (54 * scale).dp)
                    .background(Brush.verticalGradient(listOf(extra.drumHigh, extra.drumLow)), RoundedCornerShape(3.dp))
                    .border(1.dp, extra.inverseOutlineVariant, RoundedCornerShape(3.dp))
                    .drawBehind { drawRect(Color.Black, Offset(0f, size.height / 2), Size(size.width, 1.dp.toPx())) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    digit.toString(),
                    style = SwitchboardType.numeral.copy(lineHeight = SwitchboardType.numeral.fontSize),
                    color = if (index < lead) extra.inverseOnSurfaceVariant else colors.inverseOnSurface,
                )
            }
        }
    }
}

/**
 * From zero to the next milestone, with a notch at the last one reached. Past the top
 * of the ladder the bar is full and there is no next.
 */
@Composable
fun MilestoneBar(total: Int, reached: Int?, next: Int?, reachedText: String?, nextText: String?, spoken: String, modifier: Modifier = Modifier) {
    val extra = switchboard
    val colors = MaterialTheme.colorScheme
    val top = next ?: reached ?: 10
    val fill = (total.toFloat() / top).coerceIn(0f, 1f)
    val notch = if (reached != null && next != null) reached.toFloat() / next else null
    Column(modifier.clearAndSetSemantics { contentDescription = spoken }, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .drawBehind {
                    val radius = CornerRadius(2.dp.toPx())
                    drawRoundRect(extra.inverseOutlineVariant, Offset.Zero, size, radius)
                    drawRoundRect(colors.inverseOnSurface, Offset.Zero, Size(size.width * fill, size.height), radius)
                    if (notch != null) {
                        drawRect(colors.inverseSurface, Offset(size.width * notch - 1.dp.toPx(), -3.dp.toPx()), Size(2.dp.toPx(), size.height + 6.dp.toPx()))
                    }
                },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(reachedText.orEmpty(), style = SwitchboardType.note.copy(lineHeight = SwitchboardType.strip.lineHeight), color = extra.inverseOnSurfaceVariant)
            Text(nextText.orEmpty(), style = SwitchboardType.note.copy(lineHeight = SwitchboardType.strip.lineHeight), color = extra.inverseOnSurfaceVariant)
        }
    }
}
