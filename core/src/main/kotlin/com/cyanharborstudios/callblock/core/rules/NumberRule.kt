package com.cyanharborstudios.callblock.core.rules

/**
 * One of the user's own rules about how a number starts: every number whose key begins
 * with [start] ("+91804567", or "+92" for a whole country) always rings, or is always
 * blocked. [action] is ALLOW or BLOCK.
 */
data class NumberRule(val start: String, val action: Action)

/** The user's list of number rules: kept newest first, one rule a start, and stored as one line of text. */
object NumberRules {

    /** [rules] with [rule] put first. A rule that was there for the same start is replaced. */
    fun with(rules: List<NumberRule>, rule: NumberRule): List<NumberRule> =
        listOf(rule) + rules.filter { it.start != rule.start }

    fun without(rules: List<NumberRule>, start: String): List<NumberRule> = rules.filter { it.start != start }

    /** "+91804567=BLOCK;+92=BLOCK": the list for the settings file. */
    fun encode(rules: List<NumberRule>): String = rules.joinToString(";") { "${it.start}=${it.action.name}" }

    /** The list back from [encode]'s text. Anything that is not a start and an action it knows is left out. */
    fun decode(text: String?): List<NumberRule> {
        val rules = mutableListOf<NumberRule>()
        for (part in text.orEmpty().split(";")) {
            val start = part.substringBefore("=")
            val action = when (part.substringAfter("=", "")) {
                Action.ALLOW.name -> Action.ALLOW
                Action.BLOCK.name -> Action.BLOCK
                else -> continue
            }
            if (isStart(start) && rules.none { it.start == start }) rules += NumberRule(start, action)
        }
        return rules
    }

    /** A start is "+" and at least one digit, nothing else. */
    fun isStart(text: String): Boolean = text.length > 1 && text[0] == '+' && text.drop(1).all { it in '0'..'9' }

    /**
     * A plain list of starts, as the blocked frequent callers are kept: "+918046512;+919876543210".
     * A frequent caller may also be a short code, so here a start is anything of digits with
     * an optional "+" in front.
     */
    fun encodeStarts(starts: List<String>): String = starts.joinToString(";")

    fun decodeStarts(text: String?): List<String> =
        text.orEmpty().split(";").filter { isStart(it) || (it.isNotEmpty() && it.all { c -> c in '0'..'9' }) }.distinct()

    /** [starts] with [start] put first, once. */
    fun withStart(starts: List<String>, start: String): List<String> = listOf(start) + starts.filter { it != start }
}
