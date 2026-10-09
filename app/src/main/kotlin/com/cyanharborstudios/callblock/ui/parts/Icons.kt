package com.cyanharborstudios.callblock.ui.parts

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The design's own line icons, drawn as the prototype draws them (design/prototype/
 * src/30-views.js). Each is a 24-unit drawing; the size it is shown at sets the
 * stroke, so a 22 dp gear has a thinner line than a 24 dp one would.
 */
object SwitchboardIcons {

    private fun icon(name: String, vararg strokes: Pair<String, Float>, fills: List<String> = emptyList()): ImageVector {
        val builder = ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        for ((data, width) in strokes) builder.addPath(addPathNodes(data), stroke = SolidColor(Color.Black), strokeLineWidth = width)
        for (data in fills) builder.addPath(addPathNodes(data), fill = SolidColor(Color.Black))
        return builder.build()
    }

    private fun circle(cx: Float, cy: Float, r: Float) = "M${cx - r} $cy a$r $r 0 1 0 ${2 * r} 0 a$r $r 0 1 0 ${-2 * r} 0"

    val gear: ImageVector = icon(
        "gear",
        circle(12f, 12f, 6.2f) to 2f,
        "M12 1.6v3M12 19.4v3M1.6 12h3M19.4 12h3M4.65 4.65l2.1 2.1M17.25 17.25l2.1 2.1M4.65 19.35l2.1-2.1M17.25 6.75l2.1-2.1" to 3.2f,
        fills = listOf(circle(12f, 12f, 1.6f)),
    )

    /** Stays inside the app. */
    val chevron: ImageVector = icon("chevron", "M9 5l7 7-7 7" to 2.4f)

    /** Leaves the app: a browser, the email app, the store. */
    val out: ImageVector = icon("out", "M9 6h9v9M18 6 6.5 17.5" to 2.4f)

    val back: ImageVector = icon("back", "M20 12H5M11 5.5 4.5 12l6.5 6.5" to 2.2f)

    val share: ImageVector = icon(
        "share",
        circle(18f, 5f, 2.6f) to 2.2f,
        circle(6f, 12f, 2.6f) to 2.2f,
        circle(18f, 19f, 2.6f) to 2.2f,
        "M8.3 10.7l7.4-4.3M8.3 13.3l7.4 4.3" to 2.2f,
    )

    val close: ImageVector = icon("close", "M6 6l12 12M18 6 6 18" to 2.4f)

    // The row icons, at the founder's ask of 10 October 2026 (NOTES.md N-57): one beside each
    // row's word, never instead of it. Drawn in the same strokes as the icons above.

    /** Three slider lines with their knobs: Options. */
    val sliders: ImageVector = icon("sliders", "M3.5 7h17M3.5 12h17M3.5 17h17" to 2.2f, "M9 4.2v5.6M15.5 9.2v5.6M7 14.2v5.6" to 2.8f)

    val bell: ImageVector = icon("bell", "M6.5 17.5V11a5.5 5.5 0 0 1 11 0v6.5l1.6 2H4.9z" to 2.2f, "M10 21.3a2 2 0 0 0 4 0" to 2.2f)

    /** History: a clock, as the phone's own Recents tab draws it. */
    val clock: ImageVector = icon("clock", circle(12f, 12f, 8.4f) to 2.2f, "M12 7.3v5.1l3.3 2" to 2.2f)

    /** Statistics. */
    val bars: ImageVector = icon("bars", "M6 20V11M12 20V4.5M18 20V14" to 2.8f, "M3.5 20.5h17" to 2.2f)

    val timer: ImageVector = icon("timer", circle(12f, 13.6f, 7.2f) to 2.2f, "M12 10v4l2.7 1.7" to 2.2f, "M9.6 2.7h4.8M12 2.7v3.7" to 2.4f)

    /** Schedule. */
    val calendar: ImageVector = icon("calendar", "M4 6.5h16v13.5H4zM4 10.7h16M8 4v4.5M16 4v4.5" to 2.2f)

    /** How It Works. */
    val question: ImageVector = icon(
        "question",
        circle(12f, 12f, 8.4f) to 2.2f,
        "M9.6 9.9a2.5 2.5 0 1 1 3.6 2.2c-.8.4-1.2.9-1.2 1.9" to 2.2f,
        fills = listOf(circle(12f, 16.9f, 1.15f)),
    )

    /** Plans: a price tag. */
    val tag: ImageVector = icon("tag", "M3.5 12.3V4h8.3l8.7 8.7-8.3 8.3z" to 2.2f, fills = listOf(circle(7.6f, 8.1f, 1.3f)))

    val info: ImageVector = icon("info", circle(12f, 12f, 8.4f) to 2.2f, "M12 11v5.4" to 2.4f, fills = listOf(circle(12f, 8.1f, 1.2f)))

    /** Licences, and the summary notification. */
    val document: ImageVector = icon("document", "M6.5 3h7l4.5 4.5V21h-11.5z" to 2.2f, "M13.3 3.2v4.6h4.4M9.3 12.5h5.4M9.3 16.3h5.4" to 2f)

    val mail: ImageVector = icon("mail", "M3.5 6h17v12h-17z" to 2.2f, "M3.8 7l8.2 6 8.2-6" to 2.2f)

    private const val SHIELD = "M12 3l7.5 2.8v6c0 4.6-3.2 7.9-7.5 9.2-4.3-1.3-7.5-4.6-7.5-9.2v-6z"

    val shield: ImageVector = icon("shield", SHIELD to 2.2f)

    val shieldTick: ImageVector = icon("shieldTick", SHIELD to 2.2f, "M8.8 12l2.3 2.3 4.2-4.6" to 2.2f)

    val star: ImageVector = icon("star", "M12 3.3l2.7 5.6 6.1.9-4.4 4.3 1 6.1-5.4-2.9-5.4 2.9 1-6.1-4.4-4.3 6.1-.9z" to 2f)

    /** Always block: a crossed circle. */
    val ban: ImageVector = icon("ban", circle(12f, 12f, 8.4f) to 2.2f, "M6.2 6.2l11.6 11.6" to 2.4f)

    /** Call-backs: a handset, and an arrow coming in. */
    val callBack: ImageVector = icon(
        "callBack",
        "M5.2 4h3.3l1.7 4.1-2.1 1.7c1.3 2.7 3.4 4.8 6.1 6.1l1.7-2.1 4.1 1.7v3.3c0 .8-.7 1.5-1.5 1.5C9.6 20.3 3.7 14.4 3.7 5.5 3.7 4.7 4.4 4 5.2 4z" to 2f,
        "M14 9V4.6M14 9h4.4M14 9l5.5-5.5" to 2.2f,
    )

    /** Allowed numbers. */
    val tick: ImageVector = icon("tick", circle(12f, 12f, 8.4f) to 2.2f, "M8 12.3l2.7 2.7 5.3-5.8" to 2.4f)

    /** Number rules: a funnel. */
    val funnel: ImageVector = icon("funnel", "M3.5 5h17l-6.5 7.6v5.6l-4 2.2v-7.8z" to 2.2f)

    val repeat: ImageVector = icon("repeat", "M17 3l3 3-3 3M20 6H8.5A4.5 4.5 0 0 0 4 10.5V11M7 21l-3-3 3-3M4 18h11.5a4.5 4.5 0 0 0 4.5-4.5V13" to 2.2f)

    /** Unknown numbers: a person. */
    val person: ImageVector = icon("person", circle(12f, 8f, 3.6f) to 2.2f, "M4.8 20.5a7.2 7.2 0 0 1 14.4 0" to 2.2f)
}
