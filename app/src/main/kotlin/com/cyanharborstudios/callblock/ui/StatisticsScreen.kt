package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.numbers.PhoneNumber
import com.cyanharborstudios.callblock.core.stats.Statistics
import com.cyanharborstudios.callblock.core.stats.StatsSummary
import com.cyanharborstudios.callblock.ui.parts.CapsText
import com.cyanharborstudios.callblock.ui.parts.DayChart
import com.cyanharborstudios.callblock.ui.parts.Drums
import com.cyanharborstudios.callblock.ui.parts.Header
import com.cyanharborstudios.callblock.ui.parts.HeaderIconButton
import com.cyanharborstudios.callblock.ui.parts.HourChart
import com.cyanharborstudios.callblock.ui.parts.KeyChoice
import com.cyanharborstudios.callblock.ui.parts.LatchingKeys
import com.cyanharborstudios.callblock.ui.parts.MarkLine
import com.cyanharborstudios.callblock.ui.parts.MilestoneBar
import com.cyanharborstudios.callblock.ui.parts.PlateShape
import com.cyanharborstudios.callblock.ui.parts.Section
import com.cyanharborstudios.callblock.ui.parts.SectionHeading
import com.cyanharborstudios.callblock.ui.parts.Sentence
import com.cyanharborstudios.callblock.ui.parts.SwitchboardIcons
import com.cyanharborstudios.callblock.ui.parts.WeekdayChart
import com.cyanharborstudios.callblock.ui.parts.pressTint
import com.cyanharborstudios.callblock.ui.parts.ruleBelow
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import com.cyanharborstudios.callblock.ui.theme.switchboard
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.WeekFields

private val PERIODS = listOf(7, 30, 90)

/**
 * Counts and charts worked out from the app's own log. Ranked: the all-time counter
 * is the headline, the last 7 days come second, and the period keys with what they
 * drive come last.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatisticsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val calls by viewModel.handledCalls.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allowed by viewModel.allowedNumbers.collectAsStateWithLifecycle()
    val now = rememberNowMillis()
    val timeText = rememberTimeText()
    val numbers = rememberPhoneNumbers()
    val formatCount = rememberCountFormat()
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val colors = MaterialTheme.colorScheme
    val extra = switchboard

    val periodDays = settings?.statsPeriodDays ?: 30
    val handled = remember(calls) { calls?.map { it.toHandledCall() } }
    val summary = remember(handled, now / 60_000, periodDays) {
        handled?.let { Statistics.summarize(it, now, ZoneId.systemDefault(), periodDays) }
    }
    var selectedDay by rememberSaveable { mutableIntStateOf(6) }
    var selectedHour by remember(periodDays) { mutableStateOf<Int?>(null) }
    var openNumber by remember { mutableStateOf<PhoneNumber?>(null) }
    val liveAllowed = liveAllowEntries(allowed, settings?.screening?.allowListEnabled == true, now)

    val shareText = summary?.let {
        stringResource(
            R.string.share_statistics_text,
            stringResource(R.string.app_name),
            formatCount(it.allTime.total),
            formatCount(it.allTime.blocked),
            formatCount(it.allTime.silenced),
            Links.STORE_PAGE,
        )
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Box(Modifier.padding(horizontal = 16.dp)) {
            Header(
                stringResource(R.string.statistics),
                onBack,
                action = if (shareText != null && summary.allTime.total > 0) {
                    { HeaderIconButton(SwitchboardIcons.share, stringResource(R.string.share), onClick = { Links.shareText(context, shareText) }, tag = "share") }
                } else {
                    null
                },
            )
        }
        val stats = summary ?: return
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                .testTag("statistics"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AllTimeWindow(stats, formatCount)
            if (stats.allTime.total == 0) return

            val week = stats.lastSevenDays
            val dayLabels = stats.dailyLastSevenDays.map { timeText.weekday(it.date) }
            val dayDescriptions = stats.dailyLastSevenDays.map {
                stringResource(R.string.day_counts, it.date.dayOfWeek.getDisplayName(TextStyle.FULL, locale), formatCount(it.counts.blocked), formatCount(it.counts.silenced))
            }
            Section(first = true) {
                SectionHeading(stringResource(R.string.last_7_days)) {
                    Text(pluralStringResource(R.plurals.calls, week.total, formatCount(week.total)), style = SwitchboardType.lead, color = colors.onSurface, modifier = Modifier.alignByBaseline())
                }
                DayChart(
                    days = stats.dailyLastSevenDays.map { it.counts },
                    labels = dayLabels,
                    descriptions = dayDescriptions,
                    selected = selectedDay,
                    onSelect = { selectedDay = it },
                    formatCount = formatCount,
                )
                val chosen = stats.dailyLastSevenDays[selectedDay]
                // The line repeats the selected bar, which a screen reader already has.
                Text(
                    stringResource(R.string.day_counts, dayLabels[selectedDay], formatCount(chosen.counts.blocked), formatCount(chosen.counts.silenced)),
                    style = SwitchboardType.body,
                    color = colors.onSurface,
                    modifier = Modifier.clearAndSetSemantics { },
                )
                if (week.total > 0) {
                    val change = changeText(stats.changeVsPreviousSevenDays, week = true)
                    val busiest = stats.busiestHourLastSevenDays?.let { stringResource(R.string.busiest_around, timeText.hour(it)) }
                    val line = listOfNotNull(change, busiest).joinToString(" ")
                    if (line.isNotEmpty()) Sentence(line, SwitchboardType.body, color = colors.onSurfaceVariant)
                }
            }

            // Only once the history is older than a week, so a new user is not shown one count three times.
            val month = stats.lastThirtyDays
            if (month.total > week.total || stats.allTime.total > month.total) {
                Section {
                    SectionHeading(stringResource(R.string.last_30_days)) {
                        Text(pluralStringResource(R.plurals.calls, month.total, formatCount(month.total)), style = SwitchboardType.lead, color = colors.onSurface, modifier = Modifier.alignByBaseline())
                    }
                    changeText(stats.changeVsPreviousThirtyDays, week = false)?.let { Sentence(it, SwitchboardType.body, color = colors.onSurfaceVariant) }
                }
            }

            Section {
                val periodLabel = stringResource(R.string.period)
                LatchingKeys(
                    choices = PERIODS.map { KeyChoice(it, stringResource(R.string.period_days, it), stringResource(R.string.period_days_spoken, it)) },
                    selected = periodDays,
                    onSelect = viewModel::setStatsPeriodDays,
                    modifier = Modifier.semantics { contentDescription = periodLabel },
                    tag = { "period-$it" },
                )
            }

            // Start the week where the user's region starts it.
            val firstDay = WeekFields.of(locale).firstDayOfWeek
            val weekdays: List<DayOfWeek> = (0L..6L).map { firstDay.plus(it) }
            val weekdayValues = weekdays.map { stats.byWeekday[it.value - 1] }
            Section(first = true) {
                SectionHeading(stringResource(R.string.by_weekday)) {
                    Statistics.busiestIndex(weekdayValues)?.let {
                        Text(stringResource(R.string.most_on, weekdays[it].getDisplayName(TextStyle.FULL, locale)), style = SwitchboardType.body, color = colors.onSurfaceVariant, modifier = Modifier.alignByBaseline())
                    }
                }
                WeekdayChart(
                    values = weekdayValues,
                    labels = weekdays.map { it.getDisplayName(TextStyle.SHORT, locale) },
                    descriptions = weekdays.mapIndexed { index, day -> stringResource(R.string.bar_value, day.getDisplayName(TextStyle.FULL, locale), formatCount(weekdayValues[index])) },
                    formatCount = formatCount,
                )
            }

            Section(first = true) {
                SectionHeading(stringResource(R.string.by_hour)) {
                    val line = selectedHour?.let { hour ->
                        stringResource(R.string.bar_value, timeText.hour(hour), pluralStringResource(R.plurals.calls, stats.byHour[hour], formatCount(stats.byHour[hour])))
                    } ?: Statistics.busiestIndex(stats.byHour)?.let { stringResource(R.string.most_around, timeText.hour(it)) }
                    if (line != null) {
                        Text(line, style = SwitchboardType.body, color = colors.onSurfaceVariant, modifier = Modifier.alignByBaseline().semantics { liveRegion = LiveRegionMode.Polite })
                    }
                }
                HourChart(
                    values = stats.byHour,
                    descriptions = stats.byHour.mapIndexed { hour, value -> stringResource(R.string.bar_value, timeText.hour(hour), formatCount(value)) },
                    axisLabels = listOf(0, 6, 12, 18).map { timeText.hour(it) },
                    selected = selectedHour,
                    onSelect = { selectedHour = it },
                )
            }

            if (stats.topNumbers.isNotEmpty()) {
                Section(first = true) {
                    CapsText(stringResource(R.string.most_frequent), SwitchboardType.section, color = colors.onSurface)
                    Column {
                        for (entry in stats.topNumbers) {
                            val number = numbers.parse(entry.numberRaw)
                            FrequentRow(
                                display = number.display,
                                allowLine = liveAllowed[entry.numberKey]?.let { allowLine(it, timeText, now) },
                                count = pluralStringResource(R.plurals.calls, entry.counts.total, formatCount(entry.counts.total)),
                                onOpen = { openNumber = number },
                            )
                        }
                    }
                }
            }
        }
    }

    openNumber?.let { number ->
        NumberDetailsSheet(
            number = number,
            calls = calls.orEmpty(),
            thisCall = null,
            allowEntry = liveAllowed[number.key],
            timeText = timeText,
            now = now,
            formatCount = formatCount,
            onAllow = { minutes -> viewModel.allow(number, minutes) },
            onRemoveAllowed = { viewModel.removeAllowed(number.key) },
            onDeleteCall = null,
            onDismiss = { openNumber = null },
        )
    }
}

/** The dark window at the top: the counter on drums, the milestone bar, and the quiet time-saved line. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AllTimeWindow(stats: StatsSummary, formatCount: (Int) -> String) {
    val colors = MaterialTheme.colorScheme
    val extra = switchboard
    val total = stats.allTime.total
    val highlight = colors.surfaceContainerHighest
    val reachedText = stats.milestone.reached?.let { stringResource(R.string.milestone_reached, formatCount(it)) }
    val nextText = stats.milestone.next?.let { stringResource(R.string.milestone_next, formatCount(it)) }
    Column(
        Modifier
            .fillMaxWidth()
            .drawBehind { drawRoundRect(highlight, Offset(0f, 1.dp.toPx()), size, androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())) }
            .background(colors.inverseSurface, PlateShape)
            .drawBehind { drawRect(Color.Black, Offset(6.dp.toPx(), 0f), Size(size.width - 12.dp.toPx(), 2.dp.toPx())) }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        val stopped = pluralStringResource(R.plurals.calls_stopped, total, formatCount(total))
        val blockedText = stringResource(R.string.n_blocked, formatCount(stats.allTime.blocked))
        val silencedText = stringResource(R.string.n_silenced, formatCount(stats.allTime.silenced))
        FlowRow(
            Modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = "$stopped. $blockedText, $silencedText." },
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Drums(total, spoken = formatCount(total))
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                CapsText(pluralStringResource(R.plurals.calls_stopped_caption, total), SwitchboardType.caption, color = extra.inverseOnSurfaceVariant)
                MarkLine(true, blockedText, SwitchboardType.lead, color = colors.inverseOnSurface)
                MarkLine(false, silencedText, SwitchboardType.lead, color = colors.inverseOnSurface)
            }
        }
        MilestoneBar(
            total = total,
            reached = stats.milestone.reached,
            next = stats.milestone.next,
            reachedText = reachedText,
            nextText = nextText,
            spoken = listOfNotNull(reachedText?.let { "$it." }, nextText).joinToString(" "),
        )
        if (total > 0) {
            Text(
                stringResource(R.string.time_saved, savedText(stats.secondsSaved, formatCount)),
                style = SwitchboardType.note.copy(lineHeight = SwitchboardType.strip.lineHeight),
                color = extra.inverseOnSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind { drawRect(extra.inverseOutlineVariant, Offset.Zero, Size(size.width, 1.dp.toPx())) }
                    .padding(top = 10.dp),
            )
        }
    }
}

/** "12% fewer than the week before." Nothing at all when there is no earlier stretch to compare with. */
@Composable
private fun changeText(percent: Int?, week: Boolean): String? {
    if (percent == null) return null
    return when {
        percent < 0 -> stringResource(if (week) R.string.change_fewer_week else R.string.change_fewer_month, -percent)
        percent > 0 -> stringResource(if (week) R.string.change_more_week else R.string.change_more_month, percent)
        else -> stringResource(if (week) R.string.change_same_week else R.string.change_same_month)
    }
}

@Composable
private fun savedText(seconds: Long, formatCount: (Int) -> String): String {
    val minutes = (seconds / 60).toInt()
    return when {
        minutes < 1 -> stringResource(R.string.duration_under_a_minute)
        minutes < 60 -> stringResource(R.string.duration_minutes, minutes)
        else -> stringResource(R.string.duration_hours_minutes, formatCount(minutes / 60), minutes % 60)
    }
}

/** One of the most frequent numbers: the number, whether it is allowed, and its count. */
@Composable
private fun FrequentRow(display: String, allowLine: String?, count: String, onOpen: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Row(
        Modifier
            .testTag("top-number")
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onOpen)
            .pressTint(interaction)
            .semantics(mergeDescendants = true) { contentDescription = listOfNotNull(display, allowLine?.replaceFirstChar { it.lowercase() }, count).joinToString(", ") }
            .ruleBelow(colors.outlineVariant)
            .defaultMinSize(minHeight = 52.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(display, style = SwitchboardType.number, color = colors.onSurface, maxLines = 1)
            if (allowLine != null) {
                Text(allowLine, style = SwitchboardType.body.copy(lineHeight = SwitchboardType.lead.lineHeight, fontWeight = androidx.compose.ui.text.font.FontWeight.W500), color = colors.onSurface)
            }
        }
        Text(count, style = SwitchboardType.lead, color = colors.onSurfaceVariant, maxLines = 1)
    }
}
