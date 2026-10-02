package com.cyanharborstudios.callblock.ui

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.runtime.LaunchedEffect
import com.cyanharborstudios.callblock.core.numbers.PhoneNumbers
import com.cyanharborstudios.callblock.core.time.TimeText
import com.cyanharborstudios.callblock.screening.homeRegion
import kotlinx.coroutines.delay
import java.time.ZoneId

/**
 * Time text that follows the phone's 12-hour / 24-hour setting, its locale and its time
 * zone. The setting is read again each time the app returns to the foreground, because the
 * user changes it in system settings, not here.
 */
@Composable
fun rememberTimeText(): TimeText {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    var is24Hour by remember { mutableStateOf(DateFormat.is24HourFormat(context)) }
    var zone by remember { mutableStateOf(ZoneId.systemDefault()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        is24Hour = DateFormat.is24HourFormat(context)
        zone = ZoneId.systemDefault()
    }
    return remember(is24Hour, locale, zone) { TimeText(is24Hour, locale, zone) }
}

/** The parser for showing stored numbers, set to the phone's own country. */
@Composable
fun rememberPhoneNumbers(): PhoneNumbers {
    val context = LocalContext.current
    return remember { PhoneNumbers(homeRegion(context)) }
}

/**
 * The current time, refreshed every few seconds while a screen is showing, so "paused
 * until", "today" and expiring allow entries stay true without the user doing anything.
 */
@Composable
fun rememberNowMillis(): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { now = System.currentTimeMillis() }
    LaunchedEffect(Unit) {
        while (true) {
            delay(NOW_REFRESH_MILLIS)
            now = System.currentTimeMillis()
        }
    }
    return now
}

private const val NOW_REFRESH_MILLIS = 5_000L
