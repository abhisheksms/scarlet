package com.cyanharborstudios.callblock.core.plans

import com.cyanharborstudios.callblock.core.rules.Action
import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.NumberRule
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
    }

    @Test
    fun `only pro has the timer, the schedule and number rules`() {
        for (feature in ProFeature.entries) {
            assertFalse(Plans.has(Tier.FREE, feature))
            assertFalse(Plans.has(Tier.NO_ADS, feature))
            assertTrue(Plans.has(Tier.PRO, feature))
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
        assertEquals(rules, Plans.limit(settings, Tier.PRO).numberRules)
    }

    @Test
    fun `everything else in the settings is every tier's`() {
        val settings = ScreeningSettings(mode = Mode.BLOCK, repeatCallsRing = true, allowListEnabled = true, promotionalSeriesBlocked = true)
        for (tier in Tier.entries) assertEquals(settings, Plans.limit(settings, tier))
    }
}
