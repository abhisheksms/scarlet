package com.cyanharborstudios.callblock.core.rules

/**
 * Builds the ordered rule list for the user's current settings.
 *
 * Order matters: the engine stops at the first rule that matches. The user's own choices
 * come first (a contact, a pause, the allow list), then India's two number series, then
 * the automatic passes (a repeat caller, a domestic number when only callers from abroad
 * are filtered), and last what happens to an unknown caller that no earlier rule spoke
 * for. Only two rules can block or silence: the 140 rule, once the user has asked for it,
 * and the last one.
 */
object RuleBook {

    const val OFF = "off"
    const val CONTACT = "contact"
    const val PAUSED = "paused"
    const val ALLOW_LIST = "allow-list"
    const val IN_160_SERVICE = "in-160-service"
    const val IN_140_PROMOTIONAL = "in-140-promotional"
    const val REPEAT_CALL = "repeat-call"
    const val DOMESTIC_OUT_OF_SCOPE = "domestic-out-of-scope"
    const val UNKNOWN_CALLER = "unknown-caller"

    /** India's country code and the two series TRAI reserves for commercial calls. */
    const val INDIA = 91
    const val SERVICE_SERIES = "160"
    const val PROMOTIONAL_SERIES = "140"

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
        // India's 160 series always rings: TRAI reserves it for service and transactional
        // calls (1600: banks, insurers and other regulated financial entities, government
        // bodies; 1601: utilities, couriers, logistics) and bars a call-management app from
        // blocking it. The 140 series (promotional calls) is blocked only once the user asks.
        rules += Rule(IN_160_SERVICE, Condition.NumberInSeries(INDIA, SERVICE_SERIES), Action.ALLOW)
        if (settings.promotionalSeriesBlocked) {
            rules += Rule(IN_140_PROMOTIONAL, Condition.NumberInSeries(INDIA, PROMOTIONAL_SERIES), Action.BLOCK)
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
