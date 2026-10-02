package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.numbers.PhoneNumber
import com.cyanharborstudios.callblock.core.stats.Statistics
import com.cyanharborstudios.callblock.core.stats.StatsSummary
import com.cyanharborstudios.callblock.core.time.TimeText
import com.cyanharborstudios.callblock.ui.theme.OutcomeColors
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.WeekFields

private val PERIODS = listOf(7, 30, 90)

/** Counts and charts worked out from the app's own log of handled calls. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatisticsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val calls by viewModel.handledCalls.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allowed by viewModel.allowedNumbers.collectAsStateWithLifecycle()
    val now = rememberNowMillis()
    val timeText = rememberTimeText()
    val numbers = rememberPhoneNumbers()
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]

    val periodDays = settings?.statsPeriodDays ?: 30
    val handled = remember(calls) { calls?.map { it.toHandledCall() } }
    val summary = remember(handled, now / 60_000, periodDays) {
        handled?.let { Statistics.summarize(it, now, ZoneId.systemDefault(), periodDays) }
    }
    var detailsFor by remember { mutableStateOf<PhoneNumber?>(null) }
    var allowing by remember { mutableStateOf<PhoneNumber?>(null) }
    val allowedKeys = allowed.orEmpty()
        .filter { it.expiresAtMillis == null || it.expiresAtMillis > now }
        .map { it.numberKey }
        .toSet()

    val shareText = summary?.let {
        stringResource(
            R.string.share_statistics_text,
            stringResource(R.string.app_name),
            it.allTime.total,
            it.allTime.blocked,
            it.allTime.silenced,
            Links.STORE_PAGE,
        )
    }

    AppScreen(
        title = stringResource(R.string.statistics),
        onBack = onBack,
        actions = {
            if (shareText != null && summary.allTime.total > 0) {
                IconButton(onClick = { Links.shareText(context, shareText) }) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.share))
                }
            }
        },
    ) { padding ->
        val stats = summary ?: return@AppScreen
        if (stats.allTime.total == 0) {
            EmptyMessage(stringResource(R.string.statistics_empty), padding)
            return@AppScreen
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("statistics"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Tile(stats.allTime.blocked, stringResource(R.string.blocked), OutcomeColors.blocked, Modifier.weight(1f))
                Tile(stats.allTime.silenced, stringResource(R.string.silenced), OutcomeColors.silenced, Modifier.weight(1f))
            }

            Section {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val reached = stats.milestone.reached ?: stats.allTime.total
                    Text(
                        pluralStringResource(R.plurals.calls_handled, reached, reached),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    stats.milestone.next?.let { next ->
                        LinearProgressIndicator(
                            progress = { stats.milestone.fractionToNext },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            stringResource(R.string.milestone_next, next),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HorizontalDivider()
                    Text(stringResource(R.string.time_saved, savedText(stats.secondsSaved)))
                    Text(
                        stringResource(R.string.time_saved_basis),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Section {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Heading(stringResource(R.string.last_7_days))
                    Text(calls(stats.lastSevenDays.total), style = MaterialTheme.typography.titleLarge)
                    stats.busiestHourLastSevenDays?.let {
                        Text(stringResource(R.string.busiest_around, timeText.hour(it)))
                    }
                    ChangeLine(stats.changeVsPreviousSevenDays, week = true)
                }
                HorizontalDivider()
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Heading(stringResource(R.string.last_30_days))
                    Text(calls(stats.lastThirtyDays.total), style = MaterialTheme.typography.titleLarge)
                    ChangeLine(stats.changeVsPreviousThirtyDays, week = false)
                }
            }

            DayByDayChart(stats, timeText)

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (days in PERIODS) {
                    FilterChip(
                        selected = periodDays == days,
                        onClick = { viewModel.setStatsPeriodDays(days) },
                        label = { Text(stringResource(R.string.period_days, days)) },
                    )
                }
            }

            WeekdayChart(stats, locale)
            HourChart(stats, timeText)

            if (stats.topNumbers.isNotEmpty()) {
                Section {
                    Heading(stringResource(R.string.most_frequent), Modifier.padding(start = 16.dp, top = 16.dp))
                    for (entry in stats.topNumbers) {
                        val number = numbers.parse(entry.numberRaw)
                        ListItem(
                            headlineContent = { Text(number.display) },
                            trailingContent = { Text(calls(entry.counts.total), style = MaterialTheme.typography.bodyLarge) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .clickable(role = Role.Button, onClick = { detailsFor = number })
                                .testTag("top-number"),
                        )
                    }
                }
            }
        }
    }

    detailsFor?.let { number ->
        val details = remember(handled, number) { Statistics.detailsFor(number.key, handled.orEmpty()) }
        if (details == null) {
            detailsFor = null
        } else {
            NumberDetailsSheet(
                number = number,
                details = details,
                isAllowed = number.key in allowedKeys,
                timeText = timeText,
                onAllow = {
                    detailsFor = null
                    allowing = number
                },
                onRemoveAllowed = { viewModel.removeAllowed(number.key) },
                onDismiss = { detailsFor = null },
            )
        }
    }
    allowing?.let { number ->
        AllowNumberDialog(
            fixedNumber = number,
            parse = numbers::parse,
            onAllow = { chosen, minutes ->
                viewModel.allow(chosen, minutes)
                allowing = null
            },
            onDismiss = { allowing = null },
        )
    }
}

@Composable
private fun calls(count: Int): String = pluralStringResource(R.plurals.calls, count, count)

@Composable
private fun Heading(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = modifier)
}

@Composable
private fun Tile(value: Int, label: String, color: Color, modifier: Modifier) {
    Section(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(value.toString(), style = MaterialTheme.typography.headlineMedium)
            Text(label, color = color, style = MaterialTheme.typography.titleSmall)
        }
    }
}

/** "12% fewer than the week before". Nothing at all when there is no earlier stretch to compare with. */
@Composable
private fun ChangeLine(percent: Int?, week: Boolean) {
    if (percent == null) return
    val text = when {
        percent < 0 -> stringResource(if (week) R.string.change_fewer_week else R.string.change_fewer_month, -percent)
        percent > 0 -> stringResource(if (week) R.string.change_more_week else R.string.change_more_month, percent)
        else -> stringResource(if (week) R.string.change_same_week else R.string.change_same_month)
    }
    Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun savedText(seconds: Long): String {
    val minutes = (seconds / 60).toInt()
    return when {
        minutes < 1 -> stringResource(R.string.duration_under_a_minute)
        minutes < 60 -> stringResource(R.string.duration_minutes, minutes)
        else -> stringResource(R.string.duration_hours_minutes, minutes / 60, minutes % 60)
    }
}

@Composable
private fun DayByDayChart(stats: StatsSummary, timeText: TimeText) {
    var selected by remember { mutableStateOf<Int?>(null) }
    val days = stats.dailyLastSevenDays
    val blockedColor = OutcomeColors.blocked
    val silencedColor = OutcomeColors.silenced
    val descriptions = days.map {
        stringResource(R.string.day_counts, timeText.weekday(it.date), it.counts.blocked, it.counts.silenced)
    }
    Section {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Heading(stringResource(R.string.day_by_day))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem(blockedColor, stringResource(R.string.blocked))
                LegendItem(silencedColor, stringResource(R.string.silenced))
            }
            BarChart(
                bars = days.mapIndexed { index, day ->
                    Bar(
                        listOf(Segment(day.counts.blocked, blockedColor), Segment(day.counts.silenced, silencedColor)),
                        descriptions[index],
                    )
                },
                selected = selected,
                onSelect = { selected = if (selected == it) null else it },
                gap = 8.dp,
            )
            AxisLabels(days.map { timeText.weekday(it.date) })
            selected?.let { Text(descriptions[it], color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun WeekdayChart(stats: StatsSummary, locale: java.util.Locale) {
    var selected by remember(stats.periodDays) { mutableStateOf<Int?>(null) }
    // Start the week where the user's region starts it.
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    val days: List<DayOfWeek> = (0L..6L).map { firstDay.plus(it) }
    val values = days.map { stats.byWeekday[it.value - 1] }
    val color = MaterialTheme.colorScheme.primary
    val descriptions = days.mapIndexed { index, day ->
        stringResource(R.string.bar_value, day.getDisplayName(TextStyle.FULL, locale), values[index])
    }
    Section {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Heading(stringResource(R.string.by_weekday))
            Statistics.busiestIndex(values)?.let {
                Text(stringResource(R.string.most_on, days[it].getDisplayName(TextStyle.FULL, locale)))
            }
            BarChart(
                bars = values.mapIndexed { index, value -> Bar(listOf(Segment(value, color)), descriptions[index]) },
                selected = selected,
                onSelect = { selected = if (selected == it) null else it },
                gap = 8.dp,
            )
            AxisLabels(days.map { it.getDisplayName(TextStyle.SHORT, locale) })
            selected?.let { Text(descriptions[it], color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun HourChart(stats: StatsSummary, timeText: TimeText) {
    var selected by remember(stats.periodDays) { mutableStateOf<Int?>(null) }
    val color = MaterialTheme.colorScheme.primary
    val descriptions = stats.byHour.mapIndexed { hour, value ->
        stringResource(R.string.bar_value, timeText.hour(hour), value)
    }
    Section {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Heading(stringResource(R.string.by_hour))
            Statistics.busiestIndex(stats.byHour)?.let {
                Text(stringResource(R.string.most_around, timeText.hour(it)))
            }
            BarChart(
                bars = stats.byHour.mapIndexed { hour, value -> Bar(listOf(Segment(value, color)), descriptions[hour]) },
                selected = selected,
                onSelect = { selected = if (selected == it) null else it },
                gap = 2.dp,
            )
            // One label under the first bar of each quarter of the day.
            AxisLabels(listOf(0, 6, 12, 18).map { timeText.hour(it) }, align = TextAlign.Start)
            selected?.let { Text(descriptions[it], color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun AxisLabels(labels: List<String>, align: TextAlign = TextAlign.Center) {
    Row(Modifier.fillMaxWidth()) {
        for (label in labels) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = align,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
