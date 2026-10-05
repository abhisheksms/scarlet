package com.cyanharborstudios.callblock.ui.parts

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cyanharborstudios.callblock.ui.theme.LocalReducedMotion
import com.cyanharborstudios.callblock.ui.theme.Motion
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import com.cyanharborstudios.callblock.ui.theme.duration
import com.cyanharborstudios.callblock.ui.theme.switchboard

/** One choice in a strip of keys: what it stands for, the label on the key, and what is spoken. */
data class KeyChoice<T>(val value: T, val label: String, val spoken: String = label)

private val KeyShape = RoundedCornerShape(4.dp)
private val KeyDrop = 3.dp

/**
 * A raised key with a 3 dp drop. Momentary keys act at once; latching keys hold one
 * choice of a set and stay down. Pressed, the face drops onto its shadow.
 */
@Composable
fun Key(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    latched: Boolean = false,
    spoken: String = label,
    radio: Boolean = false,
    onPlate: Boolean = false,
    tag: String? = null,
    minHeight: Dp = 48.dp,
    horizontalPadding: Dp = 6.dp,
    fillWidth: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduced = LocalReducedMotion.current
    val down by animateDpAsState(
        targetValue = if (latched || pressed) KeyDrop else 0.dp,
        animationSpec = tween(duration(Motion.KEY_PRESS, reduced), easing = Motion.linear),
        label = "key",
    )
    val face = when {
        latched -> colors.primary
        onPlate -> colors.surfaceContainerHighest
        else -> colors.surfaceContainerHigh
    }
    val ring = if (latched) colors.primary else colors.outline
    val dropColor = colors.outlineVariant
    val select = if (radio) {
        Modifier.selectable(selected = latched, interactionSource = interaction, indication = null, role = Role.RadioButton, onClick = onClick)
    } else {
        Modifier.clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
    }
    Column(
        modifier
            .semantics(mergeDescendants = true) {
                contentDescription = spoken
                if (tag != null) testTag = tag
            }
            .then(select)
            .drawBehind {
                if (!latched) {
                    val radius = CornerRadius(4.dp.toPx())
                    drawRoundRect(dropColor, Offset(0f, KeyDrop.toPx()), Size(size.width, size.height - KeyDrop.toPx()), radius)
                }
            },
    ) {
        Box(
            Modifier
                .graphicsLayer { translationY = down.toPx() }
                .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                .defaultMinSize(minHeight = minHeight)
                .background(face, KeyShape)
                .border(1.dp, ring, KeyShape)
                .padding(horizontal = horizontalPadding, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            CapsText(label, SwitchboardType.key, color = if (latched) colors.onPrimary else colors.onSurface, textAlign = TextAlign.Center, maxLines = 1)
        }
        Spacer(Modifier.height(KeyDrop))
    }
}

/** The one key on a screen that outranks the rest: Resume, Set As Screening App. */
@Composable
fun MainKey(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, tag: String? = null) {
    val colors = MaterialTheme.colorScheme
    val dropColor = switchboard.handleDrop
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduced = LocalReducedMotion.current
    val down by animateDpAsState(
        targetValue = if (pressed) KeyDrop else 0.dp,
        animationSpec = tween(duration(Motion.KEY_PRESS, reduced), easing = Motion.linear),
        label = "mainKey",
    )
    Column(
        modifier
            .then(if (tag != null) Modifier.testTag(tag) else Modifier)
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .drawBehind {
                if (!pressed) {
                    drawRoundRect(dropColor, Offset(0f, KeyDrop.toPx()), Size(size.width, size.height - KeyDrop.toPx()), CornerRadius(4.dp.toPx()))
                }
            },
    ) {
        Box(
            Modifier
                .graphicsLayer { translationY = down.toPx() }
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .background(colors.primary, KeyShape)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            CapsText(label, SwitchboardType.mainKey, color = colors.onPrimary, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(KeyDrop))
    }
}

/**
 * Keys in a strip share its width equally unless a label needs more; then that key
 * takes what it needs. Four keys that cannot share one row go two by two; otherwise
 * a strip whose labels cannot share one row stacks.
 */
@Composable
fun KeyStrip(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val gap = 8.dp.roundToPx()
        val rowGap = 11.dp.roundToPx()
        val width = constraints.maxWidth
        val wanted = measurables.map { it.maxIntrinsicWidth(Constraints.Infinity) }

        // Column widths for keys sharing one row, or null when their labels cannot all fit.
        fun share(wants: List<Int>): IntArray? {
            val free = width - gap * (wants.size - 1)
            if (wants.sum() > free) return null
            val fixed = BooleanArray(wants.size)
            val widths = IntArray(wants.size)
            while (true) {
                val remaining = free - wants.indices.filter { fixed[it] }.sumOf { wants[it] }
                val flexible = wants.indices.filter { !fixed[it] }
                if (flexible.isEmpty()) break
                val each = remaining / flexible.size
                val tooWide = flexible.filter { wants[it] > each }
                if (tooWide.isEmpty()) {
                    flexible.forEachIndexed { n, i -> widths[i] = each + if (n == flexible.lastIndex) remaining - each * flexible.size else 0 }
                    break
                }
                tooWide.forEach { fixed[it] = true; widths[it] = wants[it] }
            }
            return widths
        }

        val rows: List<List<Int>> = when {
            share(wanted) != null -> listOf(measurables.indices.toList())
            measurables.size == 4 && share(wanted.subList(0, 2)) != null && share(wanted.subList(2, 4)) != null -> listOf(listOf(0, 1), listOf(2, 3))
            else -> measurables.indices.map { listOf(it) }
        }
        val placeables = arrayOfNulls<androidx.compose.ui.layout.Placeable>(measurables.size)
        val rowHeights = IntArray(rows.size)
        val columnWidths = IntArray(measurables.size)
        rows.forEachIndexed { r, row ->
            val widths = share(row.map { wanted[it] }) ?: IntArray(row.size) { width }
            row.forEachIndexed { n, i ->
                columnWidths[i] = widths[n]
                val placeable = measurables[i].measure(Constraints.fixedWidth(widths[n]))
                placeables[i] = placeable
                rowHeights[r] = maxOf(rowHeights[r], placeable.height)
            }
        }
        val height = rowHeights.sum() + rowGap * (rows.size - 1)
        layout(width, height) {
            var y = 0
            rows.forEachIndexed { r, row ->
                var x = 0
                for (i in row) {
                    placeables[i]!!.placeRelative(x, y)
                    x += columnWidths[i] + gap
                }
                y += rowHeights[r] + rowGap
            }
        }
    }
}

/** A caption over a strip of momentary keys: "Pause for", then the lengths. */
@Composable
fun <T> KeysBlock(
    caption: String,
    choices: List<KeyChoice<T>>,
    onChoose: (T) -> Unit,
    modifier: Modifier = Modifier,
    onPlate: Boolean = false,
    tag: (T) -> String? = { null },
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CapsText(caption, SwitchboardType.caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
        KeyStrip {
            for (choice in choices) {
                Key(choice.label, onClick = { onChoose(choice.value) }, spoken = choice.spoken, onPlate = onPlate, tag = tag(choice.value))
            }
        }
    }
}

/** A strip of latching keys holding one choice: who is filtered, the repeat window, the period, the summary. */
@Composable
fun <T> LatchingKeys(
    choices: List<KeyChoice<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    onPlate: Boolean = false,
    tag: (T) -> String? = { null },
) {
    KeyStrip(modifier.selectableGroup()) {
        for (choice in choices) {
            Key(
                choice.label,
                onClick = { onSelect(choice.value) },
                latched = choice.value == selected,
                spoken = choice.spoken,
                radio = true,
                onPlate = onPlate,
                tag = tag(choice.value),
            )
        }
    }
}
