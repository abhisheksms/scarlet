package com.cyanharborstudios.callblock.ui

import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.plans.Plans
import com.cyanharborstudios.callblock.core.plans.ProFeature
import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.ModeClock
import com.cyanharborstudios.callblock.core.rules.ModeNow
import com.cyanharborstudios.callblock.core.rules.ModeSource
import com.cyanharborstudios.callblock.core.rules.Scope
import com.cyanharborstudios.callblock.core.stats.Statistics
import com.cyanharborstudios.callblock.core.time.DayRelation
import com.cyanharborstudios.callblock.core.time.TimeText
import com.cyanharborstudios.callblock.ui.parts.DisplayWindow
import com.cyanharborstudios.callblock.ui.parts.HomeStatus
import com.cyanharborstudios.callblock.ui.parts.KeyChoice
import com.cyanharborstudios.callblock.ui.parts.KeysBlock
import com.cyanharborstudios.callblock.ui.parts.LampState
import com.cyanharborstudios.callblock.ui.parts.Lever
import com.cyanharborstudios.callblock.ui.parts.LeverStop
import com.cyanharborstudios.callblock.ui.parts.MainKey
import com.cyanharborstudios.callblock.ui.parts.Section
import com.cyanharborstudios.callblock.ui.parts.SectionHeading
import com.cyanharborstudios.callblock.ui.parts.Sentence
import com.cyanharborstudios.callblock.ui.parts.Strip
import com.cyanharborstudios.callblock.ui.parts.Strips
import com.cyanharborstudios.callblock.ui.parts.TallestOf
import com.cyanharborstudios.callblock.ui.parts.TileCounts
import com.cyanharborstudios.callblock.ui.parts.Trail
import com.cyanharborstudios.callblock.ui.parts.statusSentence
import com.cyanharborstudios.callblock.ui.theme.LocalReducedMotion
import com.cyanharborstudios.callblock.ui.theme.Motion
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.ZoneId

/**
 * Home: the display window says what is happening now, the lever shows the mode in
 * effect and sets it, the bay under it holds the one thing to do next, the Automatic
 * section holds the timer and the schedule, and two strips lead on. Nothing below the
 * display moves between states.
 *
 * The lever always stands at the mode in effect. A timer or the schedule can put it
 * there, and then the display's second line says until when and what follows.
 */
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onOpenOptions: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenPlans: () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val calls by viewModel.handledCalls.collectAsStateWithLifecycle()
    val allowed by viewModel.allowedNumbers.collectAsStateWithLifecycle()
    val roleHeld by viewModel.roleHeld.collectAsStateWithLifecycle()
    val access = rememberNotificationAccess(viewModel)
    val now = rememberNowMillis()
    val timeText = rememberTimeText()
    val formatCount = rememberCountFormat()
    val context = LocalContext.current
    val view = LocalView.current
    val reduced = LocalReducedMotion.current
    val scope = rememberCoroutineScope()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshSystemState() }

    // Choosing Silence or Block without the role asks Android for it first. What the user
    // picked (a stop of the lever, or a timer) is applied only if they accept.
    var modeAwaitingRole by rememberSaveable { mutableStateOf<Mode?>(null) }
    var timerMinutesAwaitingRole by rememberSaveable { mutableStateOf<Int?>(null) }
    val roleRequest = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.refreshSystemState()
        val wanted = modeAwaitingRole
        val minutes = timerMinutesAwaitingRole
        modeAwaitingRole = null
        timerMinutesAwaitingRole = null
        if (wanted != null && viewModel.roleHeld.value) {
            if (minutes == null) viewModel.setMode(wanted) else viewModel.startTimer(wanted, minutes)
        }
    }
    fun requestRole(thenMode: Mode?, forMinutes: Int? = null) {
        val intent = viewModel.roleRequestIntent() ?: return
        modeAwaitingRole = thenMode
        timerMinutesAwaitingRole = forMinutes
        roleRequest.launch(intent)
    }
    val whenNotificationsAllowed = rememberNotificationRequest(viewModel)

    val current = settings ?: return
    val screening = current.screening
    val roleAvailable = viewModel.roleAvailable
    val pro = Plans.has(current.tier, ProFeature.TIMER)
    val international = screening.scope == Scope.INTERNATIONAL_ONLY
    val inEffect = ModeClock.at(screening, now, ZoneId.systemDefault())
    val timerRunning = inEffect.source == ModeSource.TIMER
    val asksToFilter = screening.asksToFilter(now)
    val summary = remember(calls, now / MINUTE_MILLIS) {
        calls?.let { Statistics.summarize(it.map { call -> call.toHandledCall() }, now, ZoneId.systemDefault(), 30) }
    }
    val total = summary?.allTime?.total ?: 0
    // On a device that cannot screen the lever rests at Off whatever was stored.
    val shownMode = if (roleAvailable) inEffect.mode else Mode.OFF

    val modeNames = mapOf(
        Mode.OFF to stringResource(R.string.mode_off),
        Mode.SILENCE to stringResource(R.string.mode_silence),
        Mode.BLOCK to stringResource(R.string.mode_block),
    )
    // The second line is Pro's: only there can a timer hold Silence or Block, or a schedule run.
    // On the other plans the only timer is a pause, whose sentence already says until when, so
    // the display, and with it everything below, stays the height it always was.
    val note = if (pro) noteFor(inEffect, now, timeText, modeNames) else null

    val status: HomeStatus = when {
        !roleAvailable -> HomeStatus.Cannot
        asksToFilter && !roleHeld -> HomeStatus.RoleMissing
        timerRunning && inEffect.mode == Mode.OFF -> HomeStatus.Paused(untilText(timeText, screening.timerUntilMillis, now), note)
        inEffect.mode == Mode.OFF -> if (calls != null && total == 0 && !asksToFilter) HomeStatus.First(international) else HomeStatus.Off(note)
        inEffect.mode == Mode.SILENCE -> HomeStatus.Silence(international, note)
        else -> HomeStatus.Block(international, note)
    }
    // Every state the window can be in, so it is laid out as tall as the tallest of them.
    val sampleTime = timeText.time(now)
    val sampleTomorrow = stringResource(R.string.until_tomorrow, sampleTime)
    val longestName = modeNames.values.maxBy { it.length }
    val candidates = buildList {
        add(HomeStatus.Off())
        add(HomeStatus.Cannot)
        add(HomeStatus.RoleMissing)
        for (intl in listOf(false, true)) {
            add(HomeStatus.Silence(intl))
            add(HomeStatus.Block(intl))
            if (total == 0) add(HomeStatus.First(intl))
        }
        val thenLongest = if (pro) stringResource(R.string.then_mode, longestName) else null
        add(HomeStatus.Paused(sampleTime, thenLongest))
        add(HomeStatus.Paused(sampleTomorrow, thenLongest))
        if (pro) {
            val longestNote = stringResource(R.string.schedule_note, sampleTomorrow, longestName)
            add(HomeStatus.Off(longestNote))
            for (intl in listOf(false, true)) {
                add(HomeStatus.Silence(intl, longestNote))
                add(HomeStatus.Block(intl, longestNote))
            }
        }
    }

    // The sentence waits for the handle after a lever move, and changes at once otherwise.
    var lastChangeWasLever by remember { mutableStateOf(false) }

    fun chooseMode(mode: Mode) {
        if (mode == Mode.OFF || roleHeld) {
            lastChangeWasLever = true
            viewModel.setMode(mode)
            scope.launch {
                delay(if (reduced) 0L else Motion.LEVER_TRAVEL.toLong())
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        } else {
            requestRole(thenMode = mode)
        }
    }

    // A lamp says its stop is in effect. It is a ring, not a full lamp, while a timer or the
    // schedule holds the lever there and will let go by itself.
    val lamps: (Mode) -> LampState = { mode ->
        when {
            mode == Mode.OFF -> if (shownMode == Mode.OFF) LampState.Lit else LampState.Dark
            mode != shownMode || status is HomeStatus.RoleMissing || status is HomeStatus.Cannot -> LampState.Dark
            inEffect.source != ModeSource.LEVER -> LampState.Held
            else -> LampState.Lit
        }
    }
    val stops = listOf(
        LeverStop(Mode.OFF, modeNames.getValue(Mode.OFF), stringResource(R.string.mode_off_detail)),
        LeverStop(Mode.SILENCE, modeNames.getValue(Mode.SILENCE), statusSentence(HomeStatus.Silence(international))),
        LeverStop(Mode.BLOCK, modeNames.getValue(Mode.BLOCK), statusSentence(HomeStatus.Block(international))),
    )

    val liveAllowed = if (screening.allowListEnabled) allowed.orEmpty().count { it.expiresAtMillis == null || it.expiresAtMillis > now } else 0
    val exceptions = buildList {
        if (screening.callBacksRing) add(stringResource(R.string.call_backs_ring))
        if (screening.repeatCallsRing) add(stringResource(R.string.repeat_callers_ring))
        if (liveAllowed > 0) add(pluralStringResource(R.plurals.numbers_allowed, liveAllowed, liveAllowed))
        if (isEmpty()) add(stringResource(R.string.no_exceptions))
    }
    val notifying = current.notifyHandledCalls && access == NotificationAccess.ALLOWED
    var timerOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 8.dp, end = 16.dp)
            .semantics { isTraversalGroup = true },
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Read in this order: the sentence, the lever, the bay, the tiles, the timer and the schedule, the strips, the gear.
        DisplayWindow(
            status = status,
            candidates = candidates,
            counts = TileCounts(
                today = summary?.today?.total ?: 0,
                weekBlocked = summary?.lastSevenDays?.blocked ?: 0,
                weekSilenced = summary?.lastSevenDays?.silenced ?: 0,
                total = total,
            ),
            swapDelay = if (lastChangeWasLever) Motion.DISPLAY_SWAP_AFTER_LEVER else 0,
            onOpenHistory = onOpenHistory,
            onOpenStatistics = onOpenStatistics,
            onOpenSettings = onOpenSettings,
            formatCount = formatCount,
        )
        Lever(
            stops = stops,
            mode = shownMode,
            lamps = lamps,
            locked = !roleAvailable,
            onChoose = ::chooseMode,
            modifier = Modifier.semantics { traversalIndex = 1f },
        )
        val pauseChoices = Durations.PAUSE.map { KeyChoice(it, shortDurationLabel(it), durationLabel(it)) }
        val privacy = stringResource(R.string.privacy_line)
        val pauseCaption = stringResource(R.string.pause_caption)
        val resume = stringResource(R.string.resume)
        val endTimer = stringResource(R.string.end_timer)
        val roleButton = stringResource(R.string.role_request)
        TallestOf(
            candidates = listOf(
                { KeysBlock(pauseCaption, pauseChoices, {}) },
                { MainKey(resume, {}) },
                { MainKey(roleButton, {}) },
                { Sentence(privacy, SwitchboardType.note) },
            ),
            modifier = Modifier.fillMaxWidth().semantics { traversalIndex = 2f },
            fillHeight = false,
        ) {
            when {
                status == HomeStatus.Cannot -> Unit
                status == HomeStatus.RoleMissing -> MainKey(roleButton, onClick = { requestRole(thenMode = null) }, tag = "set-screening-app")
                // One key ends whatever timer is running: a pause is resumed, a hold at Silence or Block is ended.
                timerRunning && inEffect.mode == Mode.OFF -> MainKey(resume, onClick = { lastChangeWasLever = false; viewModel.resume() }, tag = "resume")
                timerRunning -> MainKey(endTimer, onClick = { lastChangeWasLever = false; viewModel.resume() }, tag = "end-timer")
                inEffect.mode == Mode.OFF -> Sentence(privacy, SwitchboardType.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> KeysBlock(
                    caption = pauseCaption,
                    choices = pauseChoices,
                    onChoose = { minutes -> lastChangeWasLever = false; viewModel.pauseFor(minutes) },
                    tag = { "pause-$it" },
                )
            }
        }

        // The timer and the schedule: the two things that move the lever by themselves. Pro has
        // them, and there they sit right under the lever. On the other plans each row says what
        // it would do and leads to the plans, and the section comes after Options and
        // Notifications: a row that is for sale never pushes a free one down the screen.
        val automatic: @Composable () -> Unit = {
            Section(Modifier.semantics { traversalIndex = if (pro) 4f else 7f }) {
                SectionHeading(stringResource(R.string.automatic))
                val proMark = stringResource(R.string.pro_mark).uppercase(LocalConfiguration.current.locales[0])
                Strip(
                    title = stringResource(R.string.timer),
                    detail = when {
                        !pro -> stringResource(R.string.timer_detail_locked)
                        timerRunning -> stringResource(R.string.timer_running, modeNames.getValue(inEffect.mode), untilText(timeText, screening.timerUntilMillis, now))
                        else -> stringResource(R.string.off)
                    },
                    trail = if (pro) Trail.Chevron else Trail.None,
                    value = if (pro) null else proMark,
                    onClick = { if (pro) timerOpen = true else onOpenPlans() },
                    tag = "open-timer",
                )
                val scheduledHours = screening.schedule.hours.count { it != null }
                Strip(
                    title = stringResource(R.string.schedule),
                    detail = if (pro) null else stringResource(R.string.schedule_detail_locked),
                    detailParts = when {
                        !pro -> null
                        screening.scheduleOn && scheduledHours > 0 -> listOf(
                            stringResource(R.string.on),
                            pluralStringResource(R.plurals.schedule_hours_a_week, scheduledHours, scheduledHours),
                        )
                        else -> listOf(stringResource(R.string.off))
                    },
                    trail = if (pro) Trail.Chevron else Trail.None,
                    value = if (pro) null else proMark,
                    onClick = { if (pro) onOpenSchedule() else onOpenPlans() },
                    rule = false,
                    tag = "open-schedule",
                )
            }
        }
        if (pro) automatic()

        Strips {
            Strip(
                title = stringResource(R.string.options),
                detailParts = exceptions,
                trail = Trail.Chevron,
                onClick = onOpenOptions,
                tag = "open-options",
                modifier = Modifier.semantics { traversalIndex = 5f },
            )
            if (access == NotificationAccess.BLOCKED) {
                Strip(
                    title = stringResource(R.string.notifications),
                    detail = stringResource(R.string.notifications_blocked),
                    trail = Trail.Out,
                    onClick = { openNotificationSettings(context) },
                    spoken = "${stringResource(R.string.notifications)}. ${stringResource(R.string.notifications_blocked)}. ${stringResource(R.string.open_settings)}",
                    rule = false,
                    tag = "notifications",
                    modifier = Modifier.semantics { traversalIndex = 6f },
                )
            } else {
                Strip(
                    title = stringResource(R.string.notifications),
                    detail = stringResource(if (notifying) R.string.notifications_on else R.string.notifications_off),
                    trail = Trail.Switch,
                    checked = notifying,
                    onClick = {
                        if (notifying) viewModel.setNotifyHandledCalls(false) else whenNotificationsAllowed { viewModel.setNotifyHandledCalls(true) }
                    },
                    rule = false,
                    tag = "notifications",
                    modifier = Modifier.semantics { traversalIndex = 6f },
                )
            }
        }
        if (!pro) automatic()
    }

    if (timerOpen) {
        TimerSheet(
            inEffect = inEffect,
            modeNames = modeNames,
            runningLine = if (timerRunning) {
                val untilAndNext = stringResource(
                    R.string.timer_note,
                    untilText(timeText, screening.timerUntilMillis, now),
                    modeNames.getValue(inEffect.next ?: screening.mode),
                )
                "${modeNames.getValue(inEffect.mode)}. $untilAndNext"
            } else {
                null
            },
            onStart = { mode, minutes ->
                lastChangeWasLever = false
                if (mode == Mode.OFF || roleHeld) viewModel.startTimer(mode, minutes) else requestRole(thenMode = mode, forMinutes = minutes)
            },
            onEnd = { lastChangeWasLever = false; viewModel.resume() },
            onDismiss = { timerOpen = false },
        )
    }
}

/**
 * The display's second line: while a timer or the schedule holds the mode, until when and
 * what follows; and, with the lever in charge, the schedule's next change if it comes
 * within a day. Null when nothing is due to change by itself.
 */
@Composable
private fun noteFor(inEffect: ModeNow, now: Long, timeText: TimeText, modeNames: Map<Mode, String>): String? {
    val until = inEffect.untilMillis
    val next = inEffect.next
    if (until == null || next == null) {
        return if (inEffect.source == ModeSource.SCHEDULE) stringResource(R.string.schedule_note_always) else null
    }
    val untilWords = untilText(timeText, until, now)
    val nextName = modeNames.getValue(next)
    return when (inEffect.source) {
        // A pause's sentence already says until when.
        ModeSource.TIMER -> if (inEffect.mode == Mode.OFF) stringResource(R.string.then_mode, nextName) else stringResource(R.string.timer_note, untilWords, nextName)
        ModeSource.SCHEDULE -> stringResource(R.string.schedule_note, untilWords, nextName)
        ModeSource.LEVER -> if (timeText.dayRelation(until, now) == DayRelation.OTHER) null else stringResource(R.string.schedule_next_note, untilWords, nextName)
    }
}

/** Home's summary only needs recomputing when the calls change or the minute rolls over. */
private const val MINUTE_MILLIS = 60_000L
