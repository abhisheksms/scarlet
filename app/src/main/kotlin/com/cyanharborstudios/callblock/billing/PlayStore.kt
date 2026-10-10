package com.cyanharborstudios.callblock.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.cyanharborstudios.callblock.core.plans.Plans
import com.cyanharborstudios.callblock.core.plans.StorePurchase
import com.cyanharborstudios.callblock.core.plans.Tier
import com.cyanharborstudios.callblock.data.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/** Whether Google Play has answered. */
enum class StoreStatus {
    /** Not asked yet, or being asked. */
    CHECKING,

    /** Google Play answered. What it has on sale is in the prices. */
    OPEN,

    /** Google Play could not be reached, or cannot sell on this phone. */
    UNREACHABLE,
}

enum class RestoreResult { FOUND, NOTHING, FAILED }

/** What the plans screen shows of the shop. */
data class StoreState(
    val status: StoreStatus = StoreStatus.CHECKING,
    /** Google Play's own price text for each product bought once that it has on sale, by product id. */
    val prices: Map<String, String> = emptyMap(),
    /** Google Play's own terms for each subscription it has on sale, by product id. */
    val subscriptions: Map<String, SubscriptionTerms> = emptyMap(),
    /** A purchase is waiting for its payment. Nothing is granted until it has been paid. */
    val paymentPending: Boolean = false,
    /** What the last press of Restore Purchases found. */
    val restore: RestoreResult? = null,
)

/**
 * The plans as Google Play sells them: three products bought once, and one subscription
 * (core's Plans).
 *
 * Google Play is the only record of what the user owns. Each time the app comes to the
 * front it is asked, and the answer, never a purchase screen's own result, sets the tier
 * the settings keep. When it cannot be asked, the tier stays as it was last told: a
 * failure neither grants a plan nor takes one away. A purchase is acknowledged once it has
 * been paid for, as Google Play requires within three days; a subscription's first purchase
 * needs that too, its renewals do not. A subscription that has run out is simply no longer
 * in the answer. Nothing of a purchase is kept or logged here: no token, no order id.
 *
 * Only ever touched from the activity. The screening path never starts it.
 */
class PlayStore(
    context: Context,
    private val scope: CoroutineScope,
    private val settingsStore: SettingsStore,
) {
    private val stateFlow = MutableStateFlow(StoreState())
    val state: StateFlow<StoreState> = stateFlow.asStateFlow()

    /** What Google Play has on sale, kept to start a purchase from. Written off the main thread, read on it. */
    @Volatile
    private var onSale: Map<String, ProductDetails> = emptyMap()

    /** One conversation with Google Play at a time. */
    private val asking = Mutex()

    private val client: BillingClient by lazy {
        BillingClient.newBuilder(context.applicationContext)
            // After a purchase screen closes, ask again what is owned: its own list holds only what was just bought.
            .setListener { result, _ -> if (result.responseCode == BillingResponseCode.OK) refresh() }
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build()
    }

    /** Asks Google Play what the user owns and what is on sale. */
    fun refresh() {
        scope.launch { ask(restoring = false) }
    }

    /** The same, at the user's own press, and says what it found. */
    fun restore() {
        scope.launch { ask(restoring = true) }
    }

    /**
     * Opens Google Play's purchase screen for [productId]. A subscription is bought at one
     * of its offers, named by [offerToken] from the terms shown; a product bought once needs
     * none. False when it is not on sale or the screen would not open.
     */
    fun buy(activity: Activity, productId: String, offerToken: String? = null): Boolean {
        val details = onSale[productId] ?: return false
        val product = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details)
        val token = if (details.productType == ProductType.SUBS) offerToken ?: return false else offerOf(details)?.offerToken
        token?.takeIf { it.isNotEmpty() }?.let { product.setOfferToken(it) }
        val flow = BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(product.build())).build()
        return client.launchBillingFlow(activity, flow).responseCode == BillingResponseCode.OK
    }

    private suspend fun ask(restoring: Boolean) = asking.withLock {
        val owned = if (connect()) readOwned() else null
        if (owned == null) {
            stateFlow.update { it.copy(status = StoreStatus.UNREACHABLE, restore = if (restoring) RestoreResult.FAILED else it.restore) }
            return@withLock
        }
        if (restoring) {
            stateFlow.update { it.copy(restore = if (owned == Tier.FREE) RestoreResult.NOTHING else RestoreResult.FOUND) }
        }
        readPrices()
    }

    private suspend fun connect(): Boolean {
        if (client.isReady) return true
        return suspendCancellableCoroutine { waiting ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    // The library reconnects by itself and reports here again; only the first report is waited for.
                    if (waiting.isActive) waiting.resume(result.responseCode == BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() {
                    // Nothing to do: the library reconnects by itself (enableAutoServiceReconnection).
                }
            })
        }
    }

    /** The tier Google Play says the user has, stored for the rest of the app; null when it could not be asked. */
    private suspend fun readOwned(): Tier? {
        // Google Play lists the two kinds apart. A subscription that has run out is not listed at all.
        val bought = owned(ProductType.INAPP) ?: return null
        val running = owned(ProductType.SUBS) ?: return null
        val purchases = bought + running
        val tier = Plans.tierFor(purchases.map { StorePurchase(it.products, paid = it.purchaseState == Purchase.PurchaseState.PURCHASED) })
        settingsStore.setTier(tier)
        stateFlow.update { it.copy(paymentPending = purchases.any { purchase -> purchase.purchaseState == Purchase.PurchaseState.PENDING }) }
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build())
            }
        }
        return tier
    }

    private suspend fun owned(type: String): List<Purchase>? {
        val asked = client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(type).build())
        return if (asked.billingResult.responseCode == BillingResponseCode.OK) asked.purchasesList else null
    }

    private suspend fun readPrices() {
        val products = onSale(Plans.PRODUCTS, ProductType.INAPP)
        val subscriptions = onSale(Plans.SUBSCRIPTIONS, ProductType.SUBS)
        if (products == null || subscriptions == null) {
            stateFlow.update { it.copy(status = StoreStatus.UNREACHABLE) }
            return
        }
        // A product Google Play does not have (the app is not in Play Console yet, or this build is not from Play) is simply absent.
        onSale = (products + subscriptions).associateBy { it.productId }
        val prices = products.mapNotNull { details -> offerOf(details)?.formattedPrice?.let { details.productId to it } }.toMap()
        val terms = subscriptions.mapNotNull { details -> termsOf(details)?.let { details.productId to it } }.toMap()
        stateFlow.update { it.copy(status = StoreStatus.OPEN, prices = prices, subscriptions = terms) }
    }

    /** What Google Play has on sale of [ids], which must all be of one [type]; null when it could not be asked. */
    private suspend fun onSale(ids: List<String>, type: String): List<ProductDetails>? {
        val products = ids.map { QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(type).build() }
        val asked = client.queryProductDetails(QueryProductDetailsParams.newBuilder().setProductList(products).build())
        return if (asked.billingResult.responseCode == BillingResponseCode.OK) asked.productDetailsList.orEmpty() else null
    }

    /**
     * The offer a purchase of [details] is made at: the cheapest Google Play lists for this buyer. A plain product has
     * one. A discount made in Play Console is listed beside it, in no stated order, and the buyer gets the lower price.
     */
    private fun offerOf(details: ProductDetails): ProductDetails.OneTimePurchaseOfferDetails? =
        details.oneTimePurchaseOfferDetailsList?.minByOrNull { it.priceAmountMicros } ?: details.oneTimePurchaseOfferDetails

    /**
     * A subscription's terms, from the offers Google Play lists for this buyer: a monthly way
     * and a yearly way of paying, each at the offer with a free stretch when the buyer may
     * still take one, else at the base plan itself. An offer is its pricing phases in order;
     * the last phase is the price that recurs, and a phase at no charge is the free stretch.
     * Written from Google's reference and not yet tried against a real offer.
     */
    private fun termsOf(details: ProductDetails): SubscriptionTerms? {
        val offers = details.subscriptionOfferDetails.orEmpty()
        fun term(period: String): SubscriptionTerm? {
            val ofPlan = offers.filter { it.pricingPhases.pricingPhaseList.lastOrNull()?.billingPeriod == period }
            val chosen = ofPlan.firstOrNull { offer -> offer.pricingPhases.pricingPhaseList.any { it.priceAmountMicros == 0L } }
                ?: ofPlan.firstOrNull { it.offerId == null }
                ?: ofPlan.firstOrNull()
                ?: return null
            val phases = chosen.pricingPhases.pricingPhaseList
            val recurring = phases.last()
            return SubscriptionTerm(
                price = recurring.formattedPrice,
                period = recurring.billingPeriod,
                free = phases.firstOrNull { it.priceAmountMicros == 0L }?.billingPeriod,
                offerToken = chosen.offerToken,
            )
        }
        val terms = SubscriptionTerms(monthly = term(MONTHLY), yearly = term(YEARLY))
        return if (terms.monthly == null && terms.yearly == null) null else terms
    }

    private companion object {
        const val MONTHLY = "P1M"
        const val YEARLY = "P1Y"
    }
}
