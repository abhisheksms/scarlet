package com.cyanharborstudios.callblock.billing

/** What a plan's plate says about its price. */
sealed interface PriceLine {

    /** Google Play's own price text. The only line a key to buy goes with. */
    data class FromGooglePlay(val price: String) : PriceLine

    /** A test build only: what the plan is meant to cost, where Google Play has nothing on sale. */
    data class Planned(val price: String) : PriceLine

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
    val fromGooglePlay = store.prices[product]
    if (fromGooglePlay != null) return PriceLine.FromGooglePlay(fromGooglePlay)
    val planned = if (testBuild) PlannedPrices.of(product) else null
    return when {
        planned != null -> PriceLine.Planned(planned)
        store.status == StoreStatus.CHECKING -> PriceLine.Asking
        store.status == StoreStatus.UNREACHABLE -> PriceLine.Unreachable
        else -> PriceLine.NotOnSale
    }
}
