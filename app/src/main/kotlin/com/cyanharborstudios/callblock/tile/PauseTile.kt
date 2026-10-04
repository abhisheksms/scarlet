package com.cyanharborstudios.callblock.tile

import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.ScreeningSettings

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
 * display uses: a device that cannot screen, then the lever at Off, then the role lost
 * to another app, then a pause, then the mode.
 */
object PauseTile {

    /** One tap pauses filtering for this long: the same hour as the second key on Home. */
    const val PAUSE_MINUTES = 60

    fun face(settings: ScreeningSettings, roleAvailable: Boolean, roleHeld: Boolean, nowMillis: Long): TileFace = when {
        !roleAvailable -> TileFace(false, TileStatus.CANNOT_SCREEN, TileTap.OPEN_APP)
        settings.mode == Mode.OFF -> TileFace(false, TileStatus.OFF, TileTap.OPEN_APP)
        !roleHeld -> TileFace(false, TileStatus.ROLE_MISSING, TileTap.OPEN_APP)
        nowMillis < settings.pausedUntilMillis -> TileFace(false, TileStatus.PAUSED, TileTap.RESUME)
        settings.mode == Mode.SILENCE -> TileFace(true, TileStatus.SILENCE, TileTap.PAUSE)
        else -> TileFace(true, TileStatus.BLOCK, TileTap.PAUSE)
    }

    /** When a pause started now ends. */
    fun pauseEnd(nowMillis: Long): Long = nowMillis + PAUSE_MINUTES * 60_000L
}
