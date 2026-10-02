package com.cyanharborstudios.callblock.core.stats

/** Round totals of handled calls worth marking. */
object Milestones {

    val LADDER: List<Int> = listOf(10, 25, 50, 100, 250, 500, 1_000, 2_500, 5_000, 10_000, 25_000, 50_000, 100_000)

    /** Where [total] sits between the last milestone reached and the next one. */
    fun progress(total: Int): MilestoneProgress {
        val reached = LADDER.lastOrNull { it <= total }
        val next = LADDER.firstOrNull { it > total }
        val from = reached ?: 0
        val fraction = if (next == null) 1f else (total - from).toFloat() / (next - from)
        return MilestoneProgress(reached, next, fraction)
    }

    /** The milestone passed when the total went from [before] to [after], or null. */
    fun crossed(before: Int, after: Int): Int? = LADDER.lastOrNull { it in (before + 1)..after }
}

data class MilestoneProgress(
    /** The highest milestone reached so far, or null before the first. */
    val reached: Int?,
    /** The next milestone, or null past the top of the ladder. */
    val next: Int?,
    /** 0 to 1: how far along from [reached] to [next]. */
    val fractionToNext: Float,
)
