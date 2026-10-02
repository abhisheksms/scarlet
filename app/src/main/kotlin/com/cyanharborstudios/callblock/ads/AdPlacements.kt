package com.cyanharborstudios.callblock.ads

/**
 * Where and when ads may appear. Plain values and one function, so the policy can be read
 * in one place and tested without a device. See knowledge-base/adr/ADR-006.
 */
object AdPlacements {

    enum class FullScreenTrigger {
        /** No full-screen ads at all. */
        NEVER,

        /** After the user has finished with a screen and goes back from it. */
        ON_LEAVING,

        /** On the tap that opens a screen, before the screen. What the reference app does. */
        ON_OPENING,
    }

    /** The founder's gate G6 in PLAN.md decides this before live ad ids go in. */
    val FULL_SCREEN_TRIGGER = FullScreenTrigger.ON_LEAVING

    /** The screens a full-screen ad is attached to (route names). */
    val FULL_SCREEN_SCREENS = setOf("history", "statistics")

    /** At most one full-screen ad in this long, and none this soon after the app starts. */
    const val FULL_SCREEN_MIN_GAP_MILLIS = 3 * 60_000L

    /**
     * Whether moving from screen [from] to screen [to] is a moment for a full-screen ad.
     * [lastShownAtMillis] is when one was last shown, or when the app started.
     */
    fun wantsFullScreenAd(
        from: String?,
        to: String?,
        nowMillis: Long,
        lastShownAtMillis: Long,
        trigger: FullScreenTrigger = FULL_SCREEN_TRIGGER,
    ): Boolean {
        if (nowMillis - lastShownAtMillis < FULL_SCREEN_MIN_GAP_MILLIS) return false
        return when (trigger) {
            FullScreenTrigger.NEVER -> false
            FullScreenTrigger.ON_LEAVING -> from in FULL_SCREEN_SCREENS && to !in FULL_SCREEN_SCREENS
            FullScreenTrigger.ON_OPENING -> to in FULL_SCREEN_SCREENS && from !in FULL_SCREEN_SCREENS
        }
    }
}
