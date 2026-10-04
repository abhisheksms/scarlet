package com.cyanharborstudios.callblock.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.cyanharborstudios.callblock.R

/*
 * The type scale, as the design spec gives it (design/prototype/SPEC.md, "Type, in sp").
 * One typeface: Hanken Grotesk, a variable font under the SIL Open Font License
 * (docs/OFL-HankenGrotesk.txt). Its figures are the same width at every weight, so
 * times and counts line up without a font feature. Ligatures are off.
 *
 * Capitals on controls and titles are a style, like engraving: the strings stay in
 * Title Case and sentence case, and CapsText (ui/parts/Text.kt) sets them in capitals.
 */

/** One file, four weights: a variable font takes its weight from the request. */
val HankenGrotesk = FontFamily(
    Font(R.font.hanken_grotesk, FontWeight.W400),
    Font(R.font.hanken_grotesk, FontWeight.W500),
    Font(R.font.hanken_grotesk, FontWeight.W600),
    Font(R.font.hanken_grotesk, FontWeight.W700),
)

private fun style(size: Int, line: Int, weight: FontWeight, tracking: TextUnit = TextUnit.Unspecified) = TextStyle(
    fontFamily = HankenGrotesk,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = tracking,
    fontFeatureSettings = "liga 0",
    // The line box is exactly the line height, as on the prototype: no font padding.
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None),
)

/** Every text style the screens use, by the names the spec gives them. */
object SwitchboardType {
    /** Counter drums; today's count on Home. */
    val numeral = style(40, 42, FontWeight.W500)

    /** Off, Silence, Block: in capitals. The set position is bold. */
    val lever = style(26, 30, FontWeight.W600, 0.04.em)
    val leverSet = lever.copy(fontWeight = FontWeight.W700)

    /** The sentence in the display window; the number on a sheet; a dialog's question. Balanced lines. */
    val display = style(21, 27, FontWeight.W500).copy(lineBreak = LineBreak.Heading)

    /** Screen titles, in capitals. */
    val title = style(18, 22, FontWeight.W600, 0.08.em)

    /** Resume, Set As Screening App: in capitals. */
    val mainKey = style(17, 21, FontWeight.W700, 0.05.em)

    /** Phone numbers in lists; the field. */
    val number = style(17, 22, FontWeight.W500)

    /** Every other key, in capitals. */
    val key = style(15, 18, FontWeight.W600, 0.03.em)

    /** Counts beside a mark; times; the second line when the role is missing. */
    val lead = style(15, 20, FontWeight.W400)
    val leadStrong = lead.copy(fontWeight = FontWeight.W500)

    /** Sentences, details, second lines of rows. */
    val body = style(14, 19, FontWeight.W400)

    /** Row titles, in capitals. Section titles track a little wider. */
    val strip = style(13, 17, FontWeight.W600, 0.08.em)
    val section = style(13, 17, FontWeight.W600, 0.1.em)

    /** The privacy line, milestone labels, the build stamp. */
    val note = style(13, 18, FontWeight.W400)

    /** Captions over keys, tile captions, day headings: in capitals. Chart labels track a little tighter. */
    val caption = style(12, 16, FontWeight.W600, 0.1.em)
    val chartLabel = style(12, 18, FontWeight.W600, 0.06.em)
    val chartValue = style(12, 12, FontWeight.W500)

    /** The word Advertisement, in capitals. */
    val slot = style(11, 14, FontWeight.W600, 0.1.em)

    /** The empty-list line and the licence names. */
    val empty = style(15, 21, FontWeight.W400)
    val licenceName = style(15, 20, FontWeight.W600)
}

/** The same styles in Material's slots, for anything Material draws itself. */
val SwitchboardTypography = Typography(
    displayMedium = SwitchboardType.numeral,
    headlineSmall = SwitchboardType.lever,
    titleLarge = SwitchboardType.display,
    titleMedium = SwitchboardType.title,
    titleSmall = SwitchboardType.strip,
    labelLarge = SwitchboardType.key,
    labelMedium = SwitchboardType.caption,
    labelSmall = SwitchboardType.slot,
    bodyLarge = SwitchboardType.lead,
    bodyMedium = SwitchboardType.body,
    bodySmall = SwitchboardType.note,
)
