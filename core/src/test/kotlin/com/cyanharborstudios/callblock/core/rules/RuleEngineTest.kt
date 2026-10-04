package com.cyanharborstudios.callblock.core.rules

import com.cyanharborstudios.callblock.core.numbers.PhoneNumbers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    // --- India's number series ---

    private val serviceCall = "+911600123456" // 1600: a bank, an insurer or a government body
    private val promotionalCall = "+911401234567" // 140: a registered telemarketer

    @Test
    fun `a call from India's 160 series rings in block mode and in silence mode`() {
        for (mode in listOf(Mode.BLOCK, Mode.SILENCE)) {
            val decision = decide(ScreeningSettings(mode = mode), call(number = serviceCall))
            assertEquals(Decision(Action.ALLOW, RuleBook.IN_160_SERVICE), decision)
        }
        // 1601 (utilities, couriers, logistics) is the same series.
        assertEquals(RuleBook.IN_160_SERVICE, decide(ScreeningSettings(mode = Mode.BLOCK), call(number = "+911601234567")).ruleId)
    }

    @Test
    fun `the 140 series is treated like any unknown caller until the user asks for it to be blocked`() {
        assertEquals(Decision(Action.BLOCK, RuleBook.UNKNOWN_CALLER), decide(ScreeningSettings(mode = Mode.BLOCK), call(number = promotionalCall)))
        assertEquals(Decision(Action.SILENCE, RuleBook.UNKNOWN_CALLER), decide(ScreeningSettings(mode = Mode.SILENCE), call(number = promotionalCall)))
    }

    @Test
    fun `once asked, a call from India's 140 series is blocked, even in silence mode`() {
        for (mode in listOf(Mode.BLOCK, Mode.SILENCE)) {
            val settings = ScreeningSettings(mode = mode, promotionalSeriesBlocked = true)
            assertEquals(Decision(Action.BLOCK, RuleBook.IN_140_PROMOTIONAL), decide(settings, call(number = promotionalCall)))
        }
    }

    @Test
    fun `the series are recognised however the number is written`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, promotionalSeriesBlocked = true)
        for (written in listOf("1401234567", "01401234567", "+91 140 123 4567")) {
            assertEquals(written, RuleBook.IN_140_PROMOTIONAL, decide(settings, call(number = written)).ruleId)
        }
        for (written in listOf("1600123456", "01600123456", "+91 1600 123 456")) {
            assertEquals(written, RuleBook.IN_160_SERVICE, decide(settings, call(number = written)).ruleId)
        }
    }

    @Test
    fun `the series rules apply only to Indian numbers that start with the series`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, promotionalSeriesBlocked = true)
        // A mobile number whose digits merely contain 140 and 160.
        assertEquals(RuleBook.UNKNOWN_CALLER, decide(settings, call(number = "+919140160123")).ruleId)
        // The same digits under another country's code.
        assertEquals(RuleBook.UNKNOWN_CALLER, decide(settings, call(number = "+11600123456")).ruleId)
        assertEquals(RuleBook.UNKNOWN_CALLER, decide(settings, call(number = "+11401234567")).ruleId)
    }

    @Test
    fun `the user's own choices come before the series rules`() {
        // A 140 number the user put on the allow list rings, and so does one that calls during a pause.
        val allowed = ScreeningSettings(mode = Mode.BLOCK, allowListEnabled = true, promotionalSeriesBlocked = true)
        val allowList = mapOf<String, Long?>(promotionalCall to null)
        assertEquals(RuleBook.ALLOW_LIST, decide(allowed, call(number = promotionalCall), allowList).ruleId)
        val paused = ScreeningSettings(mode = Mode.BLOCK, pausedUntilMillis = noon + minute, promotionalSeriesBlocked = true)
        assertEquals(RuleBook.PAUSED, decide(paused, call(number = promotionalCall)).ruleId)
    }

    @Test
    fun `the series rules come before the automatic passes`() {
        // Neither "international only" nor a repeat call lets a 140 number through.
        val international = ScreeningSettings(mode = Mode.BLOCK, scope = Scope.INTERNATIONAL_ONLY, promotionalSeriesBlocked = true)
        assertEquals(RuleBook.IN_140_PROMOTIONAL, decide(international, call(number = promotionalCall)).ruleId)
        val repeats = ScreeningSettings(mode = Mode.BLOCK, repeatCallsRing = true, promotionalSeriesBlocked = true)
        val repeatCall = call(number = promotionalCall, lastHandledAt = noon - minute)
        assertEquals(RuleBook.IN_140_PROMOTIONAL, decide(repeats, repeatCall).ruleId)
    }

    @Test
    fun `when the switch is off nothing is blocked, not even the 140 series`() {
        val off = ScreeningSettings(mode = Mode.OFF, promotionalSeriesBlocked = true)
        assertEquals(Decision(Action.ALLOW, RuleBook.OFF), decide(off, call(number = promotionalCall)))
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
            promotionalSeriesBlocked = true,
        )
        assertEquals(
            listOf(
                RuleBook.CONTACT,
                RuleBook.PAUSED,
                RuleBook.ALLOW_LIST,
                RuleBook.IN_160_SERVICE,
                RuleBook.IN_140_PROMOTIONAL,
                RuleBook.REPEAT_CALL,
                RuleBook.DOMESTIC_OUT_OF_SCOPE,
                RuleBook.UNKNOWN_CALLER,
            ),
            RuleBook.build(everything, emptyMap()).map { it.id },
        )
        // The 160 rule is always there; everything the user can switch is off.
        assertEquals(
            listOf(RuleBook.CONTACT, RuleBook.IN_160_SERVICE, RuleBook.UNKNOWN_CALLER),
            RuleBook.build(ScreeningSettings(mode = Mode.BLOCK), emptyMap()).map { it.id },
        )
        assertEquals(listOf(RuleBook.OFF), RuleBook.build(ScreeningSettings(mode = Mode.OFF), emptyMap()).map { it.id })
    }

    @Test
    fun `only the 140 rule and the last rule of a list can block or silence`() {
        val rules = RuleBook.build(
            ScreeningSettings(
                mode = Mode.BLOCK,
                scope = Scope.INTERNATIONAL_ONLY,
                pausedUntilMillis = noon,
                repeatCallsRing = true,
                allowListEnabled = true,
                promotionalSeriesBlocked = true,
            ),
            emptyMap(),
        )
        assertEquals(
            listOf(RuleBook.IN_140_PROMOTIONAL, RuleBook.UNKNOWN_CALLER),
            rules.filter { it.action != Action.ALLOW }.map { it.id },
        )
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
