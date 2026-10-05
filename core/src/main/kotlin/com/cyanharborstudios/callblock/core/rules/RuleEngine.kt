package com.cyanharborstudios.callblock.core.rules

/** Decides what to do with a call by walking an ordered rule list. First match wins. */
object RuleEngine {

    /** The rule id reported when no rule in the list matched. */
    const val NO_RULE_MATCHED = "no-rule-matched"

    /** A call that matches no rule is allowed. */
    fun decide(rules: List<Rule>, call: IncomingCall): Decision {
        for (rule in rules) {
            if (matches(rule.condition, call)) {
                return Decision(rule.action, rule.id)
            }
        }
        return Decision(Action.ALLOW, NO_RULE_MATCHED)
    }

    fun matches(condition: Condition, call: IncomingCall): Boolean = when (condition) {
        Condition.Always -> true
        Condition.CallerIsContact -> call.callerIsContact
        is Condition.NumberAllowed -> isOnAllowList(condition, call)
        is Condition.CalledAgainWithin -> calledAgainWithin(condition, call)
        is Condition.DialledWithin -> dialledWithin(condition, call)
        Condition.NumberIsDomestic -> !call.number.isInternational
        is Condition.NumberInSeries -> inSeries(condition, call)
    }

    private fun inSeries(condition: Condition.NumberInSeries, call: IncomingCall): Boolean =
        call.number.key.startsWith("+${condition.countryCode}${condition.nationalPrefix}")

    private fun isOnAllowList(condition: Condition.NumberAllowed, call: IncomingCall): Boolean {
        val key = call.number.key
        if (key.isEmpty() || key !in condition.expiryByNumberKey) return false
        val expiresAt = condition.expiryByNumberKey[key]
        return expiresAt == null || call.receivedAtMillis < expiresAt
    }

    private fun dialledWithin(condition: Condition.DialledWithin, call: IncomingCall): Boolean {
        val lastDialledAt = call.lastDialledAtMillis ?: return false
        val elapsed = call.receivedAtMillis - lastDialledAt
        // A negative gap means the clock was moved back; that is not a call back.
        return elapsed in 0 until condition.windowMillis
    }

    private fun calledAgainWithin(condition: Condition.CalledAgainWithin, call: IncomingCall): Boolean {
        val lastHandledAt = call.lastHandledAtMillis ?: return false
        val elapsed = call.receivedAtMillis - lastHandledAt
        // A negative gap means the clock was moved back; that is not a repeat call.
        return elapsed in 0 until condition.windowMillis
    }
}
