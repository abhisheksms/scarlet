package com.cyanharborstudios.callblock.ui.parts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout

/**
 * Lays out [content] at least as tall as the tallest of the [candidates] would be at
 * this width. The display window and the bay under the lever each take the height of
 * their tallest state, so the lever and the strips never move. The candidates are
 * measured, never drawn or read.
 */
@Composable
fun TallestOf(
    candidates: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    /** True stretches the content to the tallest height; false leaves the content its own height inside it. */
    fillHeight: Boolean = true,
    content: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        val loose = constraints.copy(minHeight = 0)
        var tallest = 0
        candidates.forEachIndexed { index, candidate ->
            for (measurable in subcompose("candidate-$index", candidate)) {
                tallest = maxOf(tallest, measurable.measure(loose).height)
            }
        }
        val minHeight = maxOf(tallest, constraints.minHeight).coerceAtMost(constraints.maxHeight)
        val placeables = subcompose("content", content).map { it.measure(if (fillHeight) loose.copy(minHeight = minHeight) else loose) }
        val width = placeables.maxOfOrNull { it.width } ?: constraints.minWidth
        val height = maxOf(minHeight, placeables.maxOfOrNull { it.height } ?: 0)
        layout(width, height) { placeables.forEach { it.placeRelative(0, 0) } }
    }
}
