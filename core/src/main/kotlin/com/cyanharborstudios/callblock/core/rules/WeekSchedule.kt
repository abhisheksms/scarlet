package com.cyanharborstudios.callblock.core.rules

import java.time.DayOfWeek

/**
 * What the user wants at each hour of the week: seven days of twenty-four hours, Monday
 * first. An hour either asks for a mode or asks for nothing, and then the lever decides.
 */
data class WeekSchedule(val hours: List<Mode?>) {

    init {
        require(hours.size == HOURS_IN_WEEK) { "a week has $HOURS_IN_WEEK hours, not ${hours.size}" }
    }

    /** The mode asked for at [hour] (0 to 23) of [day], or null when that hour is left to the lever. */
    fun modeAt(day: DayOfWeek, hour: Int): Mode? = hours[indexOf(day, hour)]

    /** True when no hour asks for anything. */
    val isEmpty: Boolean get() = hours.all { it == null }

    /** A copy in which every hour in [hourRange] of each of [days] asks for [mode]; null clears them. */
    fun with(days: Collection<DayOfWeek>, hourRange: IntRange, mode: Mode?): WeekSchedule {
        val changed = hours.toMutableList()
        for (day in days) {
            for (hour in hourRange) {
                if (hour in 0 until HOURS_IN_DAY) changed[indexOf(day, hour)] = mode
            }
        }
        return WeekSchedule(changed)
    }

    /** One character an hour, for the settings file. */
    fun encode(): String = hours.joinToString("") { mode ->
        when (mode) {
            null -> UNSET.toString()
            Mode.OFF -> "O"
            Mode.SILENCE -> "S"
            Mode.BLOCK -> "B"
        }
    }

    companion object {
        const val HOURS_IN_DAY = 24
        const val HOURS_IN_WEEK = 7 * HOURS_IN_DAY
        private const val UNSET = '.'

        val EMPTY = WeekSchedule(List(HOURS_IN_WEEK) { null })

        fun indexOf(day: DayOfWeek, hour: Int): Int = (day.value - 1) * HOURS_IN_DAY + hour

        /** Reads what [encode] wrote. Anything else, or nothing, is an empty schedule. */
        fun decode(text: String?): WeekSchedule {
            if (text == null || text.length != HOURS_IN_WEEK) return EMPTY
            val hours = text.map { character ->
                when (character) {
                    UNSET -> null
                    'O' -> Mode.OFF
                    'S' -> Mode.SILENCE
                    'B' -> Mode.BLOCK
                    else -> return EMPTY
                }
            }
            return WeekSchedule(hours)
        }
    }
}
