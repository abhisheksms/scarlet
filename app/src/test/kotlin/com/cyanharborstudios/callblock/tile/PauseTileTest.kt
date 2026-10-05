package com.cyanharborstudios.callblock.tile

import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.ScreeningSettings
import com.cyanharborstudios.callblock.core.rules.WeekSchedule
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

/** The tile's face for every state the app can be in, and what a tap does there. */
class PauseTileTest {

    private val zone = ZoneId.of("Asia/Kolkata")

    /** Monday 5 October 2026, 12:00 in India. */
    private val noon = ZonedDateTime.of(2026, 10, 5, 12, 0, 0, 0, zone).toInstant().toEpochMilli()
    private val minute = 60_000L

    private fun face(settings: ScreeningSettings, roleAvailable: Boolean = true, roleHeld: Boolean = true, now: Long = noon) =
        PauseTile.face(settings, roleAvailable, roleHeld, now, zone)

    /** Monday's hour from noon asks for [mode]. */
    private fun mondayNoon(mode: Mode, lever: Mode) = ScreeningSettings(
        mode = lever,
        schedule = WeekSchedule.EMPTY.with(listOf(DayOfWeek.MONDAY), 12..12, mode),
        scheduleOn = true,
    )

    @Test
    fun `while filtering the tile is lit and a tap pauses`() {
        assertEquals(TileFace(true, TileStatus.BLOCK, TileTap.PAUSE), face(ScreeningSettings(mode = Mode.BLOCK)))
        assertEquals(TileFace(true, TileStatus.SILENCE, TileTap.PAUSE), face(ScreeningSettings(mode = Mode.SILENCE)))
    }

    @Test
    fun `while paused the tile is dark and a tap resumes`() {
        val paused = ScreeningSettings(mode = Mode.BLOCK, timerUntilMillis = noon + minute)
        assertEquals(TileFace(false, TileStatus.PAUSED, TileTap.RESUME), face(paused))
    }

    @Test
    fun `a pause that has ended counts for nothing`() {
        val ended = ScreeningSettings(mode = Mode.BLOCK, timerUntilMillis = noon)
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
        // Settings that filter nothing outrank a lost role; a lost role outranks a pause; a device that cannot screen outranks all.
        val pausedOff = ScreeningSettings(mode = Mode.OFF, timerUntilMillis = noon + minute)
        assertEquals(TileStatus.OFF, face(pausedOff, roleHeld = false).status)
        val pausedNoRole = ScreeningSettings(mode = Mode.BLOCK, timerUntilMillis = noon + minute)
        assertEquals(TileStatus.ROLE_MISSING, face(pausedNoRole, roleHeld = false).status)
        assertEquals(TileStatus.CANNOT_SCREEN, face(pausedNoRole, roleAvailable = false, roleHeld = false).status)
    }

    @Test
    fun `one tap pauses for an hour`() {
        assertEquals(noon + 60 * minute, PauseTile.pauseEnd(noon))
    }

    @Test
    fun `a timer at block lights the tile while the lever is at off`() {
        val timed = ScreeningSettings(mode = Mode.OFF, timerMode = Mode.BLOCK, timerUntilMillis = noon + minute)
        assertEquals(TileFace(true, TileStatus.BLOCK, TileTap.PAUSE), face(timed))
        // Once it has ended the lever's Off is back, and there is nothing to pause.
        assertEquals(TileFace(false, TileStatus.OFF, TileTap.OPEN_APP), face(timed, now = noon + minute))
    }

    @Test
    fun `an hour the schedule set to block lights the tile, and a lost role is said even then`() {
        val scheduled = mondayNoon(Mode.BLOCK, lever = Mode.OFF)
        assertEquals(TileFace(true, TileStatus.BLOCK, TileTap.PAUSE), face(scheduled))
        assertEquals(TileStatus.ROLE_MISSING, face(scheduled, roleHeld = false).status)
        // An hour later the schedule asks for nothing: the lever's Off, and a tap opens the app.
        assertEquals(TileFace(false, TileStatus.OFF, TileTap.OPEN_APP), face(scheduled, now = noon + 60 * minute))
    }

    @Test
    fun `an hour the schedule set to off is not a pause`() {
        val scheduledOff = mondayNoon(Mode.OFF, lever = Mode.BLOCK)
        assertEquals(TileFace(false, TileStatus.OFF, TileTap.OPEN_APP), face(scheduledOff))
        assertEquals(TileFace(true, TileStatus.BLOCK, TileTap.PAUSE), face(scheduledOff, now = noon + 60 * minute))
    }
}
