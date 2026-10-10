package com.cyanharborstudios.callblock.billing

import com.cyanharborstudios.callblock.core.plans.Plans
import com.cyanharborstudios.callblock.core.plans.Tier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Which plans the plans screen lists for a user, in what order, and what each plate shows of a price. */
class PlanOffersTest {

    private val plusTerms = SubscriptionTerms(
        monthly = SubscriptionTerm(price = "Rs 49.00", period = "P1M", free = "P2M", offerToken = "token-month"),
        yearly = SubscriptionTerm(price = "Rs 299.00", period = "P1Y", free = "P2M", offerToken = "token-year"),
    )

    private val onSale = StoreState(
        status = StoreStatus.OPEN,
        prices = mapOf(Plans.NO_ADS_PRODUCT to "Rs 99.00", Plans.PRO_PRODUCT to "Rs 199.00", Plans.PRO_UPGRADE_PRODUCT to "Rs 100.00"),
        subscriptions = mapOf(Plans.PLUS_PRODUCT to plusTerms),
    )

    @Test
    fun `on Free the dearest plan is first and the user's own is last`() {
        val offers = planOffers(Tier.FREE, onSale, testBuild = false)
        assertEquals(listOf(Tier.PLUS, Tier.PRO, Tier.NO_ADS, Tier.FREE), offers.map { it.plan })
        assertEquals(listOf(Plans.PLUS_PRODUCT, Plans.PRO_PRODUCT, Plans.NO_ADS_PRODUCT, null), offers.map { it.product })
        assertEquals(listOf("Rs 49.00", "Rs 199.00", "Rs 99.00", null), offers.map { it.priceText })
        assertEquals(listOf(false, false, false, true), offers.map { it.yours })
        assertEquals(plusTerms, offers.first().subscription)
        assertEquals(listOf(null, null, null), offers.drop(1).map { it.subscription })
    }

    @Test
    fun `on No Ads the plan below is gone and Pro is the upgrade at its own price`() {
        val offers = planOffers(Tier.NO_ADS, onSale, testBuild = false)
        assertEquals(listOf(Tier.PLUS, Tier.PRO, Tier.NO_ADS), offers.map { it.plan })
        assertEquals(listOf(Plans.PLUS_PRODUCT, Plans.PRO_UPGRADE_PRODUCT, null), offers.map { it.product })
        assertEquals(listOf("Rs 49.00", "Rs 100.00", null), offers.map { it.priceText })
        assertEquals(listOf(false, false, true), offers.map { it.yours })
    }

    @Test
    fun `on Pro the subscription is still on offer, at the same terms as for everyone`() {
        val offers = planOffers(Tier.PRO, onSale, testBuild = false)
        assertEquals(listOf(Tier.PLUS, Tier.PRO), offers.map { it.plan })
        assertEquals(listOf(Plans.PLUS_PRODUCT, null), offers.map { it.product })
        assertEquals(plusTerms, offers.first().subscription)
        assertEquals(PlanOffer(Tier.PRO, yours = true, product = null, price = null), offers.last())
    }

    @Test
    fun `on Plus there is one plate, the user's own, with nothing to buy`() {
        for (testBuild in listOf(true, false)) {
            val offers = planOffers(Tier.PLUS, onSale, testBuild)
            assertEquals(listOf(PlanOffer(Tier.PLUS, yours = true, product = null, price = null)), offers)
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
                assertEquals(null, own.subscription)
            }
        }
    }

    @Test
    fun `a test build shows the planned prices where Google Play has none, and a build from Google Play shows none`() {
        val nothingOnSale = StoreState(status = StoreStatus.OPEN)
        val test = planOffers(Tier.FREE, nothingOnSale, testBuild = true)
        assertEquals(
            listOf(PlannedPrices.plus.monthly?.price, PlannedPrices.of(Plans.PRO_PRODUCT), PlannedPrices.of(Plans.NO_ADS_PRODUCT), null),
            test.map { it.priceText },
        )
        assertTrue(test.filter { !it.yours }.all { it.planned })
        assertEquals(PlannedPrices.plus, test.first().subscription)

        val fromPlay = planOffers(Tier.FREE, nothingOnSale, testBuild = false)
        assertEquals(listOf(null, null, null, null), fromPlay.map { it.priceText })
        assertEquals(listOf(PriceLine.NotOnSale, PriceLine.NotOnSale, PriceLine.NotOnSale, null), fromPlay.map { it.price })
        assertTrue(fromPlay.none { it.planned })
    }

    @Test
    fun `a subscription's figure is its monthly price, or the yearly one when that is all there is`() {
        val monthly = PlanOffer(Tier.PLUS, yours = false, product = Plans.PLUS_PRODUCT, price = PriceLine.Subscription(plusTerms, planned = false))
        assertEquals("Rs 49.00", monthly.priceText)
        val yearlyOnly = monthly.copy(price = PriceLine.Subscription(plusTerms.copy(monthly = null), planned = false))
        assertEquals("Rs 299.00", yearlyOnly.priceText)
        assertEquals(plusTerms.yearly, yearlyOnly.subscription?.leading)
    }
}
