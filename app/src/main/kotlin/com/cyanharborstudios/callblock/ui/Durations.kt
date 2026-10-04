package com.cyanharborstudios.callblock.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import com.cyanharborstudios.callblock.R

/** The lengths of time the app offers, in minutes. */
object Durations {
    val PAUSE = listOf(15, 60, 240, 1_440)

    /** For repeat callers. 0 is off. */
    val REPEAT_WINDOW = listOf(0, 5, 15, 30)

    /** For an allow-list entry. Null means "always". */
    val ALLOW = listOf(60, 1_440, null)
}

/** "15 minutes", "1 hour", "24 hours": what a screen reader says for a length. */
@Composable
fun durationLabel(minutes: Int): String =
    if (minutes % 60 == 0) {
        pluralStringResource(R.plurals.hours, minutes / 60, minutes / 60)
    } else {
        pluralStringResource(R.plurals.minutes, minutes, minutes)
    }

/** "15 Min", "1 Hr", "24 Hr": the same length on a key. */
@Composable
fun shortDurationLabel(minutes: Int): String =
    if (minutes % 60 == 0) {
        pluralStringResource(R.plurals.short_hours, minutes / 60, minutes / 60)
    } else {
        pluralStringResource(R.plurals.short_minutes, minutes, minutes)
    }
