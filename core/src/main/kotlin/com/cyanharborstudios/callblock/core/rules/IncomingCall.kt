package com.cyanharborstudios.callblock.core.rules

import com.cyanharborstudios.callblock.core.numbers.PhoneNumber

/** The facts about one incoming call that rules may look at. */
data class IncomingCall(
    val number: PhoneNumber,
    val receivedAtMillis: Long,
    /** True when the caller is saved in the user's contacts. */
    val callerIsContact: Boolean = false,
    /** When the app last blocked or silenced this number, or null if it never has. */
    val lastHandledAtMillis: Long? = null,
)
