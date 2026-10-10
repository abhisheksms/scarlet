package com.cyanharborstudios.callblock.core.plans

import com.cyanharborstudios.callblock.core.rules.Action
import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.NumberRule
import com.cyanharborstudios.callblock.core.rules.Scope
import com.cyanharborstudios.callblock.core.rules.ScreeningSettings
import com.cyanharborstudios.callblock.core.rules.WeekSchedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

/** What each tier holds, and what the engine is allowed to use of the settings. */
class PlansTest {

    private val nights = WeekSchedule.EMPTY.with(listOf(DayOfWeek.MONDAY), 22..23, Mode.BLOCK)

    @Test
    fun `only the free tier shows ads`() {
        assertTrue(Plans.showsAds(Tier.FREE))
        assertFalse(Plans.showsAds(Tier.NO_ADS))
        assertFalse(Plans.showsAds(Tier.PRO))
        assertFalse(Plans.showsAds(Tier.PLUS))
    }

    @Test
    fun `pro and plus have the timer, the schedule and number rules, and only plus has the frequent callers`() {
        for (feature in ProFeature.entries) {
            assertFalse(Plans.has(Tier.FREE, feature))
            assertFalse(Plans.has(Tier.NO_ADS, feature))
            assertTrue(Plans.has(Tier.PLUS, feature))
            assertEquals(feature != ProFeature.FREQUENT_CALLERS, Plans.has(Tier.PRO, feature))
        }
    }

    @Test
    fun `the products a user owns add up to a tier`() {
        assertEquals(Tier.FREE, Plans.tierFor(emptySet()))
        assertEquals(Tier.FREE, Plans.tierFor(setOf("something_else")))
        assertEquals(Tier.NO_ADS, Plans.tierFor(setOf(Plans.NO_ADS_PRODUCT)))
        assertEquals(Tier.PRO, Plans.tierFor(setOf(Plans.PRO_PRODUCT)))
        assertEquals(Tier.PRO, Plans.tierFor(setOf(Plans.NO_ADS_PRODUCT, Plans.PRO_UPGRADE_PRODUCT)))
        // Someone who paid for the upgrade has Pro, whatever became of the first purchase.
        assertEquals(Tier.PRO, Plans.tierFor(setOf(Plans.PRO_UPGRADE_PRODUCT)))
        // A running subscription is Plus, whatever else was bought once.
        assertEquals(Tier.PLUS, Plans.tierFor(setOf(Plans.PLUS_PRODUCT)))
        assertEquals(Tier.PLUS, Plans.tierFor(setOf(Plans.PRO_PRODUCT, Plans.PLUS_PRODUCT)))
    }

    @Test
    fun `plus is the same subscription from every plan below it, and a lapsed one leaves pro's own purchase`() {
        for (from in listOf(Tier.FREE, Tier.NO_ADS, Tier.PRO)) assertEquals(Plans.PLUS_PRODUCT, Plans.productFor(from, Tier.PLUS))
        assertEquals(null, Plans.productFor(Tier.PLUS, Tier.PLUS))
        assertEquals(null, Plans.productFor(Tier.PLUS, Tier.PRO))
        assertEquals(Tier.PRO, Plans.tierFor(listOf(StorePurchase(listOf(Plans.PRO_PRODUCT), paid = true), StorePurchase(listOf(Plans.PLUS_PRODUCT), paid = false))))
        assertEquals(listOf(Plans.PLUS_PRODUCT), Plans.SUBSCRIPTIONS)
    }

    @Test
    fun `without plus there are no blocked frequent callers, none is blocked unasked, and the frequent scope reads as all unknown numbers`() {
        val plus = ScreeningSettings(mode = Mode.BLOCK, scope = Scope.FREQUENT_ONLY, frequentCallers = listOf("+918046512"), frequentAutoBlock = true)
        for (tier in listOf(Tier.FREE, Tier.NO_ADS, Tier.PRO)) {
            assertEquals(ScreeningSettings(mode = Mode.BLOCK, scope = Scope.ALL_UNKNOWN), Plans.limit(plus, tier))
        }
        assertEquals(plus, Plans.limit(plus, Tier.PLUS))
        // The other scopes are every plan's.
        val abroad = ScreeningSettings(mode = Mode.BLOCK, scope = Scope.INTERNATIONAL_ONLY)
        for (tier in Tier.entries) assertEquals(abroad, Plans.limit(abroad, tier))
    }

    @Test
    fun `a purchase still waiting for its payment grants nothing`() {
        val paidNoAds = StorePurchase(listOf(Plans.NO_ADS_PRODUCT), paid = true)
        val pendingPro = StorePurchase(listOf(Plans.PRO_PRODUCT), paid = false)
        val pendingUpgrade = StorePurchase(listOf(Plans.PRO_UPGRADE_PRODUCT), paid = false)
        assertEquals(Tier.FREE, Plans.tierFor(emptyList<StorePurchase>()))
        assertEquals(Tier.FREE, Plans.tierFor(listOf(pendingPro)))
        assertEquals(Tier.NO_ADS, Plans.tierFor(listOf(paidNoAds, pendingUpgrade)))
        assertEquals(Tier.PRO, Plans.tierFor(listOf(paidNoAds, pendingUpgrade.copy(paid = true))))
        assertEquals(Tier.PRO, Plans.tierFor(listOf(pendingPro.copy(paid = true))))
    }

    @Test
    fun `each step up has its product, and there is nothing to buy at or below the user's plan`() {
        assertEquals(Plans.NO_ADS_PRODUCT, Plans.productFor(Tier.FREE, Tier.NO_ADS))
        assertEquals(Plans.PRO_PRODUCT, Plans.productFor(Tier.FREE, Tier.PRO))
        // From No Ads, Pro is the upgrade at the difference, never the full product again.
        assertEquals(Plans.PRO_UPGRADE_PRODUCT, Plans.productFor(Tier.NO_ADS, Tier.PRO))
        for (from in Tier.entries) {
            for (to in Tier.entries.filter { it <= from }) assertEquals("$from to $to", null, Plans.productFor(from, to))
        }
    }

    @Test
    fun `buying the product for a step gives the tier it was for`() {
        assertEquals(Tier.NO_ADS, Plans.tierFor(setOf(Plans.productFor(Tier.FREE, Tier.NO_ADS)!!)))
        assertEquals(Tier.PRO, Plans.tierFor(setOf(Plans.productFor(Tier.FREE, Tier.PRO)!!)))
        assertEquals(Tier.PRO, Plans.tierFor(setOf(Plans.NO_ADS_PRODUCT, Plans.productFor(Tier.NO_ADS, Tier.PRO)!!)))
        assertEquals(Plans.PRODUCTS.toSet(), setOf(Plans.NO_ADS_PRODUCT, Plans.PRO_PRODUCT, Plans.PRO_UPGRADE_PRODUCT))
    }

    @Test
    fun `without pro the schedule is as if switched off, and it is kept for later`() {
        val settings = ScreeningSettings(mode = Mode.OFF, schedule = nights, scheduleOn = true)
        for (tier in listOf(Tier.FREE, Tier.NO_ADS)) {
            val limited = Plans.limit(settings, tier)
            assertFalse(limited.scheduleOn)
            assertEquals(nights, limited.schedule)
        }
        assertEquals(settings, Plans.limit(settings, Tier.PRO))
    }

    @Test
    fun `without pro a timer at silence or block does not count, and a pause still does`() {
        val timed = ScreeningSettings(mode = Mode.OFF, timerMode = Mode.BLOCK, timerUntilMillis = 5_000)
        assertEquals(ScreeningSettings(mode = Mode.OFF), Plans.limit(timed, Tier.FREE))
        assertEquals(timed, Plans.limit(timed, Tier.PRO))
        val paused = ScreeningSettings(mode = Mode.BLOCK, timerMode = Mode.OFF, timerUntilMillis = 5_000)
        for (tier in Tier.entries) assertEquals(paused, Plans.limit(paused, tier))
    }

    @Test
    fun `without pro there are no number rules`() {
        val rules = listOf(NumberRule("+92", Action.BLOCK), NumberRule("+914428", Action.ALLOW))
        val settings = ScreeningSettings(mode = Mode.SILENCE, numberRules = rules)
        for (tier in listOf(Tier.FREE, Tier.NO_ADS)) {
            assertEquals(ScreeningSettings(mode = Mode.SILENCE), Plans.limit(settings, tier))
        }
        for (tier in listOf(Tier.PRO, Tier.PLUS)) assertEquals(rules, Plans.limit(settings, tier).numberRules)
    }

    @Test
    fun `everything else in the settings is every tier's`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, repeatCallsRing = true, allowListEnabled = true, promotionalSeriesBlocked = true)
        for (tier in Tier.entries) assertEquals(settings, Plans.limit(settings, tier))
    }
}
