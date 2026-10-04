package com.cyanharborstudios.callblock.tile

import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.ScreeningSettings
import org.junit.Assert.assertEquals
import org.junit.Test

/** The tile's face for every state the app can be in, and what a tap does there. */
class PauseTileTest {

    private val noon = 1_000_000_000L
    private val minute = 60_000L

    private fun face(settings: ScreeningSettings, roleAvailable: Boolean = true, roleHeld: Boolean = true, now: Long = noon) =
        PauseTile.face(settings, roleAvailable, roleHeld, now)

    @Test
    fun `while filtering the tile is lit and a tap pauses`() {
        assertEquals(TileFace(true, TileStatus.BLOCK, TileTap.PAUSE), face(ScreeningSettings(mode = Mode.BLOCK)))
        assertEquals(TileFace(true, TileStatus.SILENCE, TileTap.PAUSE), face(ScreeningSettings(mode = Mode.SILENCE)))
    }

    @Test
    fun `while paused the tile is dark and a tap resumes`() {
        val paused = ScreeningSettings(mode = Mode.BLOCK, pausedUntilMillis = noon + minute)
        assertEquals(TileFace(false, TileStatus.PAUSED, TileTap.RESUME), face(paused))
    }

    @Test
    fun `a pause that has ended counts for nothing`() {
        val ended = ScreeningSettings(mode = Mode.BLOCK, pausedUntilMillis = noon)
        assertEquals(TileFace(true, TileStatus.BLOCK, TileTap.PAUSE), face(ended, now = noon))
    }

    @Test
    fun `when nothing is filtered a tap opens the app`() {
        assertEquals(TileFace(false, TileStatus.OFF, TileTap.OPEN_APP), face(ScreeningSettings(mode = Mode.OFF)))
        assertEquals(TileFace(false, TileStatus.ROLE_MISSING, TileTap.OPEN_APP), face(ScreeningSettings(mode = Mode.BLOCK), roleHeld = false))
        assertEquals(TileFace(false, TileStatus.CANNOT_SCREEN, TileTap.OPEN_APP), face(ScreeningSettings(mode = Mode.BLOCK), roleAvailable = false))
    }

    @Test
    fun `the states come in Home's order`() {
        // Off outranks a lost role; a lost role outranks a pause; a device that cannot screen outranks all.
        val pausedOff = ScreeningSettings(mode = Mode.OFF, pausedUntilMillis = noon + minute)
        assertEquals(TileStatus.OFF, face(pausedOff, roleHeld = false).status)
        val pausedNoRole = ScreeningSettings(mode = Mode.BLOCK, pausedUntilMillis = noon + minute)
        assertEquals(TileStatus.ROLE_MISSING, face(pausedNoRole, roleHeld = false).status)
        assertEquals(TileStatus.CANNOT_SCREEN, face(pausedNoRole, roleAvailable = false, roleHeld = false).status)
    }

    @Test
    fun `one tap pauses for an hour`() {
        assertEquals(noon + 60 * minute, PauseTile.pauseEnd(noon))
    }
}
