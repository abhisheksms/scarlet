package com.cyanharborstudios.callblock.core.rules

/** What happens to an incoming call. */
enum class Action {
    /** The call rings as usual. */
    ALLOW,

    /** The call comes through but the phone does not ring. */
    SILENCE,

    /** The call is rejected before the phone rings. */
    BLOCK,
}

/** The outcome for one call, and the id of the rule that decided it. */
data class Decision(val action: Action, val ruleId: String)
