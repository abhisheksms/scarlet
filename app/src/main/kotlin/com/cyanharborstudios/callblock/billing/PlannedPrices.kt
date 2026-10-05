package com.cyanharborstudios.callblock.billing

import com.cyanharborstudios.callblock.core.plans.Plans

/**
 * What the plans are meant to cost, in rupees, for a test build to show where Google Play
 * has nothing on sale. A build from Google Play never shows these: the price there is
 * Google Play's own, in the buyer's own currency. A price really lives in Play Console;
 * these are the numbers to enter there (PLAN.md, gate G12), kept in step by a test.
 */
object PlannedPrices {

    val rupees = mapOf(
        Plans.NO_ADS_PRODUCT to 99,
        Plans.PRO_PRODUCT to 199,
        // Pro for someone who has No Ads: the difference between the two.
        Plans.PRO_UPGRADE_PRODUCT to 100,
    )

    fun of(product: String): String? = rupees[product]?.let { "\u20B9$it" }
}
