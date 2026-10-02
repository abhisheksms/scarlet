package com.cyanharborstudios.callblock.core.rules

/**
 * Builds the ordered rule list for the user's current settings.
 *
 * Every rule but the last one lets a call through; the last one is what happens to an
 * unknown caller that no earlier rule spoke for. Order matters: the engine stops at the
 * first rule that matches.
 */
object RuleBook {

    const val OFF = "off"
    const val CONTACT = "contact"
    const val PAUSED = "paused"
    const val ALLOW_LIST = "allow-list"
    const val REPEAT_CALL = "repeat-call"
    const val DOMESTIC_OUT_OF_SCOPE = "domestic-out-of-scope"
    const val UNKNOWN_CALLER = "unknown-caller"

    /** [allowList] maps a number's key to when its entry ends (null: never). */
    fun build(settings: ScreeningSettings, allowList: Map<String, Long?>): List<Rule> {
        if (settings.mode == Mode.OFF) {
            return listOf(Rule(OFF, Condition.Always, Action.ALLOW))
        }

        val rules = mutableListOf<Rule>()
        rules += Rule(CONTACT, Condition.CallerIsContact, Action.ALLOW)
        if (settings.pausedUntilMillis > 0) {
            rules += Rule(PAUSED, Condition.PausedUntil(settings.pausedUntilMillis), Action.ALLOW)
        }
        if (settings.allowListEnabled) {
            rules += Rule(ALLOW_LIST, Condition.NumberAllowed(allowList), Action.ALLOW)
        }
        if (settings.repeatCallsRing) {
            val windowMillis = settings.repeatWindowMinutes * 60_000L
            rules += Rule(REPEAT_CALL, Condition.CalledAgainWithin(windowMillis), Action.ALLOW)
        }
        if (settings.scope == Scope.INTERNATIONAL_ONLY) {
            rules += Rule(DOMESTIC_OUT_OF_SCOPE, Condition.NumberIsDomestic, Action.ALLOW)
        }
        val actionForUnknownCaller = if (settings.mode == Mode.BLOCK) Action.BLOCK else Action.SILENCE
        rules += Rule(UNKNOWN_CALLER, Condition.Always, actionForUnknownCaller)
        return rules
    }
}
