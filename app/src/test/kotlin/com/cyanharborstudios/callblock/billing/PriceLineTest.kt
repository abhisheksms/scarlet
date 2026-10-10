package com.cyanharborstudios.callblock.billing

import com.cyanharborstudios.callblock.core.plans.Plans
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** What a plan's plate says about its price, and the rule that a build from Google Play shows only Google Play's. */
class PriceLineTest {

    private val everyStatus = StoreStatus.entries

    private val plusTerms = SubscriptionTerms(
        monthly = SubscriptionTerm(price = "Rs 59.00", period = "P1M", free = null, offerToken = "token-month"),
        yearly = SubscriptionTerm(price = "Rs 349.00", period = "P1Y", free = "P1M", offerToken = "token-year"),
    )

    @Test
    fun `Google Play's own price is shown whenever it has given one, in any build`() {
        val store = StoreState(status = StoreStatus.OPEN, prices = mapOf(Plans.PRO_PRODUCT to "Rs 249.00"), subscriptions = mapOf(Plans.PLUS_PRODUCT to plusTerms))
        for (testBuild in listOf(true, false)) {
            assertEquals(PriceLine.FromGooglePlay("Rs 249.00"), priceLineFor(Plans.PRO_PRODUCT, store, testBuild))
            assertEquals(PriceLine.Subscription(plusTerms, planned = false), priceLineFor(Plans.PLUS_PRODUCT, store, testBuild))
        }
    }

    @Test
    fun `a build from Google Play never shows a planned price`() {
        for (status in everyStatus) {
            for (product in Plans.PRODUCTS) {
                val line = priceLineFor(product, StoreState(status = status), testBuild = false)
                assertFalse("$product while $status: $line", line is PriceLine.Planned)
            }
            for (product in Plans.SUBSCRIPTIONS) {
                val line = priceLineFor(product, StoreState(status = status), testBuild = false)
                assertFalse("$product while $status: $line", line is PriceLine.Subscription)
            }
        }
    }

    @Test
    fun `without a price a build from Google Play says why there is none`() {
        for (product in listOf(Plans.PRO_PRODUCT, Plans.PLUS_PRODUCT)) {
            assertEquals(PriceLine.Asking, priceLineFor(product, StoreState(status = StoreStatus.CHECKING), testBuild = false))
            assertEquals(PriceLine.Unreachable, priceLineFor(product, StoreState(status = StoreStatus.UNREACHABLE), testBuild = false))
            assertEquals(PriceLine.NotOnSale, priceLineFor(product, StoreState(status = StoreStatus.OPEN), testBuild = false))
        }
        // One product on sale does not put another on sale.
        val onlyNoAds = StoreState(status = StoreStatus.OPEN, prices = mapOf(Plans.NO_ADS_PRODUCT to "Rs 99.00"))
        assertEquals(PriceLine.NotOnSale, priceLineFor(Plans.PRO_PRODUCT, onlyNoAds, testBuild = false))
        assertEquals(PriceLine.NotOnSale, priceLineFor(Plans.PLUS_PRODUCT, onlyNoAds, testBuild = false))
    }

    @Test
    fun `a test build shows the planned price where Google Play has none, whatever Google Play said`() {
        for (status in everyStatus) {
            for (product in Plans.PRODUCTS) {
                val line = priceLineFor(product, StoreState(status = status), testBuild = true)
                assertEquals("$product while $status", PriceLine.Planned(PlannedPrices.of(product)!!), line)
            }
            assertEquals("plus while $status", PriceLine.Subscription(PlannedPrices.plus, planned = true), priceLineFor(Plans.PLUS_PRODUCT, StoreState(status = status), testBuild = true))
        }
    }

    @Test
    fun `every product has a planned price, and the upgrade is the difference between the two plans`() {
        assertEquals(Plans.PRODUCTS.toSet(), PlannedPrices.rupees.keys)
        val noAds = PlannedPrices.rupees.getValue(Plans.NO_ADS_PRODUCT)
        val pro = PlannedPrices.rupees.getValue(Plans.PRO_PRODUCT)
        assertTrue(noAds in 1 until pro)
        assertEquals(pro - noAds, PlannedPrices.rupees.getValue(Plans.PRO_UPGRADE_PRODUCT))
        assertEquals(null, PlannedPrices.of("something_else"))
        assertEquals(null, PlannedPrices.subscription("something_else"))
    }

    @Test
    fun `the planned subscription is a month or a year, each with the same free stretch, and the year costs less than twelve months`() {
        val plus = PlannedPrices.subscription(Plans.PLUS_PRODUCT)!!
        assertEquals("P1M", plus.monthly?.period)
        assertEquals("P1Y", plus.yearly?.period)
        assertEquals(plus.monthly, plus.leading)
        assertEquals(PlannedPrices.PLUS_FREE_PERIOD, plus.monthly?.free)
        assertEquals(PlannedPrices.PLUS_FREE_PERIOD, plus.yearly?.free)
        assertTrue(PlannedPrices.PLUS_MONTHLY_RUPEES in 1 until PlannedPrices.PLUS_YEARLY_RUPEES)
        assertTrue(PlannedPrices.PLUS_YEARLY_RUPEES < 12 * PlannedPrices.PLUS_MONTHLY_RUPEES)
        // A test build's terms carry no offer token: there is nothing to buy with one.
        assertEquals(null, plus.monthly?.offerToken)
        assertEquals(null, plus.yearly?.offerToken)
    }
}
