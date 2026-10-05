package com.cyanharborstudios.callblock.billing

import com.cyanharborstudios.callblock.core.plans.Plans
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** What a plan's plate says about its price, and the rule that a build from Google Play shows only Google Play's. */
class PriceLineTest {

    private val everyStatus = StoreStatus.entries

    @Test
    fun `Google Play's own price is shown whenever it has given one, in any build`() {
        val store = StoreState(status = StoreStatus.OPEN, prices = mapOf(Plans.PRO_PRODUCT to "Rs 249.00"))
        for (testBuild in listOf(true, false)) {
            assertEquals(PriceLine.FromGooglePlay("Rs 249.00"), priceLineFor(Plans.PRO_PRODUCT, store, testBuild))
        }
    }

    @Test
    fun `a build from Google Play never shows a planned price`() {
        for (status in everyStatus) {
            for (product in Plans.PRODUCTS) {
                val line = priceLineFor(product, StoreState(status = status), testBuild = false)
                assertFalse("$product while $status: $line", line is PriceLine.Planned)
            }
        }
    }

    @Test
    fun `without a price a build from Google Play says why there is none`() {
        assertEquals(PriceLine.Asking, priceLineFor(Plans.PRO_PRODUCT, StoreState(status = StoreStatus.CHECKING), testBuild = false))
        assertEquals(PriceLine.Unreachable, priceLineFor(Plans.PRO_PRODUCT, StoreState(status = StoreStatus.UNREACHABLE), testBuild = false))
        assertEquals(PriceLine.NotOnSale, priceLineFor(Plans.PRO_PRODUCT, StoreState(status = StoreStatus.OPEN), testBuild = false))
        // One product on sale does not put another on sale.
        val onlyNoAds = StoreState(status = StoreStatus.OPEN, prices = mapOf(Plans.NO_ADS_PRODUCT to "Rs 99.00"))
        assertEquals(PriceLine.NotOnSale, priceLineFor(Plans.PRO_PRODUCT, onlyNoAds, testBuild = false))
    }

    @Test
    fun `a test build shows the planned price where Google Play has none, whatever Google Play said`() {
        for (status in everyStatus) {
            for (product in Plans.PRODUCTS) {
                val line = priceLineFor(product, StoreState(status = status), testBuild = true)
                assertEquals("$product while $status", PriceLine.Planned(PlannedPrices.of(product)!!), line)
            }
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
    }
}
