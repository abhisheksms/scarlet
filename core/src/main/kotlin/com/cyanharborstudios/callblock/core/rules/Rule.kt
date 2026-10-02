package com.cyanharborstudios.callblock.core.rules

/** One line of the rule list: when [condition] holds for a call, do [action]. */
data class Rule(val id: String, val condition: Condition, val action: Action)

/**
 * The kinds of test a rule can make. Each is plain data, so a rule list can be built,
 * compared, printed and stored.
 *
 * To add a kind of rule (a number-prefix rule, say): add it here and add its line to
 * [RuleEngine.matches]. Nothing else has to change.
 */
sealed interface Condition {

    /** Holds for every call. The last rule in a list uses it. */
    data object Always : Condition

    /** The caller is saved in the user's contacts. */
    data object CallerIsContact : Condition

    /** Filtering is paused: holds for calls that arrive before [untilMillis]. */
    data class PausedUntil(val untilMillis: Long) : Condition

    /**
     * The caller's number is on the allow list and its entry has not expired.
     * [expiryByNumberKey] maps a number's key to when its entry ends, or to null
     * for an entry that never ends.
     */
    data class NumberAllowed(val expiryByNumberKey: Map<String, Long?>) : Condition

    /** The app blocked or silenced this same number less than [windowMillis] ago. */
    data class CalledAgainWithin(val windowMillis: Long) : Condition

    /** The call is from the user's own country, not from abroad. */
    data object NumberIsDomestic : Condition
}
