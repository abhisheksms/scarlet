package com.cyanharborstudios.callblock.ui.parts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.ui.theme.LocalReducedMotion
import com.cyanharborstudios.callblock.ui.theme.Motion
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import com.cyanharborstudios.callblock.ui.theme.duration
import com.cyanharborstudios.callblock.ui.theme.switchboard
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** What a lamp shows: dark, lit, or held (lit as a ring, while filtering is paused). */
enum class LampState { Dark, Lit, Held }

/** One stop of the lever: its mode, the engraved label, and the sentence a screen reader adds. */
data class LeverStop(val mode: Mode, val label: String, val sentence: String)

private val HandleWidth = 46.dp
private val HandleHeight = 36.dp
private val HandleLeft = 13.dp

/**
 * Three stops. The handle is the position, the lamp says it is in effect, the
 * engraving names it. Tap a row or drag the handle. Locked (a device that cannot
 * screen), the handle moves 7 dp towards Silence and returns.
 */
@Composable
fun Lever(
    stops: List<LeverStop>,
    mode: Mode,
    lamps: (Mode) -> LampState,
    locked: Boolean,
    onChoose: (Mode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extra = switchboard
    val reduced = LocalReducedMotion.current
    val density = LocalDensity.current
    val rowHeight = with(density) { maxOf(62.dp, 49.9.sp.toDp()) }
    val stopIndex = stops.indexOfFirst { it.mode == mode }.coerceAtLeast(0)
    val restY = rowHeight * stopIndex + (rowHeight - HandleHeight) / 2
    val settled by animateDpAsState(restY, tween(duration(Motion.LEVER_TRAVEL, reduced), easing = Motion.emphasized), label = "handle")
    val nudge = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var dragging by remember { mutableStateOf(false) }
    var dragY by remember { mutableFloatStateOf(0f) }

    fun choose(wanted: Mode) {
        if (wanted == mode) return
        if (locked && wanted != Mode.OFF) {
            if (!reduced) {
                scope.launch {
                    nudge.animateTo(1f, tween(Motion.LEVER_LOCKED / 2, easing = Motion.emphasized))
                    nudge.animateTo(0f, tween(Motion.LEVER_LOCKED / 2, easing = Motion.emphasized))
                }
            }
            return
        }
        onChoose(wanted)
    }

    Box(
        modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(colors.outlineVariant, Offset(0f, 2.dp.toPx()), Size(size.width, size.height - 2.dp.toPx()), CornerRadius(6.dp.toPx()))
            }
            .padding(bottom = 2.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainerHigh, PlateShape)
                .border(1.dp, colors.outlineVariant, PlateShape),
        ) {
            Column(Modifier.selectableGroup()) {
                stops.forEachIndexed { index, stop ->
                    val set = stop.mode == mode
                    val spoken = "${stop.label}. ${stop.sentence}"
                    Row(
                        Modifier
                            .testTag("mode-${stop.mode.name}")
                            .fillMaxWidth()
                            .height(rowHeight)
                            .then(
                                if (index > 0) {
                                    Modifier.drawBehind { drawRect(colors.outlineVariant, Offset(72.dp.toPx(), 0f), Size(size.width - 72.dp.toPx(), 1.dp.toPx())) }
                                } else {
                                    Modifier
                                },
                            )
                            .selectable(
                                selected = set,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.RadioButton,
                                onClick = { choose(stop.mode) },
                            )
                            .semantics { contentDescription = spoken },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.width(72.dp).fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
                            if (!set) Box(Modifier.padding(end = 10.dp).size(10.dp, 2.dp).background(colors.outline))
                        }
                        CapsText(
                            stop.label,
                            if (set) SwitchboardType.leverSet else SwitchboardType.lever,
                            Modifier.weight(1f),
                            color = if (set) colors.onSurface else colors.onSurfaceVariant,
                            maxLines = 1,
                        )
                        Box(Modifier.width(60.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            if (stop.mode == Mode.OFF) OffRing(lit = lamps(stop.mode) == LampState.Lit) else Lamp(lamps(stop.mode))
                        }
                    }
                }
            }
            // the slot the handle runs in
            Box(Modifier.matchParentSize().padding(start = 28.dp, top = 18.dp, bottom = 18.dp)) {
                val highlight = colors.surfaceContainerHighest
                Box(
                    Modifier
                        .width(16.dp)
                        .fillMaxHeight()
                        .drawBehind { drawRoundRect(highlight, Offset(0f, 1.dp.toPx()), size, CornerRadius(8.dp.toPx())) }
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.inverseSurface)
                        .drawBehind { drawRect(Color.Black, Offset.Zero, Size(size.width, 2.dp.toPx())) },
                )
            }
            // the handle
            val handleY = if (dragging) dragY else with(density) { settled.toPx() + nudge.value * 7.dp.toPx() }
            val lowest = with(density) { (rowHeight * (stops.size - 1) + (rowHeight - HandleHeight) / 2).toPx() }
            val highest = with(density) { ((rowHeight - HandleHeight) / 2).toPx() }
            Box(
                Modifier
                    .offset { IntOffset(HandleLeft.roundToPx(), handleY.roundToInt()) }
                    .size(HandleWidth, HandleHeight)
                    .pointerInput(mode, locked, rowHeight) {
                        detectDragGestures(
                            onDragStart = {
                                dragging = true
                                dragY = restY.toPx()
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                dragY = (dragY + amount.y).coerceIn(highest, lowest)
                            },
                            onDragEnd = {
                                dragging = false
                                val stop = ((dragY - highest) / rowHeight.toPx()).roundToInt().coerceIn(0, stops.size - 1)
                                choose(stops[stop].mode)
                            },
                            onDragCancel = { dragging = false },
                        )
                    }
                    .drawBehind {
                        val radius = CornerRadius(4.dp.toPx())
                        drawRoundRect(extra.handleDrop, Offset(0f, 3.dp.toPx()), size, radius)
                        drawRoundRect(Brush.verticalGradient(0f to extra.handleHigh, 0.55f to extra.handle), Offset.Zero, size, radius)
                        drawRoundRect(Color.White.copy(alpha = 0.25f), Offset(2.dp.toPx(), 0f), Size(size.width - 4.dp.toPx(), 1.dp.toPx()), radius)
                        for (y in listOf(11, 17, 23)) {
                            drawRect(extra.handleRidge, Offset(10.dp.toPx(), y.dp.toPx()), Size(size.width - 20.dp.toPx(), 2.dp.toPx()))
                        }
                    },
            )
        }
    }
}

/** A lamp: dark glass, or lit with a halo, or held as a ring while paused. The old lamp goes dark at once; the new one warms up. */
@Composable
fun Lamp(state: LampState, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val extra = switchboard
    val reduced = LocalReducedMotion.current
    val lit by animateFloatAsState(
        targetValue = if (state == LampState.Lit) 1f else 0f,
        animationSpec = if (state == LampState.Lit) tween(duration(Motion.LAMP_WARM, reduced), delayMillis = duration(Motion.LAMP_WARM_DELAY, reduced), easing = Motion.decelerate) else snap(),
        label = "lamp",
    )
    Canvas(modifier.size(42.dp)) {
        val centre = center
        val glassRadius = 14.dp.toPx()
        val glassCentre = Offset(centre.x - glassRadius + 0.36f * 2 * glassRadius, centre.y - glassRadius + 0.30f * 2 * glassRadius)
        drawCircle(Brush.radialGradient(0f to extra.lampGlassHigh, 0.62f to extra.lampGlass, center = glassCentre, radius = glassRadius * 1.9f), glassRadius, centre)
        drawCircle(colors.outline, glassRadius - 1.dp.toPx(), centre, style = Stroke(2.dp.toPx()))
        if (state == LampState.Held) drawCircle(colors.onSurface, glassRadius - 3.5.dp.toPx(), centre, style = Stroke(3.dp.toPx()))
        if (lit > 0f) {
            val litRadius = 16.dp.toPx()
            val litCentre = Offset(centre.x - litRadius + 0.36f * 2 * litRadius, centre.y - litRadius + 0.30f * 2 * litRadius)
            drawCircle(extra.lampHalo, litRadius + 2.5.dp.toPx(), centre, alpha = lit, style = Stroke(5.dp.toPx()))
            drawCircle(Brush.radialGradient(0f to extra.lampOnHigh, 0.58f to colors.onSurface, center = litCentre, radius = litRadius * 1.9f), litRadius, centre, alpha = lit)
            drawCircle(colors.outline, litRadius - 1.dp.toPx(), centre, alpha = lit, style = Stroke(2.dp.toPx()))
        }
    }
}

/** The Off stop has a ring, not a lamp: a thin outline, or a thick one when Off is in effect. */
@Composable
fun OffRing(lit: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier.size(22.dp)) {
        val stroke = if (lit) 3.5.dp.toPx() else 2.5.dp.toPx()
        drawCircle(if (lit) colors.onSurface else colors.outline, size.minDimension / 2 - stroke / 2, center, style = Stroke(stroke))
    }
}
