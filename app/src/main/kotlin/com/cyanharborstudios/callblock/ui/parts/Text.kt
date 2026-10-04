package com.cyanharborstudios.callblock.ui.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

/**
 * Text set in capitals, as an engraved label is. The string itself stays in Title
 * Case or sentence case, and that is what a screen reader is given.
 */
@Composable
fun CapsText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
) {
    val locale = LocalConfiguration.current.locales[0]
    Text(
        text = text.uppercase(locale),
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        softWrap = softWrap,
        modifier = modifier.clearAndSetSemantics { this.text = AnnotatedString(text) },
    )
}

/**
 * Keeps the last two words of a sentence on one line, so no line holds a single
 * word: the last space becomes a no-break space when the last word is short.
 */
fun tieWidows(sentence: String): String {
    val last = sentence.lastIndexOf(' ')
    if (last <= 0 || sentence.indexOf(' ') == last || sentence.length - last > 13) return sentence
    return sentence.substring(0, last) + ' ' + sentence.substring(last + 1)
}

/** A sentence with its widow tied. */
@Composable
fun Sentence(text: String, style: TextStyle, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Text(tieWidows(text), style = style, color = color, modifier = modifier)
}

/** The outcome of a call as a shape: a filled dot for blocked, a ring for silenced. */
@Composable
fun Mark(blocked: Boolean, modifier: Modifier = Modifier, color: Color = LocalContentColor.current) {
    val base = modifier.size(10.dp)
    Box(if (blocked) base.background(color, CircleShape) else base.border(2.5.dp, color, CircleShape))
}

/** A mark and its words on one line: "31 blocked", "3 silenced". */
@Composable
fun MarkLine(blocked: Boolean, text: String, style: TextStyle, modifier: Modifier = Modifier, color: Color = LocalContentColor.current) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Mark(blocked, color = color)
        Text(text, style = style, color = color)
    }
}

/**
 * Parts joined by a middle dot, wrapping as parts: "Today, 17:41 · Blocked" on one
 * line, or the time over the outcome on two. A part that starts a new line has no
 * dot before it.
 */
@Composable
fun DottedParts(parts: List<String>, style: TextStyle, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    if (parts.size <= 1) {
        Text(parts.firstOrNull().orEmpty(), style = style, color = color, modifier = modifier)
        return
    }
    val gap = with(LocalDensity.current) { (style.fontSize * 0.9f).toDp() }
    Layout(
        modifier = modifier,
        content = {
            for (part in parts) Text(part, style = style, color = color, maxLines = 1)
            repeat(parts.size - 1) {
                Text("·", style = style, color = color, modifier = Modifier.layoutId("dot").clearAndSetSemantics { })
            }
        },
    ) { measurables, constraints ->
        val gapPx = gap.roundToPx()
        val loose = Constraints(maxWidth = constraints.maxWidth)
        val partPlaceables = measurables.filter { it.layoutId != "dot" }.map { it.measure(loose) }
        val dotPlaceables = measurables.filter { it.layoutId == "dot" }.map { it.measure(loose) }
        val lineHeight = partPlaceables.maxOf { it.height }
        val xs = IntArray(partPlaceables.size)
        val ys = IntArray(partPlaceables.size)
        val dotX = IntArray(dotPlaceables.size) { -1 }
        var x = 0
        var y = 0
        var widest = 0
        partPlaceables.forEachIndexed { index, part ->
            if (index > 0) {
                if (x + gapPx + part.width > constraints.maxWidth) {
                    x = 0
                    y += lineHeight
                } else {
                    dotX[index - 1] = x + (gapPx - dotPlaceables[index - 1].width) / 2
                    x += gapPx
                }
            }
            xs[index] = x
            ys[index] = y
            x += part.width
            widest = maxOf(widest, x)
        }
        layout(widest.coerceIn(constraints.minWidth, constraints.maxWidth), y + lineHeight) {
            partPlaceables.forEachIndexed { index, part -> part.placeRelative(xs[index], ys[index]) }
            dotPlaceables.forEachIndexed { index, dot ->
                if (dotX[index] >= 0) dot.placeRelative(dotX[index], ys[index + 1])
            }
        }
    }
}
