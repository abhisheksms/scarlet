package com.cyanharborstudios.callblock.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

/**
 * Lets UI automation find controls by their test tag. A dialog or a sheet is its own
 * window with its own semantics tree, so each needs this on its root, as the
 * activity's content has it.
 */
fun Modifier.exposeTestTags(): Modifier = semantics { testTagsAsResourceId = true }
