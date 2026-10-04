package com.cyanharborstudios.callblock.ui.parts

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cyanharborstudios.callblock.R
import androidx.compose.ui.res.stringResource
import com.cyanharborstudios.callblock.ui.theme.LocalReducedMotion
import com.cyanharborstudios.callblock.ui.theme.Motion
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import com.cyanharborstudios.callblock.ui.theme.duration
import com.cyanharborstudios.callblock.ui.theme.switchboard

/** What sits at the end of a strip. A chevron stays in the app; an arrow leaves it; a switch toggles. */
enum class Trail { None, Chevron, Out, Switch }

/** An engraved rule with its highlight under it: the top edge of a group of strips or a section. */
fun Modifier.engravedTop(groove: Color, highlight: Color): Modifier = drawBehind {
    val one = 1.dp.toPx()
    drawRect(groove, Offset.Zero, Size(size.width, one))
    drawRect(highlight, Offset(0f, one), Size(size.width, one))
}

/** The hairline under a strip or a row. */
fun Modifier.ruleBelow(groove: Color): Modifier = drawBehind {
    val one = 1.dp.toPx()
    drawRect(groove, Offset(0f, size.height - one), Size(size.width, one))
}

/** The tint a row takes while pressed: no ripple. */
@Composable
fun Modifier.pressTint(interaction: MutableInteractionSource): Modifier {
    val pressed by interaction.collectIsPressedAsState()
    val tint = switchboard.press
    return if (pressed) background(tint) else this
}

/**
 * A full-width row between engraved rules: a title in capitals, a line of detail,
 * and at most one trailing part. The whole row is the target.
 */
@Composable
fun Strip(
    title: String,
    modifier: Modifier = Modifier,
    detail: String? = null,
    detailParts: List<String>? = null,
    trail: Trail = Trail.None,
    checked: Boolean = false,
    value: String? = null,
    onClick: (() -> Unit)? = null,
    spoken: String? = null,
    rule: Boolean = true,
    head: Boolean = false,
    tag: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val target = when {
        onClick == null -> Modifier
        trail == Trail.Switch -> Modifier.toggleable(value = checked, interactionSource = interaction, indication = null, role = Role.Switch, onValueChange = { onClick() })
        else -> Modifier.clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
    }
    Row(
        modifier
            .then(if (tag != null) Modifier.testTag(tag) else Modifier)
            .then(if (spoken != null) Modifier.semantics(mergeDescendants = true) { contentDescription = spoken } else Modifier)
            .then(target)
            .pressTint(interaction)
            .then(if (rule) Modifier.ruleBelow(colors.outlineVariant) else Modifier)
            .fillMaxWidth()
            .defaultMinSize(minHeight = if (head) 49.dp else 58.dp)
            .padding(top = if (head) 0.dp else 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CapsText(title, SwitchboardType.strip, color = colors.onSurface)
            when {
                detailParts != null -> DottedParts(detailParts, SwitchboardType.body, color = colors.onSurfaceVariant)
                detail != null -> Text(tieWidows(detail), style = SwitchboardType.body, color = colors.onSurfaceVariant)
            }
        }
        when (trail) {
            Trail.Chevron -> Icon(SwitchboardIcons.chevron, null, Modifier.size(18.dp), tint = colors.onSurface)
            Trail.Out -> Icon(SwitchboardIcons.out, null, Modifier.size(18.dp), tint = colors.onSurface)
            Trail.Switch -> PanelSwitch(checked)
            Trail.None -> if (value != null) Text(value, style = SwitchboardType.leadStrong, color = colors.onSurface)
        }
    }
}

/** A slot with a sliding handle. Left is off, right is on; the row says the word. */
@Composable
fun PanelSwitch(checked: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val extra = switchboard
    val reduced = LocalReducedMotion.current
    val travel by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = tween(duration(Motion.SWITCH_SLIDE, reduced), easing = Motion.emphasized),
        label = "switch",
    )
    val highlight = colors.surfaceContainerHighest
    Box(
        modifier
            .size(46.dp, 26.dp)
            .drawBehind {
                // the 1 dp highlight under the slot, then the slot with its 2 dp dark top edge
                drawRoundRect(highlight, Offset(0f, 1.dp.toPx()), size, androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
                drawRoundRect(colors.inverseSurface, Offset.Zero, size, androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
                drawRect(Color.Black.copy(alpha = 0.6f), Offset(2.dp.toPx(), 0f), Size(size.width - 4.dp.toPx(), 2.dp.toPx()))
            },
    ) {
        Box(
            Modifier
                .padding(2.dp)
                .graphicsLayer { translationX = travel.toPx() }
                .size(22.dp)
                .drawBehind {
                    val radius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                    drawRoundRect(extra.handleDrop, Offset(0f, 1.dp.toPx()), size, radius)
                    drawRoundRect(Brush.verticalGradient(0f to extra.handleHigh, 0.6f to extra.handle), Offset.Zero, size, radius)
                },
        )
    }
}

/** A group of strips under one engraved rule. The last strip keeps its rule when [closed]. */
@Composable
fun Strips(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier
            .fillMaxWidth()
            .engravedTop(colors.outlineVariant, colors.surfaceContainerHighest)
            .padding(top = 1.dp),
        content = content,
    )
}

/** A titled block of a screen, under an engraved rule unless it is the first. */
@Composable
fun Section(modifier: Modifier = Modifier, first: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier
            .fillMaxWidth()
            .then(if (first) Modifier else Modifier.engravedTop(colors.outlineVariant, colors.surfaceContainerHighest).padding(top = 12.dp)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

/** A section's title in capitals, with an optional line at its right, both on one baseline. */
@Composable
fun SectionHeading(title: String, modifier: Modifier = Modifier, trailing: (@Composable RowScope.() -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        CapsText(title, SwitchboardType.section, Modifier.alignByBaseline().semantics { heading() }, color = MaterialTheme.colorScheme.onSurface)
        if (trailing != null) trailing()
    }
}

/**
 * Back, the title in capitals, and at most one action. It stays in place while the
 * screen scrolls, and reaches 12 dp into the screen's side margins so the back
 * button's target is wide.
 */
@Composable
fun Header(title: String, onBack: () -> Unit, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .widenBy(12.dp)
            .background(colors.surface)
            .defaultMinSize(minHeight = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderIconButton(SwitchboardIcons.back, stringResource(R.string.back), onBack, iconSize = 24.dp, tag = "back")
        CapsText(
            title,
            SwitchboardType.title,
            Modifier.weight(1f).padding(horizontal = 4.dp).semantics { heading() },
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (action != null) action()
    }
}

/** A 48 dp square button in the header. */
@Composable
fun HeaderIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 22.dp,
    tag: String? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .then(if (tag != null) Modifier.testTag(tag) else Modifier)
            .size(48.dp)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .pressTint(interaction),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, Modifier.size(iconSize), tint = MaterialTheme.colorScheme.onSurface)
    }
}

/** A text action in the header: Delete All. */
@Composable
fun HeaderTextButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, tag: String? = null) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .then(if (tag != null) Modifier.testTag(tag) else Modifier)
            .defaultMinSize(minHeight = 48.dp)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .pressTint(interaction)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        CapsText(label, SwitchboardType.strip, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}

/** Lets a row reach [amount] into the margins on both sides of its parent. */
fun Modifier.widenBy(amount: androidx.compose.ui.unit.Dp): Modifier = layout { measurable, constraints ->
    val extra = amount.roundToPx()
    val placeable = measurable.measure(constraints.copy(minWidth = 0, maxWidth = constraints.maxWidth + 2 * extra))
    layout(constraints.maxWidth, placeable.height) { placeable.placeRelative(-extra, 0) }
}

/** A corner radius shared by plates, the display and sheets. */
val PlateShape = RoundedCornerShape(6.dp)
