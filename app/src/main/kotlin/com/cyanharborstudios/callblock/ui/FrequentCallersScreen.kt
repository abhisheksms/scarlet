package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.frequent.CallerClass
import com.cyanharborstudios.callblock.core.frequent.FrequentCallers
import com.cyanharborstudios.callblock.core.numbers.PhoneNumbers
import com.cyanharborstudios.callblock.core.plans.Plans
import com.cyanharborstudios.callblock.core.plans.ProFeature
import com.cyanharborstudios.callblock.core.rules.ScreeningSettings
import com.cyanharborstudios.callblock.data.SeenCallEntity
import com.cyanharborstudios.callblock.ui.parts.DottedParts
import com.cyanharborstudios.callblock.ui.parts.Header
import com.cyanharborstudios.callblock.ui.parts.Key
import com.cyanharborstudios.callblock.ui.parts.Section
import com.cyanharborstudios.callblock.ui.parts.SectionHeading
import com.cyanharborstudios.callblock.ui.parts.Strip
import com.cyanharborstudios.callblock.ui.parts.SwitchboardIcons
import com.cyanharborstudios.callblock.ui.parts.Trail
import com.cyanharborstudios.callblock.ui.parts.pressTint
import com.cyanharborstudios.callblock.ui.parts.ruleBelow
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import java.time.ZoneId

/**
 * The frequent callers: the ones found in the app's own record and waiting for a word,
 * each with a key to block it, and the ones blocked, each with its figures and a way back.
 * Blocking, and Auto-block, are Plus's; the list itself is every plan's to read.
 */
@Composable
fun FrequentCallersScreen(viewModel: AppViewModel, onBack: () -> Unit, onOpenPlans: () -> Unit) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val seen by viewModel.seenCalls.collectAsStateWithLifecycle()
    val allowed by viewModel.allowedNumbers.collectAsStateWithLifecycle()
    val now = rememberNowMillis()
    val numbers = rememberPhoneNumbers()
    val formatCount = rememberCountFormat()
    val colors = MaterialTheme.colorScheme
    val plusMark = stringResource(R.string.plus_mark).uppercase(LocalConfiguration.current.locales[0])

    Column(Modifier.fillMaxSize().statusBarsPadding().testTag("frequent-screen")) {
        Box(Modifier.padding(horizontal = 16.dp)) { Header(stringResource(R.string.frequent_callers), onBack) }
        val current = settings ?: return
        val screening = current.screening
        val plus = Plans.has(current.tier, ProFeature.FREQUENT_CALLERS)
        val allowedKeys = liveAllowEntries(allowed, screening.allowListEnabled, now).keys
        val found = rememberFrequentCallers(seen, screening, allowedKeys, now)
        val calls = remember(seen) { seen.orEmpty().map { it.toSeenCall() } }
        val blocked = screening.frequentCallers.map { FrequentCallers.figures(calls, it, now, ZoneId.systemDefault()) }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Block a newly found one without asking. Plus's; on the other plans the row leads to the plans.
            Section(first = true) {
                if (plus) {
                    Strip(
                        title = stringResource(R.string.frequent_auto),
                        icon = SwitchboardIcons.repeat,
                        detail = stringResource(if (screening.frequentAutoBlock) R.string.on else R.string.off),
                        trail = Trail.Switch,
                        checked = screening.frequentAutoBlock,
                        onClick = { viewModel.setFrequentAutoBlock(!screening.frequentAutoBlock) },
                        head = true,
                        rule = false,
                        tag = "frequent-auto",
                    )
                } else {
                    Strip(
                        title = stringResource(R.string.frequent_auto),
                        icon = SwitchboardIcons.repeat,
                        detail = stringResource(R.string.frequent_auto_detail_locked),
                        value = plusMark,
                        onClick = onOpenPlans,
                        head = true,
                        rule = false,
                        tag = "frequent-auto",
                    )
                }
            }

            Section {
                SectionHeading(stringResource(R.string.frequent_found_heading))
                if (found.isEmpty()) {
                    Text(stringResource(R.string.frequent_none_yet), style = SwitchboardType.body, color = colors.onSurfaceVariant, modifier = Modifier.testTag("frequent-none"))
                }
                for (one in found) {
                    CallerRow(
                        display = displayOf(numbers, one),
                        parts = partsOf(one, formatCount),
                        tag = "frequent-found",
                    ) {
                        if (plus) {
                            Key(stringResource(R.string.frequent_block), onClick = { viewModel.blockFrequentCaller(one.start) }, fillWidth = false, horizontalPadding = 14.dp, tag = "frequent-block")
                        } else {
                            Text(plusMark, style = SwitchboardType.leadStrong, color = colors.onSurface, modifier = Modifier.clickable(onClick = onOpenPlans).testTag("frequent-block"))
                        }
                    }
                }
            }

            if (blocked.isNotEmpty()) {
                Section {
                    SectionHeading(stringResource(R.string.frequent_blocked_heading))
                    for (one in blocked) {
                        val display = displayOf(numbers, one)
                        CallerRow(display = display, parts = partsOf(one, formatCount), tag = "frequent-blocked") {
                            RemoveButton(stringResource(R.string.frequent_unblock, display), "frequent-unblock") { viewModel.unblockFrequentCaller(one.start) }
                        }
                    }
                }
            }
        }
    }
}

/** The frequent callers found in the record, worked out again only when the record or the settings change. */
@Composable
fun rememberFrequentCallers(seen: List<SeenCallEntity>?, screening: ScreeningSettings, allowedKeys: Set<String>, now: Long): List<CallerClass> =
    remember(seen, screening, allowedKeys, now / MINUTE_MILLIS) {
        if (seen == null) emptyList() else FrequentCallers.classes(seen.map { it.toSeenCall() }, now, ZoneId.systemDefault(), screening, allowedKeys)
    }

/** A class by what its numbers share, with an ellipsis for the digits that differ; one number as itself. */
private fun displayOf(numbers: PhoneNumbers, one: CallerClass): String =
    if (one.single) numbers.parse(one.start).display else numbers.startDisplay(one.start) + "…"

/** The figures, as parts: the calls, the numbers (for a class), the days. */
@Composable
private fun partsOf(one: CallerClass, formatCount: (Int) -> String): List<String> = buildList {
    add(pluralStringResource(R.plurals.calls, one.calls, formatCount(one.calls)))
    if (!one.single) add(pluralStringResource(R.plurals.numbers_count, one.numbers, one.numbers))
    add(pluralStringResource(R.plurals.days_count, one.days, one.days))
}

/** One caller: the number or the shared start, its figures under it, and one trailing part. */
@Composable
private fun CallerRow(display: String, parts: List<String>, tag: String, trailing: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .ruleBelow(colors.outlineVariant)
            .defaultMinSize(minHeight = 56.dp)
            .padding(vertical = 6.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(Modifier.weight(1f).semantics(mergeDescendants = true) { contentDescription = "$display, ${parts.joinToString(", ")}" }) {
            Text(display, style = SwitchboardType.number, color = colors.onSurface, maxLines = 1)
            DottedParts(parts, SwitchboardType.body, color = colors.onSurfaceVariant)
        }
        trailing()
    }
}

/** The 48 dp cross that takes a row out of a list, as Options' lists have it. */
@Composable
private fun RemoveButton(label: String, tag: String, onRemove: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Box(
        Modifier
            .offset(x = 12.dp)
            .size(48.dp)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onRemove)
            .pressTint(interaction)
            .testTag(tag)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(SwitchboardIcons.close, null, Modifier.size(20.dp), tint = colors.onSurfaceVariant)
    }
}

private const val MINUTE_MILLIS = 60_000L
