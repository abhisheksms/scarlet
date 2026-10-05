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
import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.Scope
import com.cyanharborstudios.callblock.core.stats.Statistics
import com.cyanharborstudios.callblock.ui.parts.DisplayWindow
import com.cyanharborstudios.callblock.ui.parts.HomeStatus
import com.cyanharborstudios.callblock.ui.parts.KeyChoice
import com.cyanharborstudios.callblock.ui.parts.KeysBlock
import com.cyanharborstudios.callblock.ui.parts.LampState
import com.cyanharborstudios.callblock.ui.parts.Lever
import com.cyanharborstudios.callblock.ui.parts.LeverStop
import com.cyanharborstudios.callblock.ui.parts.MainKey
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
 * Home: the display window says what is happening now, the lever sets the mode, the
 * bay under it holds the one thing to do next, and two strips lead on. Nothing below
 * the display moves between states.
 */
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onOpenOptions: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenSettings: () -> Unit,
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

    // Choosing Silence or Block without the role asks Android for it first. The mode the
    // user picked is applied only if they accept; the lever waits at its old stop.
    var modeAwaitingRole by rememberSaveable { mutableStateOf<Mode?>(null) }
    val roleRequest = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.refreshSystemState()
        val wanted = modeAwaitingRole
        modeAwaitingRole = null
        if (wanted != null && viewModel.roleHeld.value) viewModel.setMode(wanted)
    }
    fun requestRole(thenMode: Mode?) {
        val intent = viewModel.roleRequestIntent() ?: return
        modeAwaitingRole = thenMode
        roleRequest.launch(intent)
    }
    val whenNotificationsAllowed = rememberNotificationRequest(viewModel)

    val current = settings ?: return
    val screening = current.screening
    val roleAvailable = viewModel.roleAvailable
    val paused = screening.mode != Mode.OFF && screening.timerMode == Mode.OFF && now < screening.timerUntilMillis
    val international = screening.scope == Scope.INTERNATIONAL_ONLY
    val summary = remember(calls, now / MINUTE_MILLIS) {
        calls?.let { Statistics.summarize(it.map { call -> call.toHandledCall() }, now, ZoneId.systemDefault(), 30) }
    }
    val total = summary?.allTime?.total ?: 0
    // On a device that cannot screen the lever rests at Off whatever was stored.
    val shownMode = if (roleAvailable) screening.mode else Mode.OFF

    val status: HomeStatus = when {
        !roleAvailable -> HomeStatus.Cannot
        shownMode != Mode.OFF && !roleHeld -> HomeStatus.RoleMissing
        paused -> HomeStatus.Paused(untilText(timeText, screening.timerUntilMillis, now))
        shownMode == Mode.OFF -> if (calls != null && total == 0) HomeStatus.First(international) else HomeStatus.Off
        shownMode == Mode.SILENCE -> HomeStatus.Silence(international)
        else -> HomeStatus.Block(international)
    }
    // Every state the window can be in, so it is laid out as tall as the tallest of them.
    val sampleTime = timeText.time(now)
    val candidates = buildList {
        add(HomeStatus.Off)
        add(HomeStatus.Cannot)
        add(HomeStatus.RoleMissing)
        for (intl in listOf(false, true)) {
            add(HomeStatus.Silence(intl))
            add(HomeStatus.Block(intl))
            if (total == 0) add(HomeStatus.First(intl))
        }
        add(HomeStatus.Paused(sampleTime))
        add(HomeStatus.Paused(stringResource(R.string.until_tomorrow, sampleTime)))
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

    val lamps: (Mode) -> LampState = { mode ->
        when {
            mode == Mode.OFF -> if (shownMode == Mode.OFF) LampState.Lit else LampState.Dark
            mode != shownMode || status is HomeStatus.RoleMissing || status is HomeStatus.Cannot -> LampState.Dark
            paused -> LampState.Held
            else -> LampState.Lit
        }
    }
    val stops = listOf(
        LeverStop(Mode.OFF, stringResource(R.string.mode_off), stringResource(R.string.mode_off_detail)),
        LeverStop(Mode.SILENCE, stringResource(R.string.mode_silence), statusSentence(HomeStatus.Silence(international))),
        LeverStop(Mode.BLOCK, stringResource(R.string.mode_block), statusSentence(HomeStatus.Block(international))),
    )

    val liveAllowed = if (screening.allowListEnabled) allowed.orEmpty().count { it.expiresAtMillis == null || it.expiresAtMillis > now } else 0
    val exceptions = buildList {
        if (screening.repeatCallsRing) add(stringResource(R.string.repeat_callers_ring))
        if (liveAllowed > 0) add(pluralStringResource(R.plurals.numbers_allowed, liveAllowed, liveAllowed))
        if (isEmpty()) add(stringResource(R.string.no_exceptions))
    }
    val notifying = current.notifyHandledCalls && access == NotificationAccess.ALLOWED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 8.dp, end = 16.dp)
            .semantics { isTraversalGroup = true },
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Read in this order: the sentence, the lever, the bay, the tiles, the strips, the gear.
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
            when (status) {
                HomeStatus.Cannot -> Unit
                HomeStatus.RoleMissing -> MainKey(roleButton, onClick = { requestRole(thenMode = null) }, tag = "set-screening-app")
                is HomeStatus.Paused -> MainKey(resume, onClick = { lastChangeWasLever = false; viewModel.resume() }, tag = "resume")
                HomeStatus.Off, is HomeStatus.First -> Sentence(privacy, SwitchboardType.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> KeysBlock(
                    caption = pauseCaption,
                    choices = pauseChoices,
                    onChoose = { minutes -> lastChangeWasLever = false; viewModel.pauseFor(minutes) },
                    tag = { "pause-$it" },
                )
            }
        }
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
    }
}

/** Home's summary only needs recomputing when the calls change or the minute rolls over. */
private const val MINUTE_MILLIS = 60_000L
