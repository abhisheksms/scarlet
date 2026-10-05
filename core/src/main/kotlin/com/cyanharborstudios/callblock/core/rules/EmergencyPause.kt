package com.cyanharborstudios.callblock.core.rules

/**
 * After the user calls an emergency number, every call rings for a day.
 *
 * The people who ring back after such a call (a control room, an ambulance crew, a
 * hospital) do so from numbers nobody has saved, and those are the calls this app stops.
 * So the app pauses itself: an ordinary pause, shown on Home with its end time, which
 * Resume or a move of the lever ends early.
 */
object EmergencyPause {

    const val MINUTES = 24 * 60

    /**
     * The settings after a call to an emergency number at [atMillis]. Unchanged when the
     * settings stop no call anyway: there is nothing to pause.
     */
    fun after(settings: ScreeningSettings, atMillis: Long): ScreeningSettings {
        if (!settings.asksToFilter(atMillis)) return settings
        return settings.copy(timerMode = Mode.OFF, timerUntilMillis = atMillis + MINUTES * 60_000L)
    }
}
