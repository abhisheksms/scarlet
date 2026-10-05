package com.cyanharborstudios.callblock.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.BuildConfig
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.billing.PriceLine
import com.cyanharborstudios.callblock.billing.RestoreResult
import com.cyanharborstudios.callblock.billing.priceLineFor
import com.cyanharborstudios.callblock.core.plans.Plans
import com.cyanharborstudios.callblock.core.plans.Tier
import com.cyanharborstudios.callblock.ui.parts.CapsText
import com.cyanharborstudios.callblock.ui.parts.Header
import com.cyanharborstudios.callblock.ui.parts.Key
import com.cyanharborstudios.callblock.ui.parts.KeyChoice
import com.cyanharborstudios.callblock.ui.parts.LatchingKeys
import com.cyanharborstudios.callblock.ui.parts.Plate
import com.cyanharborstudios.callblock.ui.parts.Section
import com.cyanharborstudios.callblock.ui.parts.Sentence
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType

/**
 * The three plans, each holding everything the one before it holds, and which one is the
 * user's. Each paid plan is bought once, through Google Play, at the price Google Play
 * gives. A test build can also try each plan without buying.
 */
@Composable
fun PlansScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val store by viewModel.store.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().statusBarsPadding().testTag("plans-screen")) {
        Box(Modifier.padding(horizontal = 16.dp)) { Header(stringResource(R.string.plans), onBack) }
        val tier = settings?.tier ?: return
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Sentence(stringResource(R.string.plans_lead), SwitchboardType.lead, color = colors.onSurface)
            val names = mapOf(
                Tier.FREE to stringResource(R.string.tier_free),
                Tier.NO_ADS to stringResource(R.string.tier_no_ads),
                Tier.PRO to stringResource(R.string.tier_pro),
            )
            val details = mapOf(
                Tier.FREE to stringResource(R.string.plan_free_detail),
                Tier.NO_ADS to stringResource(R.string.plan_no_ads_detail),
                Tier.PRO to stringResource(R.string.plan_pro_detail),
            )
            for (plan in Tier.entries) {
                // A plan above the user's has a product. Its plate carries Google Play's own price
                // and a key, or says why there is neither. A test build shows the planned price
                // where Google Play has none, and says that is what it is.
                val product = Plans.productFor(tier, plan)
                val line = product?.let { priceLineFor(it, store, testBuild = BuildConfig.DEBUG) }
                PlanPlate(
                    name = names.getValue(plan),
                    detail = details.getValue(plan),
                    yours = plan == tier,
                    note = when (line) {
                        null -> null
                        is PriceLine.FromGooglePlay -> priceSentence(product, line.price)
                        is PriceLine.Planned -> priceSentence(product, line.price) + " " + stringResource(R.string.plan_price_is_planned)
                        PriceLine.Asking -> stringResource(R.string.plan_asking_price)
                        PriceLine.Unreachable -> stringResource(R.string.store_unreachable)
                        PriceLine.NotOnSale -> stringResource(R.string.plan_not_on_sale)
                    },
                    buyLabel = if (line is PriceLine.FromGooglePlay) stringResource(R.string.buy_plan, names.getValue(plan)) else null,
                    onBuy = { if (activity != null && product != null) viewModel.buy(activity, product) },
                    tag = "plan-${plan.name}",
                    buyTag = "buy-${plan.name}",
                )
            }
            if (store.paymentPending) {
                Sentence(stringResource(R.string.payment_pending), SwitchboardType.body, color = colors.onSurface)
            }

            // Google Play is asked what is owned each time the app comes to the front. This asks
            // again at the user's own press, and says what it found.
            Section {
                Key(stringResource(R.string.restore_purchases), onClick = viewModel::restorePurchases, tag = "restore-purchases")
                val found = when (store.restore) {
                    RestoreResult.FOUND -> stringResource(R.string.restore_found, names.getValue(tier))
                    RestoreResult.NOTHING -> stringResource(R.string.restore_nothing)
                    RestoreResult.FAILED -> stringResource(R.string.store_unreachable)
                    null -> null
                }
                if (found != null) {
                    Sentence(found, SwitchboardType.body, Modifier.testTag("restore-result"), color = colors.onSurfaceVariant)
                }
            }

            if (BuildConfig.DEBUG) {
                Section {
                    val heading = stringResource(R.string.debug_tier_heading)
                    CapsText(heading, SwitchboardType.strip, color = colors.onSurface)
                    Sentence(stringResource(R.string.debug_tier_detail), SwitchboardType.body, color = colors.onSurfaceVariant)
                    LatchingKeys(
                        choices = Tier.entries.map { KeyChoice(it, names.getValue(it)) },
                        selected = tier,
                        onSelect = viewModel::setDebugTier,
                        modifier = Modifier.semantics { contentDescription = heading },
                        tag = { "debug-tier-${it.name}" },
                    )
                }
            }
        }
    }
}

/** "₹99, once.", or for the upgrade the same with why it is less. */
@Composable
private fun priceSentence(product: String, price: String): String =
    stringResource(if (product == Plans.PRO_UPGRADE_PRODUCT) R.string.plan_price_upgrade else R.string.plan_price_once, price)

/**
 * One plan on a plate: its name as engraved, what it holds, whether it is the user's, and
 * for a plan that can be bought its price and the key that opens Google Play's purchase screen.
 */
@Composable
private fun PlanPlate(
    name: String,
    detail: String,
    yours: Boolean,
    note: String?,
    buyLabel: String?,
    onBuy: () -> Unit,
    tag: String,
    buyTag: String,
) {
    val colors = MaterialTheme.colorScheme
    val yoursWord = stringResource(R.string.plan_yours)
    Plate(Modifier.testTag(tag).semantics(mergeDescendants = true) { if (yours) stateDescription = yoursWord }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                CapsText(name, if (yours) SwitchboardType.leverSet else SwitchboardType.lever, color = colors.onSurface, maxLines = 1)
                if (yours) CapsText(yoursWord, SwitchboardType.caption, color = colors.onSurface, maxLines = 1)
            }
            Sentence(detail, SwitchboardType.lead, color = colors.onSurfaceVariant)
            if (note != null) Sentence(note, SwitchboardType.note, Modifier.padding(top = 4.dp), color = colors.onSurfaceVariant)
            if (buyLabel != null) Key(buyLabel, onClick = onBuy, modifier = Modifier.padding(top = 8.dp), onPlate = true, tag = buyTag)
        }
    }
}
