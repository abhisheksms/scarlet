package com.cyanharborstudios.callblock.core.rules

/** One line of the rule list: when [condition] holds for a call, do [action]. */
data class Rule(val id: String, val condition: Condition, val action: Action)

/**
 * The kinds of test a rule can make. Each is plain data, so a rule list can be built,
 * compared, printed and stored.
 *
 * To add a kind of rule: add it here and add its line to [RuleEngine.matches]. Nothing
 * else has to change.
 */
sealed interface Condition {

    /** Holds for every call. The last rule in a list uses it. */
    data object Always : Condition

    /** The caller is saved in the user's contacts. */
    data object CallerIsContact : Condition

    /**
     * The caller's number is on the allow list and its entry has not expired.
     * [expiryByNumberKey] maps a number's key to when its entry ends, or to null
     * for an entry that never ends.
     */
    data class NumberAllowed(val expiryByNumberKey: Map<String, Long?>) : Condition

    /** The app blocked or silenced this same number less than [windowMillis] ago. */
    data class CalledAgainWithin(val windowMillis: Long) : Condition

    /** The user called this number themselves less than [windowMillis] ago: it is calling back. */
    data class DialledWithin(val windowMillis: Long) : Condition

    /** The number's key begins with [start]: one of the user's own number rules. */
    data class NumberStartsWith(val start: String) : Condition

    /** The call is from the user's own country, not from abroad. */
    data object NumberIsDomestic : Condition

    /**
     * The number belongs to the country with [countryCode] and its national part starts
     * with [nationalPrefix]: India's 140 and 160 series, for example. A number's key is
     * its E.164 form ("+91" then the national number), so this is a test on the key, and
     * a number written without its country code was already read as the phone's own.
     */
    data class NumberInSeries(val countryCode: Int, val nationalPrefix: String) : Condition
}
