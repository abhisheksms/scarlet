package com.cyanharborstudios.callblock.billing

import com.cyanharborstudios.callblock.core.plans.Plans
import com.cyanharborstudios.callblock.core.plans.Tier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Which plans the plans screen lists for a user, in what order, and what each plate shows of a price. */
class PlanOffersTest {

    private val onSale = StoreState(
        status = StoreStatus.OPEN,
        prices = mapOf(Plans.NO_ADS_PRODUCT to "Rs 99.00", Plans.PRO_PRODUCT to "Rs 199.00", Plans.PRO_UPGRADE_PRODUCT to "Rs 100.00"),
    )

    @Test
    fun `on Free the dearest plan is first and the user's own is last`() {
        val offers = planOffers(Tier.FREE, onSale, testBuild = false)
        assertEquals(listOf(Tier.PRO, Tier.NO_ADS, Tier.FREE), offers.map { it.plan })
        assertEquals(listOf(Plans.PRO_PRODUCT, Plans.NO_ADS_PRODUCT, null), offers.map { it.product })
        assertEquals(listOf("Rs 199.00", "Rs 99.00", null), offers.map { it.priceText })
        assertEquals(listOf(false, false, true), offers.map { it.yours })
    }

    @Test
    fun `on No Ads the plan below is gone and Pro is the upgrade at its own price`() {
        val offers = planOffers(Tier.NO_ADS, onSale, testBuild = false)
        assertEquals(listOf(Tier.PRO, Tier.NO_ADS), offers.map { it.plan })
        assertEquals(listOf(Plans.PRO_UPGRADE_PRODUCT, null), offers.map { it.product })
        assertEquals(listOf("Rs 100.00", null), offers.map { it.priceText })
        assertEquals(listOf(false, true), offers.map { it.yours })
    }

    @Test
    fun `on Pro there is one plate, the user's own, with nothing to buy`() {
        for (testBuild in listOf(true, false)) {
            val offers = planOffers(Tier.PRO, onSale, testBuild)
            assertEquals(listOf(PlanOffer(Tier.PRO, yours = true, product = null, price = null)), offers)
            assertEquals(null, offers.single().priceText)
        }
    }

    @Test
    fun `the user's own plan never carries a price, on any plan and in any build`() {
        for (tier in Tier.entries) {
            for (testBuild in listOf(true, false)) {
                val own = planOffers(tier, onSale, testBuild).single { it.yours }
                assertEquals(tier, own.plan)
                assertEquals(null, own.product)
                assertEquals(null, own.priceText)
            }
        }
    }

    @Test
    fun `a test build shows the planned price where Google Play has none, and a build from Google Play shows none`() {
        val nothingOnSale = StoreState(status = StoreStatus.OPEN)
        val test = planOffers(Tier.FREE, nothingOnSale, testBuild = true)
        assertEquals(listOf(PlannedPrices.of(Plans.PRO_PRODUCT), PlannedPrices.of(Plans.NO_ADS_PRODUCT), null), test.map { it.priceText })
        assertTrue(test.filter { !it.yours }.all { it.price is PriceLine.Planned })

        val fromPlay = planOffers(Tier.FREE, nothingOnSale, testBuild = false)
        assertEquals(listOf(null, null, null), fromPlay.map { it.priceText })
        assertEquals(listOf(PriceLine.NotOnSale, PriceLine.NotOnSale, null), fromPlay.map { it.price })
    }
}
