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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.BuildConfig
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.billing.PlanOffer
import com.cyanharborstudios.callblock.billing.PriceLine
import com.cyanharborstudios.callblock.billing.RestoreResult
import com.cyanharborstudios.callblock.billing.SubscriptionTerm
import com.cyanharborstudios.callblock.billing.planOffers
import com.cyanharborstudios.callblock.core.plans.Plans
import com.cyanharborstudios.callblock.core.plans.Tier
import com.cyanharborstudios.callblock.ui.parts.CapsText
import com.cyanharborstudios.callblock.ui.parts.Header
import com.cyanharborstudios.callblock.ui.parts.Key
import com.cyanharborstudios.callblock.ui.parts.KeyChoice
import com.cyanharborstudios.callblock.ui.parts.LatchingKeys
import com.cyanharborstudios.callblock.ui.parts.MainKey
import com.cyanharborstudios.callblock.ui.parts.Plate
import com.cyanharborstudios.callblock.ui.parts.Section
import com.cyanharborstudios.callblock.ui.parts.Sentence
import com.cyanharborstudios.callblock.ui.parts.Strip
import com.cyanharborstudios.callblock.ui.parts.Strips
import com.cyanharborstudios.callblock.ui.parts.SwitchboardIcons
import com.cyanharborstudios.callblock.ui.parts.Trail
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType

/**
 * The plans as a price list, the dearest first: what each holds, its price as Google Play
 * gives it, and a key that opens Google Play's purchase screen. Plus is a subscription, a
 * month or a year at a time, with the free stretch Google Play grants the buyer said before
 * its keys; the other paid plans are bought once. The user's own plan comes last, and a
 * plan below it is not shown.
 *
 * A test build shows the planned prices where Google Play has nothing on sale, and says so
 * once, at the top; its Buy keys then switch the plan without a payment, so the page can be
 * seen and tried as a buyer will have it.
 */
@Composable
fun PlansScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val store by viewModel.store.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val context = LocalContext.current
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
            val names = mapOf(
                Tier.FREE to stringResource(R.string.tier_free),
                Tier.NO_ADS to stringResource(R.string.tier_no_ads),
                Tier.PRO to stringResource(R.string.tier_pro),
                Tier.PLUS to stringResource(R.string.tier_plus),
            )
            val offers = planOffers(tier, store, testBuild = BuildConfig.DEBUG)
            if (offers.any { it.planned }) {
                Sentence(stringResource(R.string.plans_test_build), SwitchboardType.note, Modifier.testTag("plans-test-build"), color = colors.onSurfaceVariant)
            }
            for (offer in offers) {
                PlanPlate(
                    offer = offer,
                    name = names.getValue(offer.plan),
                    // Under the price: how it is paid, or for the upgrade why it is less.
                    term = when {
                        offer.product == null -> null
                        offer.subscription != null -> offer.subscription?.leading?.let { everyWords(it.period) }
                        offer.product == Plans.PRO_UPGRADE_PRODUCT -> stringResource(R.string.plan_upgrade_from, names.getValue(Tier.NO_ADS))
                        else -> stringResource(R.string.plan_pay_once)
                    },
                    onBuy = { chosen ->
                        when {
                            // A test build only: there is nothing to buy, so the key gives the plan as its own keys below do.
                            offer.planned -> viewModel.setDebugTier(offer.plan)
                            offer.price is PriceLine.FromGooglePlay || offer.price is PriceLine.Subscription ->
                                if (activity != null && offer.product != null) viewModel.buy(activity, offer.product, chosen?.offerToken)
                            else -> Unit
                        }
                    },
                )
            }
            if (store.paymentPending) {
                Sentence(stringResource(R.string.payment_pending), SwitchboardType.body, color = colors.onSurface)
            }

            // A subscription is cancelled or changed on Google Play's own page for it, never here.
            if (tier == Tier.PLUS) {
                Strips {
                    Strip(
                        title = stringResource(R.string.manage_subscription),
                        icon = SwitchboardIcons.tag,
                        trail = Trail.Out,
                        onClick = { Links.openSubscriptions(context) },
                        rule = false,
                        tag = "manage-subscription",
                    )
                }
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

/**
 * One plan on a plate. Its name is at one end and its price at the other, as on a price
 * list, with how it is paid under the price. Then what it holds, and for a plan that can be
 * bought the key that opens Google Play's purchase screen. A subscription has one key for
 * each way of paying, with the free stretch said over them and what follows under them;
 * the dearest plan's keys are the screen's main ones.
 */
@Composable
private fun PlanPlate(offer: PlanOffer, name: String, term: String?, onBuy: (SubscriptionTerm?) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val plan = offer.plan
    val price = offer.priceText
    val yoursWord = stringResource(R.string.plan_yours)
    Plate(Modifier.testTag("plan-${plan.name}").semantics(mergeDescendants = true) { if (offer.yours) stateDescription = yoursWord }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Ends(Modifier.fillMaxWidth()) {
                CapsText(name, if (offer.yours) SwitchboardType.leverSet else SwitchboardType.lever, color = colors.onSurface, maxLines = 1)
                when {
                    offer.yours -> CapsText(yoursWord, SwitchboardType.caption, color = colors.onSurface, maxLines = 1)
                    price != null -> Text(price, style = SwitchboardType.price, color = colors.onSurface, maxLines = 1)
                }
            }
            if (price != null && term != null) {
                CapsText(term, SwitchboardType.caption, Modifier.fillMaxWidth(), color = colors.onSurfaceVariant, textAlign = TextAlign.End)
            }
            // No price to show: the plate says why.
            val why = when (offer.price) {
                PriceLine.Asking -> stringResource(R.string.plan_asking_price)
                PriceLine.Unreachable -> stringResource(R.string.store_unreachable)
                PriceLine.NotOnSale -> stringResource(R.string.plan_not_on_sale)
                else -> null
            }
            if (why != null) Sentence(why, SwitchboardType.note, Modifier.padding(top = 4.dp), color = colors.onSurfaceVariant)

            PlanHolds(plan)

            val terms = offer.subscription
            when {
                terms != null -> SubscriptionKeys(plan, terms, onBuy)
                price != null -> Key(stringResource(R.string.buy_plan, name), onClick = { onBuy(null) }, modifier = Modifier.padding(top = 12.dp), onPlate = true, tag = "buy-${plan.name}")
            }
        }
    }
}

/**
 * The keys that start a subscription: one for each way of paying, named by its price, with
 * the free stretch over them and what follows under them, as Google Play's policy asks to
 * be said before a purchase.
 */
@Composable
private fun SubscriptionKeys(plan: Tier, terms: com.cyanharborstudios.callblock.billing.SubscriptionTerms, onBuy: (SubscriptionTerm?) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val free = terms.leading?.free
    Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CapsText(
            if (free != null) stringResource(R.string.plan_free_then, periodWords(free)) else stringResource(R.string.plan_billed),
            SwitchboardType.caption,
            color = colors.onSurfaceVariant,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            terms.monthly?.let { MainKey(priceWords(it), onClick = { onBuy(it) }, modifier = Modifier.weight(1f), tag = "buy-${plan.name}-monthly") }
            terms.yearly?.let { MainKey(priceWords(it), onClick = { onBuy(it) }, modifier = Modifier.weight(1f), tag = "buy-${plan.name}-yearly") }
        }
        Sentence(stringResource(R.string.plan_renews), SwitchboardType.note, color = colors.onSurfaceVariant)
    }
}

/** "₹49 a month", "₹299 a year", or "₹99 every 3 months": a key's name, the price first. */
@Composable
private fun priceWords(term: SubscriptionTerm): String = when (term.period) {
    "P1M" -> stringResource(R.string.plan_monthly_price, term.price)
    "P1Y" -> stringResource(R.string.plan_yearly_price, term.price)
    else -> stringResource(R.string.plan_every_price, term.price, periodWords(term.period))
}

/** "A month", "A year", or "Every 3 months": the caption under a subscription's price. */
@Composable
private fun everyWords(period: String): String = when (period) {
    "P1M" -> stringResource(R.string.plan_a_month)
    "P1Y" -> stringResource(R.string.plan_a_year)
    else -> stringResource(R.string.plan_every, periodWords(period))
}

/** An ISO 8601 period as Google Play writes it, in words: "P2M" is "2 months", "P14D" is "14 days". */
@Composable
fun periodWords(period: String): String {
    val match = Regex("P(\\d+)([DWMY])").matchEntire(period) ?: return period
    val count = match.groupValues[1].toInt()
    return when (match.groupValues[2]) {
        "D" -> pluralStringResource(R.plurals.period_days, count, count)
        "W" -> pluralStringResource(R.plurals.period_weeks, count, count)
        "M" -> pluralStringResource(R.plurals.period_months, count, count)
        else -> pluralStringResource(R.plurals.period_years, count, count)
    }
}

/**
 * Two parts at the two ends of one line. When they cannot share it, as a price in another
 * currency or at large text may not, the second goes under the first, still at the end.
 */
@Composable
private fun Ends(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content, modifier) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val first = placeables.first()
        val second = placeables.getOrNull(1)
        val width = constraints.maxWidth
        val oneLine = second == null || first.width + 12.dp.roundToPx() + second.width <= width
        val height = if (oneLine) maxOf(first.height, second?.height ?: 0) else first.height + (second?.height ?: 0)
        layout(width, height) {
            first.placeRelative(0, if (oneLine) (height - first.height) / 2 else 0)
            second?.placeRelative(width - second.width, if (oneLine) (height - second.height) / 2 else first.height)
        }
    }
}

/**
 * What a plan holds, as the rows that open those things elsewhere in the app: the same
 * icon and the same words, so a row met on Home is known again here. No Ads holds what its
 * name says, and nothing is listed under it.
 */
@Composable
private fun PlanHolds(plan: Tier) {
    when (plan) {
        Tier.PLUS -> Strips(Modifier.padding(top = 10.dp)) {
            Strip(stringResource(R.string.frequent_callers), icon = SwitchboardIcons.frequent, detail = stringResource(R.string.frequent_detail_locked), low = true)
            Strip(stringResource(R.string.timer), icon = SwitchboardIcons.timer, detail = stringResource(R.string.timer_detail_locked), low = true)
            Strip(stringResource(R.string.schedule), icon = SwitchboardIcons.calendar, detail = stringResource(R.string.schedule_detail_locked), low = true)
            Strip(stringResource(R.string.number_rules), icon = SwitchboardIcons.funnel, detail = stringResource(R.string.number_rules_detail_locked), low = true)
            Strip(stringResource(R.string.tier_no_ads), icon = SwitchboardIcons.megaphoneOff, rule = false, low = true)
        }
        Tier.PRO -> Strips(Modifier.padding(top = 10.dp)) {
            Strip(stringResource(R.string.timer), icon = SwitchboardIcons.timer, detail = stringResource(R.string.timer_detail_locked), low = true)
            Strip(stringResource(R.string.schedule), icon = SwitchboardIcons.calendar, detail = stringResource(R.string.schedule_detail_locked), low = true)
            Strip(stringResource(R.string.number_rules), icon = SwitchboardIcons.funnel, detail = stringResource(R.string.number_rules_detail_locked), low = true)
            Strip(stringResource(R.string.tier_no_ads), icon = SwitchboardIcons.megaphoneOff, rule = false, low = true)
        }
        Tier.NO_ADS -> Unit
        Tier.FREE -> Strips(Modifier.padding(top = 10.dp)) {
            Strip(stringResource(R.string.plan_with_ads), icon = SwitchboardIcons.megaphone, rule = false, low = true)
        }
    }
}
