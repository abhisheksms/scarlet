package com.cyanharborstudios.callblock.core.numbers

/** A caller's number, worked out once from the text Android gives the app. */
data class PhoneNumber(
    /** The number exactly as it arrived. Empty when the call carried none. */
    val raw: String,
    /**
     * One spelling per number, however it was written: "+918012345678" for both
     * "+91 80 1234 5678" and "080 1234 5678" on an Indian phone. The allow list and
     * repeat-call matching compare keys. Empty when there is no number.
     */
    val key: String,
    /** True when the number's country is not the phone's own. */
    val isInternational: Boolean,
    /** Spaced and grouped for reading. */
    val display: String,
)
