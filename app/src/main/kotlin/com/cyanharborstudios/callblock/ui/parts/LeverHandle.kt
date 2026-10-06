package com.cyanharborstudios.callblock.ui.parts

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * The arithmetic of the lever's handle. Positions are counted in stops from the top: 0 is
 * the first stop, 1 the second, 1.5 halfway between the second and the third.
 */
object LeverHandle {

    /** How far past a stop, in stops, a flick must have carried the handle to send it on to the next. */
    const val FLICK_PLAY = 0.15f

    /**
     * The stop the handle takes when the finger lets go at [position], moving at [speed]
     * stops a second (down is positive).
     *
     * Let go slower than [flick], it takes the nearest stop. Flicked, it goes on to the next
     * stop in that direction, however short the flick, unless it has barely left a stop:
     * a finger that only twitched on the handle changes nothing.
     */
    fun stopWhenLetGo(position: Float, speed: Float, flick: Float, lastStop: Int): Int {
        val stop = when {
            speed >= flick -> ceil(position - FLICK_PLAY).toInt()
            speed <= -flick -> floor(position + FLICK_PLAY).toInt()
            else -> position.roundToInt()
        }
        return stop.coerceIn(0, lastStop)
    }
}
