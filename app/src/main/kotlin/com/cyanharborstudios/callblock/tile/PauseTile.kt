package com.cyanharborstudios.callblock.tile

import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.ModeClock
import com.cyanharborstudios.callblock.core.rules.ModeSource
import com.cyanharborstudios.callblock.core.rules.ScreeningSettings
import java.time.ZoneId

/** What the Quick Settings tile shows, and what one tap on it does. */
data class TileFace(
    /** True while calls are being filtered: the tile is lit. */
    val filtering: Boolean,
    val status: TileStatus,
    val tap: TileTap,
)

enum class TileStatus { CANNOT_SCREEN, OFF, ROLE_MISSING, PAUSED, SILENCE, BLOCK }

enum class TileTap { OPEN_APP, RESUME, PAUSE }

/**
 * The tile's logic, with no Android in it. The order of the states is the one Home's
 * display uses: a device that cannot screen, then settings that filter nothing, then the
 * role lost to another app, then a pause, then the mode in effect now, which a timer or
 * the schedule may have chosen.
 */
object PauseTile {

    /** One tap pauses filtering for this long: the same hour as the second key on Home. */
    const val PAUSE_MINUTES = 60

    fun face(settings: ScreeningSettings, roleAvailable: Boolean, roleHeld: Boolean, nowMillis: Long, zone: ZoneId): TileFace {
        if (!roleAvailable) return TileFace(false, TileStatus.CANNOT_SCREEN, TileTap.OPEN_APP)
        if (!settings.asksToFilter(nowMillis)) return TileFace(false, TileStatus.OFF, TileTap.OPEN_APP)
        if (!roleHeld) return TileFace(false, TileStatus.ROLE_MISSING, TileTap.OPEN_APP)
        val now = ModeClock.at(settings, nowMillis, zone)
        return when (now.mode) {
            // A pause can be resumed from here. An hour the schedule set to Off, or the lever
            // at Off between the schedule's hours, is not a pause: the app is the place for that.
            Mode.OFF -> if (now.source == ModeSource.TIMER) TileFace(false, TileStatus.PAUSED, TileTap.RESUME) else TileFace(false, TileStatus.OFF, TileTap.OPEN_APP)
            Mode.SILENCE -> TileFace(true, TileStatus.SILENCE, TileTap.PAUSE)
            Mode.BLOCK -> TileFace(true, TileStatus.BLOCK, TileTap.PAUSE)
        }
    }

    /** When a pause started now ends. */
    fun pauseEnd(nowMillis: Long): Long = nowMillis + PAUSE_MINUTES * 60_000L
}
