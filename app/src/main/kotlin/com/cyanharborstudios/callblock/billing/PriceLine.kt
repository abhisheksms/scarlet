package com.cyanharborstudios.callblock.billing

import com.cyanharborstudios.callblock.core.plans.Plans

/**
 * One way of paying for a subscription, as Google Play lists it: the recurring [price] with
 * its [period] ("P1M", "P1Y"), the free stretch that comes first for this buyer, if any
 * ("P2M"; null when there is none, or none left for them), and the [offerToken] a
 * purchase is started with. The token is null for a test build's planned terms.
 */
data class SubscriptionTerm(val price: String, val period: String, val free: String?, val offerToken: String?)

/** A subscription's two ways of paying. Either may be missing when Play Console has only the other. */
data class SubscriptionTerms(val monthly: SubscriptionTerm?, val yearly: SubscriptionTerm?) {
    /** The way of paying shown as the plan's price: the monthly one, or the yearly one when that is all there is. */
    val leading: SubscriptionTerm? get() = monthly ?: yearly
}

/** What a plan's plate says about its price. */
sealed interface PriceLine {

    /** Google Play's own price text. The only line a key that really buys goes with. */
    data class FromGooglePlay(val price: String) : PriceLine

    /** A test build only: what the plan is meant to cost, where Google Play has nothing on sale. Its key takes no payment. */
    data class Planned(val price: String) : PriceLine

    /**
     * A subscription: Google Play's own terms, or, in a test build only, the planned ones
     * ([planned] is then true, and its keys take no payment).
     */
    data class Subscription(val terms: SubscriptionTerms, val planned: Boolean) : PriceLine

    /** Google Play has not answered yet. */
    data object Asking : PriceLine

    /** Google Play could not be reached, or cannot sell on this phone. */
    data object Unreachable : PriceLine

    /** Google Play answered, and does not have this product. */
    data object NotOnSale : PriceLine
}

/**
 * The line for [product]. Google Play's price comes first, in any build. Without one, a
 * test build shows the planned price; a build from Google Play shows no price at all,
 * only why there is none.
 */
fun priceLineFor(product: String, store: StoreState, testBuild: Boolean): PriceLine {
    if (product in Plans.SUBSCRIPTIONS) {
        val fromGooglePlay = store.subscriptions[product]
        if (fromGooglePlay != null) return PriceLine.Subscription(fromGooglePlay, planned = false)
        val planned = if (testBuild) PlannedPrices.subscription(product) else null
        if (planned != null) return PriceLine.Subscription(planned, planned = true)
        return whyNone(store)
    }
    val fromGooglePlay = store.prices[product]
    if (fromGooglePlay != null) return PriceLine.FromGooglePlay(fromGooglePlay)
    val planned = if (testBuild) PlannedPrices.of(product) else null
    return if (planned != null) PriceLine.Planned(planned) else whyNone(store)
}

private fun whyNone(store: StoreState): PriceLine = when (store.status) {
    StoreStatus.CHECKING -> PriceLine.Asking
    StoreStatus.UNREACHABLE -> PriceLine.Unreachable
    StoreStatus.OPEN -> PriceLine.NotOnSale
}
