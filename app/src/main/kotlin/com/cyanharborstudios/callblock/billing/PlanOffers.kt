package com.cyanharborstudios.callblock.billing

import com.cyanharborstudios.callblock.core.plans.Plans
import com.cyanharborstudios.callblock.core.plans.Tier

/** One plan as the plans screen lays it out. */
data class PlanOffer(
    val plan: Tier,
    /** The user's own plan: no price, nothing to buy. */
    val yours: Boolean,
    /** The product that takes the user to this plan; null for their own. */
    val product: String?,
    /** What the plate says about the price; null for their own plan. */
    val price: PriceLine?,
) {
    /** The price to show, whether Google Play's or a test build's planned one; null when there is none to show. */
    val priceText: String?
        get() = when (price) {
            is PriceLine.FromGooglePlay -> price.price
            is PriceLine.Planned -> price.price
            else -> null
        }
}

/**
 * The plans a user on [tier] is shown, as a price list: the dearest first, their own last.
 * The plan that holds the most is the one read first, and beside its price the cheaper one
 * reads as the small step it is. A plan below the user's own is left out: there is nothing
 * to do with it.
 */
fun planOffers(tier: Tier, store: StoreState, testBuild: Boolean): List<PlanOffer> =
    Tier.entries.filter { it >= tier }.sortedDescending().map { plan ->
        val product = Plans.productFor(tier, plan)
        PlanOffer(plan, yours = plan == tier, product = product, price = product?.let { priceLineFor(it, store, testBuild) })
    }
