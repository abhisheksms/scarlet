package com.cyanharborstudios.callblock.ui

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
import com.cyanharborstudios.callblock.core.plans.Tier
import com.cyanharborstudios.callblock.ui.parts.CapsText
import com.cyanharborstudios.callblock.ui.parts.Header
import com.cyanharborstudios.callblock.ui.parts.KeyChoice
import com.cyanharborstudios.callblock.ui.parts.LatchingKeys
import com.cyanharborstudios.callblock.ui.parts.Plate
import com.cyanharborstudios.callblock.ui.parts.Section
import com.cyanharborstudios.callblock.ui.parts.Sentence
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType

/**
 * The three plans, each holding everything the one before it holds, and which one is the
 * user's. Each paid plan is bought once. A test build can try each plan without buying.
 */
@Composable
fun PlansScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
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
                PlanPlate(
                    name = names.getValue(plan),
                    detail = details.getValue(plan),
                    yours = plan == tier,
                    // A plan above the user's is for sale. Until Google Play can sell it, the plate says so and offers no key.
                    forSale = plan > tier,
                    tag = "plan-${plan.name}",
                )
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

/** One plan on a plate: its name as engraved, what it holds, and whether it is the user's. */
@Composable
private fun PlanPlate(name: String, detail: String, yours: Boolean, forSale: Boolean, tag: String) {
    val colors = MaterialTheme.colorScheme
    val yoursWord = stringResource(R.string.plan_yours)
    Plate(Modifier.testTag(tag).semantics(mergeDescendants = true) { if (yours) stateDescription = yoursWord }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                CapsText(name, if (yours) SwitchboardType.leverSet else SwitchboardType.lever, color = colors.onSurface, maxLines = 1)
                if (yours) CapsText(yoursWord, SwitchboardType.caption, color = colors.onSurface, maxLines = 1)
            }
            Sentence(detail, SwitchboardType.lead, color = colors.onSurfaceVariant)
            if (forSale) Sentence(stringResource(R.string.plan_not_on_sale), SwitchboardType.note, Modifier.padding(top = 4.dp), color = colors.onSurfaceVariant)
        }
    }
}
