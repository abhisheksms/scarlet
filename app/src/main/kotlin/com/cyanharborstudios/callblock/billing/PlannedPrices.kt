package com.cyanharborstudios.callblock.billing

import com.cyanharborstudios.callblock.core.plans.Plans

/**
 * What the plans are meant to cost, in rupees, for a test build to show where Google Play
 * has nothing on sale. A build from Google Play never shows these: the price there is
 * Google Play's own, in the buyer's own currency. A price really lives in Play Console;
 * these are the numbers to enter there (PLAN.md, gates G12 and G14), kept in step by a test.
 */
object PlannedPrices {

    val rupees = mapOf(
        Plans.NO_ADS_PRODUCT to 99,
        Plans.PRO_PRODUCT to 199,
        // Pro for someone who has No Ads: the difference between the two.
        Plans.PRO_UPGRADE_PRODUCT to 100,
    )

    /** Plus: a month or a year, each with the first two months free (docs/launch/LAUNCH_PLAN.md, "Pricing"). */
    const val PLUS_MONTHLY_RUPEES = 49
    const val PLUS_YEARLY_RUPEES = 299
    const val PLUS_FREE_PERIOD = "P2M"

    val plus = SubscriptionTerms(
        monthly = SubscriptionTerm(price = rupees(PLUS_MONTHLY_RUPEES), period = "P1M", free = PLUS_FREE_PERIOD, offerToken = null),
        yearly = SubscriptionTerm(price = rupees(PLUS_YEARLY_RUPEES), period = "P1Y", free = PLUS_FREE_PERIOD, offerToken = null),
    )

    fun of(product: String): String? = rupees[product]?.let { rupees(it) }

    fun subscription(product: String): SubscriptionTerms? = if (product == Plans.PLUS_PRODUCT) plus else null

    private fun rupees(amount: Int) = "₹$amount"
}
