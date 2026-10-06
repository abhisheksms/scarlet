package com.cyanharborstudios.callblock.ui.parts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Which stop the lever's handle takes when a finger lets go of it. Three stops: 0, 1 and 2. */
class LeverHandleTest {

    private val flick = 6f
    private val lastStop = 2

    private fun letGo(position: Float, speed: Float = 0f) = LeverHandle.stopWhenLetGo(position, speed, flick, lastStop)

    @Test
    fun `let go slowly, the handle takes the nearest stop`() {
        assertEquals(0, letGo(0f))
        assertEquals(0, letGo(0.49f))
        assertEquals(1, letGo(0.51f))
        assertEquals(1, letGo(1.2f))
        assertEquals(2, letGo(1.6f))
        assertEquals(2, letGo(2f))
        // Just under a flick, either way, is still slow: the nearest stop, not the next one.
        assertEquals(0, letGo(0.4f, speed = flick - 0.1f))
        assertEquals(2, letGo(1.6f, speed = -(flick - 0.1f)))
    }

    @Test
    fun `a flick sends the handle on to the next stop that way, however short it was`() {
        assertEquals(1, letGo(0.2f, speed = flick))
        assertEquals(2, letGo(1.2f, speed = flick))
        assertEquals(1, letGo(1.8f, speed = -flick))
        assertEquals(0, letGo(0.8f, speed = -flick))
        // Flicked from the first stop almost to the second, it stops at the second.
        assertEquals(1, letGo(0.9f, speed = flick * 4))
        // Flicked well past the second stop, it goes on to the third.
        assertEquals(2, letGo(1.3f, speed = flick * 4))
    }

    @Test
    fun `a flick that has barely left a stop changes nothing`() {
        val barely = LeverHandle.FLICK_PLAY - 0.05f
        assertEquals(0, letGo(barely, speed = flick * 4))
        assertEquals(1, letGo(1 + barely, speed = flick * 4))
        assertEquals(2, letGo(2 - barely, speed = -flick * 4))
        assertEquals(1, letGo(1 - barely, speed = -flick * 4))
        // A little further and it is a flick.
        val enough = LeverHandle.FLICK_PLAY + 0.05f
        assertEquals(1, letGo(enough, speed = flick * 4))
        assertEquals(1, letGo(2 - enough, speed = -flick * 4))
    }

    @Test
    fun `a flick back towards the stop the handle left returns it there`() {
        assertEquals(0, letGo(0.4f, speed = -flick * 2))
        assertEquals(2, letGo(1.6f, speed = flick * 2))
    }

    @Test
    fun `the handle never leaves the slot`() {
        assertEquals(2, letGo(2f, speed = flick * 10))
        assertEquals(0, letGo(0f, speed = -flick * 10))
        for (tenths in -10..30) {
            for (speed in listOf(-flick * 10, -flick, 0f, flick, flick * 10)) {
                val stop = letGo(tenths / 10f, speed)
                assertTrue("at ${tenths / 10f} moving $speed: stop $stop", stop in 0..lastStop)
            }
        }
    }
}
