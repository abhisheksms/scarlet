package com.cyanharborstudios.callblock.core.rules

import com.cyanharborstudios.callblock.core.numbers.PhoneNumbers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The screening rules, end to end: settings -> rule list -> decision. */
class RuleEngineTest {

    private val numbers = PhoneNumbers("IN")
    private val unknownCaller = "+918012345678"
    private val noon = 1_000_000_000L
    private val minute = 60_000L

    private fun call(
        number: String = unknownCaller,
        at: Long = noon,
        isContact: Boolean = false,
        lastHandledAt: Long? = null,
    ) = IncomingCall(numbers.parse(number), at, isContact, lastHandledAt)

    private fun decide(
        settings: ScreeningSettings,
        call: IncomingCall,
        allowList: Map<String, Long?> = emptyMap(),
    ): Decision = RuleEngine.decide(RuleBook.build(settings, allowList), call)

    // --- the main switch ---

    @Test
    fun `off lets every call ring`() {
        val decision = decide(ScreeningSettings(mode = Mode.OFF), call())
        assertEquals(Decision(Action.ALLOW, RuleBook.OFF), decision)
    }

    @Test
    fun `block mode rejects a caller who is not a contact`() {
        val decision = decide(ScreeningSettings(mode = Mode.BLOCK), call())
        assertEquals(Decision(Action.BLOCK, RuleBook.UNKNOWN_CALLER), decision)
    }

    @Test
    fun `silence mode silences a caller who is not a contact`() {
        val decision = decide(ScreeningSettings(mode = Mode.SILENCE), call())
        assertEquals(Decision(Action.SILENCE, RuleBook.UNKNOWN_CALLER), decision)
    }

    @Test
    fun `a call with no number is treated as an unknown caller`() {
        val decision = decide(ScreeningSettings(mode = Mode.BLOCK), call(number = ""))
        assertEquals(Action.BLOCK, decision.action)
    }

    // --- contacts ---

    @Test
    fun `a contact rings in block mode and in silence mode`() {
        for (mode in listOf(Mode.BLOCK, Mode.SILENCE)) {
            val decision = decide(ScreeningSettings(mode = mode), call(isContact = true))
            assertEquals(Decision(Action.ALLOW, RuleBook.CONTACT), decision)
        }
    }

    // --- pause ---

    @Test
    fun `while paused an unknown caller rings`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, pausedUntilMillis = noon + 30 * minute)
        val decision = decide(settings, call(at = noon + 29 * minute))
        assertEquals(Decision(Action.ALLOW, RuleBook.PAUSED), decision)
    }

    @Test
    fun `the pause ends at its end time, not after it`() {
        val pausedUntil = noon + 30 * minute
        val settings = ScreeningSettings(mode = Mode.BLOCK, pausedUntilMillis = pausedUntil)
        assertEquals(Action.ALLOW, decide(settings, call(at = pausedUntil - 1)).action)
        assertEquals(Action.BLOCK, decide(settings, call(at = pausedUntil)).action)
        assertEquals(Action.BLOCK, decide(settings, call(at = pausedUntil + 1)).action)
    }

    // --- allow list ---

    @Test
    fun `a number on the allow list rings, others are still blocked`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, allowListEnabled = true)
        val allowList = mapOf<String, Long?>(unknownCaller to null)
        assertEquals(Decision(Action.ALLOW, RuleBook.ALLOW_LIST), decide(settings, call(), allowList))
        assertEquals(Action.BLOCK, decide(settings, call(number = "+918099999999"), allowList).action)
    }

    @Test
    fun `the allow list matches a number however it is written`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, allowListEnabled = true)
        val allowList = mapOf<String, Long?>(numbers.parse("080 1234 5678").key to null)
        assertEquals(Action.ALLOW, decide(settings, call(number = "+91 80 1234 5678"), allowList).action)
    }

    @Test
    fun `the allow list does nothing while it is switched off`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, allowListEnabled = false)
        val allowList = mapOf<String, Long?>(unknownCaller to null)
        assertEquals(Action.BLOCK, decide(settings, call(), allowList).action)
    }

    @Test
    fun `a temporary allow lets the number ring until it expires`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, allowListEnabled = true)
        val expiresAt = noon + 60 * minute
        val allowList = mapOf<String, Long?>(unknownCaller to expiresAt)
        assertEquals(Action.ALLOW, decide(settings, call(at = noon), allowList).action)
        assertEquals(Action.ALLOW, decide(settings, call(at = expiresAt - 1), allowList).action)
        assertEquals(Action.BLOCK, decide(settings, call(at = expiresAt), allowList).action)
        assertEquals(Action.BLOCK, decide(settings, call(at = expiresAt + 5 * minute), allowList).action)
    }

    @Test
    fun `a call with no number never matches the allow list`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, allowListEnabled = true)
        assertEquals(Action.BLOCK, decide(settings, call(number = ""), mapOf("" to null)).action)
    }

    // --- repeat calls ---

    @Test
    fun `a number that calls again inside the window rings`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, repeatCallsRing = true, repeatWindowMinutes = 15)
        val decision = decide(settings, call(at = noon, lastHandledAt = noon - 3 * minute))
        assertEquals(Decision(Action.ALLOW, RuleBook.REPEAT_CALL), decision)
    }

    @Test
    fun `a repeat call at or after the window is blocked again`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, repeatCallsRing = true, repeatWindowMinutes = 15)
        assertEquals(Action.ALLOW, decide(settings, call(lastHandledAt = noon - 15 * minute + 1)).action)
        assertEquals(Action.BLOCK, decide(settings, call(lastHandledAt = noon - 15 * minute)).action)
        assertEquals(Action.BLOCK, decide(settings, call(lastHandledAt = noon - 16 * minute)).action)
    }

    @Test
    fun `a first call is not a repeat call`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, repeatCallsRing = true)
        assertEquals(Action.BLOCK, decide(settings, call(lastHandledAt = null)).action)
    }

    @Test
    fun `an earlier call that claims to be in the future is not a repeat call`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, repeatCallsRing = true)
        assertEquals(Action.BLOCK, decide(settings, call(lastHandledAt = noon + minute)).action)
    }

    @Test
    fun `repeat calls are blocked while the setting is off`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, repeatCallsRing = false)
        assertEquals(Action.BLOCK, decide(settings, call(lastHandledAt = noon - minute)).action)
    }

    // --- scope ---

    @Test
    fun `international-only scope lets a domestic unknown caller ring`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, scope = Scope.INTERNATIONAL_ONLY)
        val decision = decide(settings, call(number = "+918012345678"))
        assertEquals(Decision(Action.ALLOW, RuleBook.DOMESTIC_OUT_OF_SCOPE), decision)
    }

    @Test
    fun `international-only scope still blocks a caller from abroad`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, scope = Scope.INTERNATIONAL_ONLY)
        assertEquals(Action.BLOCK, decide(settings, call(number = "+16505551234")).action)
    }

    @Test
    fun `all-unknown scope blocks domestic and international callers alike`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, scope = Scope.ALL_UNKNOWN)
        assertEquals(Action.BLOCK, decide(settings, call(number = "+918012345678")).action)
        assertEquals(Action.BLOCK, decide(settings, call(number = "+16505551234")).action)
    }

    // --- the engine itself ---

    @Test
    fun `the first matching rule wins`() {
        val blockFirst = listOf(
            Rule("everyone", Condition.Always, Action.BLOCK),
            Rule("contact", Condition.CallerIsContact, Action.ALLOW),
        )
        val contactFirst = blockFirst.reversed()
        assertEquals(Decision(Action.BLOCK, "everyone"), RuleEngine.decide(blockFirst, call(isContact = true)))
        assertEquals(Decision(Action.ALLOW, "contact"), RuleEngine.decide(contactFirst, call(isContact = true)))
    }

    @Test
    fun `a call that matches no rule is allowed`() {
        val onlyContacts = listOf(Rule("contact", Condition.CallerIsContact, Action.BLOCK))
        assertEquals(Decision(Action.ALLOW, RuleEngine.NO_RULE_MATCHED), RuleEngine.decide(onlyContacts, call()))
        assertEquals(Decision(Action.ALLOW, RuleEngine.NO_RULE_MATCHED), RuleEngine.decide(emptyList(), call()))
    }

    @Test
    fun `the rule list is built in a fixed order and only with the rules that are switched on`() {
        val everything = ScreeningSettings(
            mode = Mode.SILENCE,
            scope = Scope.INTERNATIONAL_ONLY,
            pausedUntilMillis = noon,
            repeatCallsRing = true,
            allowListEnabled = true,
        )
        assertEquals(
            listOf(
                RuleBook.CONTACT,
                RuleBook.PAUSED,
                RuleBook.ALLOW_LIST,
                RuleBook.REPEAT_CALL,
                RuleBook.DOMESTIC_OUT_OF_SCOPE,
                RuleBook.UNKNOWN_CALLER,
            ),
            RuleBook.build(everything, emptyMap()).map { it.id },
        )
        assertEquals(
            listOf(RuleBook.CONTACT, RuleBook.UNKNOWN_CALLER),
            RuleBook.build(ScreeningSettings(mode = Mode.BLOCK), emptyMap()).map { it.id },
        )
        assertEquals(listOf(RuleBook.OFF), RuleBook.build(ScreeningSettings(mode = Mode.OFF), emptyMap()).map { it.id })
    }

    @Test
    fun `only the last rule of a list can block or silence`() {
        val rules = RuleBook.build(
            ScreeningSettings(Mode.BLOCK, Scope.INTERNATIONAL_ONLY, noon, true, 15, true),
            emptyMap(),
        )
        assertTrue(rules.dropLast(1).all { it.action == Action.ALLOW })
        assertFalse(rules.last().action == Action.ALLOW)
    }

    @Test
    fun `a pause outranks nothing it should not, and a contact outranks everything`() {
        // Paused and on the allow list and a repeat caller: the earliest rule in the list is reported.
        val settings = ScreeningSettings(
            mode = Mode.BLOCK,
            pausedUntilMillis = noon + minute,
            repeatCallsRing = true,
            allowListEnabled = true,
        )
        val allowList = mapOf<String, Long?>(unknownCaller to null)
        val pausedCall = call(lastHandledAt = noon - minute)
        assertEquals(RuleBook.PAUSED, decide(settings, pausedCall, allowList).ruleId)
        assertEquals(RuleBook.CONTACT, decide(settings, pausedCall.copy(callerIsContact = true), allowList).ruleId)
    }
}
