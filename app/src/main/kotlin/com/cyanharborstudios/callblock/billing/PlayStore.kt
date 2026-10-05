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
    /** Google Play's own price text for each product it has on sale, by product id. */
    val prices: Map<String, String> = emptyMap(),
    /** A purchase is waiting for its payment. Nothing is granted until it has been paid. */
    val paymentPending: Boolean = false,
    /** What the last press of Restore Purchases found. */
    val restore: RestoreResult? = null,
)

/**
 * The plans as Google Play sells them: three products, each bought once (core's Plans).
 *
 * Google Play is the only record of what the user owns. Each time the app comes to the
 * front it is asked, and the answer, never a purchase screen's own result, sets the tier
 * the settings keep. When it cannot be asked, the tier stays as it was last told: a
 * failure neither grants a plan nor takes one away. A purchase is acknowledged once it has
 * been paid for, as Google Play requires within three days. Nothing of a purchase is kept
 * or logged here: no token, no order id.
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

    /** Opens Google Play's purchase screen for [productId]. False when it is not on sale or the screen would not open. */
    fun buy(activity: Activity, productId: String): Boolean {
        val details = onSale[productId] ?: return false
        val product = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details)
        offerOf(details)?.offerToken?.let { if (it.isNotEmpty()) product.setOfferToken(it) }
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
        val asked = client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build())
        if (asked.billingResult.responseCode != BillingResponseCode.OK) return null
        val purchases = asked.purchasesList
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

    private suspend fun readPrices() {
        val products = Plans.PRODUCTS.map {
            QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(ProductType.INAPP).build()
        }
        val asked = client.queryProductDetails(QueryProductDetailsParams.newBuilder().setProductList(products).build())
        if (asked.billingResult.responseCode != BillingResponseCode.OK) {
            stateFlow.update { it.copy(status = StoreStatus.UNREACHABLE) }
            return
        }
        // A product Google Play does not have (the app is not in Play Console yet, or this build is not from Play) is simply absent.
        onSale = asked.productDetailsList.orEmpty().associateBy { it.productId }
        val prices = onSale.mapNotNull { (id, details) -> offerOf(details)?.formattedPrice?.let { id to it } }.toMap()
        stateFlow.update { it.copy(status = StoreStatus.OPEN, prices = prices) }
    }

    /** The offer a purchase of [details] is made at: the first Google Play lists, which for a plain product is its only one. */
    private fun offerOf(details: ProductDetails): ProductDetails.OneTimePurchaseOfferDetails? =
        details.oneTimePurchaseOfferDetailsList?.firstOrNull() ?: details.oneTimePurchaseOfferDetails
}
