package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.plans.Plans
import com.cyanharborstudios.callblock.core.plans.ProFeature
import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.WeekSchedule
import com.cyanharborstudios.callblock.core.time.TimeText
import com.cyanharborstudios.callblock.ui.parts.CapsText
import com.cyanharborstudios.callblock.ui.parts.Header
import com.cyanharborstudios.callblock.ui.parts.HourCell
import com.cyanharborstudios.callblock.ui.parts.KeyChoice
import com.cyanharborstudios.callblock.ui.parts.LatchingKeys
import com.cyanharborstudios.callblock.ui.parts.Section
import com.cyanharborstudios.callblock.ui.parts.Sentence
import com.cyanharborstudios.callblock.ui.parts.Strip
import com.cyanharborstudios.callblock.ui.parts.TallestOf
import com.cyanharborstudios.callblock.ui.parts.Trail
import com.cyanharborstudios.callblock.ui.parts.WeekGrid
import com.cyanharborstudios.callblock.ui.parts.WeekRow
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/** What a touch on the grid sets: one of the lever's stops, or nothing. */
private enum class Brush(val mode: Mode?) { OFF(Mode.OFF), SILENCE(Mode.SILENCE), BLOCK(Mode.BLOCK), CLEAR(null) }

/**
 * The schedule: the hours of the week that set the mode by themselves. Pick what to set,
 * then drag across a day's hours, or across the top row for every day at once. An hour
 * left empty follows the lever.
 */
@Composable
fun ScheduleScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val timeText = rememberTimeText()
    val locale = LocalConfiguration.current.locales[0]

    Column(Modifier.fillMaxSize().statusBarsPadding().testTag("schedule-screen")) {
        Box(Modifier.padding(horizontal = 16.dp)) { Header(stringResource(R.string.schedule), onBack) }
        val current = settings ?: return
        // The schedule is Pro's. If the plan changes while this screen is open, leave it.
        if (!Plans.has(current.tier, ProFeature.SCHEDULE)) {
            LaunchedEffect(Unit) { onBack() }
            return
        }
        val screening = current.screening

        var brush by rememberSaveable { mutableStateOf(Brush.BLOCK) }
        // What this screen has set, shown at once; the settings file catches up a moment later.
        var edited by remember { mutableStateOf<WeekSchedule?>(null) }
        // During a drag: the schedule as it was when the drag began, and as the drag would leave it.
        var dragFrom by remember { mutableStateOf<WeekSchedule?>(null) }
        var dragTo by remember { mutableStateOf<WeekSchedule?>(null) }
        var readout by remember { mutableStateOf<String?>(null) }
        val kept = edited ?: screening.schedule
        val shown = dragTo ?: kept

        val modeNames = mapOf(
            Mode.OFF to stringResource(R.string.mode_off),
            Mode.SILENCE to stringResource(R.string.mode_silence),
            Mode.BLOCK to stringResource(R.string.mode_block),
        )
        val clearName = stringResource(R.string.schedule_clear)
        val brushName = brush.mode?.let { modeNames.getValue(it) } ?: clearName

        fun keep(schedule: WeekSchedule) {
            edited = schedule
            // Setting an hour switches the schedule on, as adding a number switches the allow list on.
            viewModel.setSchedule(schedule, switchOn = brush.mode != null && !schedule.isEmpty)
        }

        val days = remember(locale) { daysInOrder(locale) }
        val everyDay = stringResource(R.string.schedule_every_day)
        val rows = buildList {
            add(
                WeekRow(
                    tag = "schedule-row-ALL",
                    label = stringResource(R.string.schedule_every_day_short),
                    spoken = stringResource(R.string.schedule_day_spoken, everyDay, spokenHours(everyDayCells(shown), timeText, modeNames)),
                    days = days,
                    cells = everyDayCells(shown),
                ),
            )
            for (day in days) {
                val cells = (0..23).map { hour -> shown.modeAt(day, hour)?.let { HourCell.Set(it) } ?: HourCell.Empty }
                add(
                    WeekRow(
                        tag = "schedule-row-${day.name}",
                        label = day.getDisplayName(TextStyle.SHORT, locale),
                        spoken = stringResource(R.string.schedule_day_spoken, day.getDisplayName(TextStyle.FULL, locale), spokenHours(cells, timeText, modeNames)),
                        days = listOf(day),
                        cells = cells,
                    ),
                )
            }
        }

        val spanFormat = stringResource(R.string.schedule_span)
        fun spanWords(row: WeekRow, hours: IntRange): String {
            val dayName = if (row.days.size > 1) everyDay else row.label
            val span = String.format(locale, spanFormat, dayName, timeText.hour(hours.first), timeText.hour((hours.last + 1) % 24))
            return "$span: $brushName"
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Strip(
                title = stringResource(R.string.schedule),
                detail = stringResource(if (screening.scheduleOn) R.string.on else R.string.off),
                trail = Trail.Switch,
                checked = screening.scheduleOn,
                onClick = { viewModel.setScheduleOn(!screening.scheduleOn) },
                tag = "schedule-on",
            )

            Section(first = true) {
                val setHoursTo = stringResource(R.string.schedule_set_hours_to)
                CapsText(setHoursTo, SwitchboardType.strip, color = colors.onSurface)
                LatchingKeys(
                    choices = Brush.entries.map { KeyChoice(it, it.mode?.let(modeNames::getValue) ?: clearName) },
                    selected = brush,
                    onSelect = { brush = it },
                    modifier = Modifier.semantics { contentDescription = setHoursTo },
                    tag = { "brush-${it.name}" },
                )
            }

            Section {
                // One line that says what the last touch set. As tall as its longest wording, so the grid holds still.
                val hint = stringResource(R.string.schedule_hint_drag)
                TallestOf(
                    candidates = listOf(
                        { Sentence(hint, SwitchboardType.body) },
                        { Sentence(String.format(locale, spanFormat, everyDay, timeText.hour(22), timeText.hour(23)) + ": " + modeNames.values.maxBy { it.length }, SwitchboardType.body) },
                    ),
                    fillHeight = false,
                ) {
                    Sentence(
                        readout ?: hint,
                        SwitchboardType.body,
                        Modifier.testTag("schedule-readout").semantics { liveRegion = LiveRegionMode.Polite },
                        color = if (readout == null) colors.onSurfaceVariant else colors.onSurface,
                    )
                }
                WeekGrid(
                    rows = rows,
                    axisLabels = listOf(0, 6, 12, 18).map { timeText.hour(it) },
                    setWholeDayLabel = stringResource(R.string.schedule_set_whole_day, brushName),
                    clearDayLabel = stringResource(R.string.schedule_clear_day),
                    onDrag = { row, hours ->
                        val from = dragFrom ?: kept.also { dragFrom = it }
                        dragTo = from.with(row.days, hours, brush.mode)
                        readout = spanWords(row, hours)
                    },
                    onDragEnd = { keepIt ->
                        val result = dragTo
                        dragFrom = null
                        dragTo = null
                        if (keepIt && result != null) keep(result)
                    },
                    onTap = { row, hour ->
                        // A tap sets the hour; a tap on an hour that is already so clears it again.
                        val alreadySo = brush.mode != null && row.days.all { kept.modeAt(it, hour) == brush.mode }
                        keep(kept.with(row.days, hour..hour, if (alreadySo) null else brush.mode))
                        readout = spanWords(row, hour..hour)
                    },
                    onSetWholeDay = { row -> keep(kept.with(row.days, 0..23, brush.mode)) },
                    onClearDay = { row -> keep(kept.with(row.days, 0..23, null)) },
                )
                Sentence(stringResource(R.string.schedule_hint_empty), SwitchboardType.body, color = colors.onSurfaceVariant)
            }
        }
    }
}

/** The seven days starting where the phone's region starts its week. */
private fun daysInOrder(locale: Locale): List<DayOfWeek> {
    val first = WeekFields.of(locale).firstDayOfWeek
    return (0L..6L).map { first.plus(it) }
}

/** The top row: an hour shows a mode when every day has it, nothing when no day has anything, and a dot when the days differ. */
private fun everyDayCells(schedule: WeekSchedule): List<HourCell> = (0..23).map { hour ->
    val values = DayOfWeek.entries.map { schedule.modeAt(it, hour) }.distinct()
    when {
        values.size > 1 -> HourCell.Mixed
        values.single() == null -> HourCell.Empty
        else -> HourCell.Set(values.single()!!)
    }
}

/** A row's set hours in words: "Block from 10 PM to 12 AM, Silence from 9 AM to 6 PM". */
@Composable
private fun spokenHours(cells: List<HourCell>, timeText: TimeText, modeNames: Map<Mode, String>): String {
    if (cells.any { it == HourCell.Mixed }) return stringResource(R.string.schedule_day_mixed)
    val spans = mutableListOf<String>()
    var start = 0
    while (start < cells.size) {
        val cell = cells[start]
        var end = start
        while (end + 1 < cells.size && cells[end + 1] == cell) end++
        if (cell is HourCell.Set) {
            spans += stringResource(R.string.schedule_range_spoken, modeNames.getValue(cell.mode), timeText.hour(start), timeText.hour((end + 1) % 24))
        }
        start = end + 1
    }
    return if (spans.isEmpty()) stringResource(R.string.schedule_day_empty) else spans.joinToString(", ")
}
