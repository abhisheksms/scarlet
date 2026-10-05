package com.cyanharborstudios.callblock.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.ModeNow
import com.cyanharborstudios.callblock.ui.parts.CapsText
import com.cyanharborstudios.callblock.ui.parts.Key
import com.cyanharborstudios.callblock.ui.parts.KeyChoice
import com.cyanharborstudios.callblock.ui.parts.KeysBlock
import com.cyanharborstudios.callblock.ui.parts.LatchingKeys
import com.cyanharborstudios.callblock.ui.parts.Sentence
import com.cyanharborstudios.callblock.ui.parts.Sheet
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType

/**
 * The timer: switch to Off, Silence or Block for a while, after which the schedule or the
 * lever takes over again. Choose where to, then tap how long; the length is the commit,
 * as on the number's sheet.
 */
@Composable
fun TimerSheet(
    inEffect: ModeNow,
    modeNames: Map<Mode, String>,
    /** What a running timer is doing, or null when none runs. */
    runningLine: String?,
    onStart: (mode: Mode, minutes: Int) -> Unit,
    onEnd: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val title = stringResource(R.string.timer)
    // Start on a mode other than the one in effect: that is what a timer is for.
    var target by rememberSaveable { mutableStateOf(if (inEffect.mode == Mode.BLOCK) Mode.SILENCE else Mode.BLOCK) }
    Sheet(label = title, onDismiss = onDismiss, tag = "timer-sheet") {
        Text(title, style = SwitchboardType.display, color = colors.onSurface, modifier = Modifier.semantics { heading() })
        if (runningLine != null) {
            Sentence(runningLine, SwitchboardType.body, Modifier.testTag("timer-running"), color = colors.onSurface)
            Key(stringResource(R.string.end_timer), onClick = { onEnd(); close() }, onPlate = true, tag = "timer-end")
        }
        val switchTo = stringResource(R.string.timer_switch_to)
        CapsText(switchTo, SwitchboardType.caption, color = colors.onSurfaceVariant)
        LatchingKeys(
            choices = Mode.entries.map { KeyChoice(it, modeNames.getValue(it)) },
            selected = target,
            onSelect = { target = it },
            modifier = Modifier.semantics { contentDescription = switchTo },
            onPlate = true,
            tag = { "timer-mode-${it.name}" },
        )
        KeysBlock(
            caption = stringResource(R.string.timer_for),
            choices = Durations.PAUSE.map { KeyChoice(it, shortDurationLabel(it), durationLabel(it)) },
            onChoose = { minutes ->
                onStart(target, minutes)
                close()
            },
            onPlate = true,
            tag = { "timer-for-$it" },
        )
    }
}
