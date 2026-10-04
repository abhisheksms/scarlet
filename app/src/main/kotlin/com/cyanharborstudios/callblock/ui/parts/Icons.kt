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
}
