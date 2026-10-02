package com.cyanharborstudios.callblock.core.stats

import com.cyanharborstudios.callblock.core.rules.Action

/** A call the app blocked or silenced. Allowed calls are never recorded. */
data class HandledCall(
    val numberKey: String,
    val numberRaw: String,
    val atMillis: Long,
    /** [Action.BLOCK] or [Action.SILENCE]. */
    val action: Action,
)
