package com.cyanharborstudios.callblock.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import com.cyanharborstudios.callblock.R

/** The lengths of time the app offers, in minutes. */
object Durations {
    val PAUSE = listOf(15, 60, 240, 1_440)
    val REPEAT_WINDOW = listOf(5, 15, 30)

    /** For an allow-list entry. Null means "always". */
    val ALLOW = listOf(null, 60, 1_440)
}

/** "15 minutes", "1 hour", "24 hours". */
@Composable
fun durationLabel(minutes: Int): String =
    if (minutes % 60 == 0) {
        pluralStringResource(R.plurals.hours, minutes / 60, minutes / 60)
    } else {
        pluralStringResource(R.plurals.minutes, minutes, minutes)
    }
