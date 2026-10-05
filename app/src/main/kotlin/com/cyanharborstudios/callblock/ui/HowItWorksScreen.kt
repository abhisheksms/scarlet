package com.cyanharborstudios.callblock.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.ui.parts.CapsText
import com.cyanharborstudios.callblock.ui.parts.Header
import com.cyanharborstudios.callblock.ui.parts.MainKey
import com.cyanharborstudios.callblock.ui.parts.Plate
import com.cyanharborstudios.callblock.ui.parts.Section
import com.cyanharborstudios.callblock.ui.parts.SectionHeading
import com.cyanharborstudios.callblock.ui.parts.Sentence
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType

/**
 * The tutorial section: who always rings, what the lever does to every other call, how
 * to let one through, and where the stopped ones go. It opens by itself until it has
 * been closed once, and from Settings after that. One idea to a sentence.
 */
@Composable
fun HowItWorksScreen(
    /** How long a number the user called may ring back, or null when that is switched off. */
    callBackMinutes: Int?,
    onDone: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    // The system's back closes it as the arrow and the key do: one press, wherever it was opened from.
    BackHandler(onBack = onDone)
    Column(Modifier.fillMaxSize().statusBarsPadding().testTag("how-screen")) {
        Box(Modifier.padding(horizontal = 16.dp)) { Header(stringResource(R.string.how_it_works), onDone) }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Section(first = true) {
                SectionHeading(stringResource(R.string.how_always_heading))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val always = buildList {
                        add(stringResource(R.string.how_always_contacts))
                        if (callBackMinutes != null) add(stringResource(R.string.how_always_called, durationLabel(callBackMinutes)))
                        add(stringResource(R.string.how_always_allowed))
                        add(stringResource(R.string.how_always_service))
                        add(stringResource(R.string.how_always_hidden))
                    }
                    for (line in always) Sentence(line, SwitchboardType.lead, color = colors.onSurface)
                }
            }
            Section {
                SectionHeading(stringResource(R.string.how_others_heading))
                StopsPlate(
                    listOf(
                        stringResource(R.string.mode_off) to stringResource(R.string.mode_off_detail),
                        stringResource(R.string.mode_silence) to stringResource(R.string.how_silence),
                        stringResource(R.string.mode_block) to stringResource(R.string.how_block),
                    ),
                )
                Sentence(stringResource(R.string.how_first_time), SwitchboardType.body, color = colors.onSurfaceVariant)
            }
            Section {
                SectionHeading(stringResource(R.string.how_pause_heading))
                Sentence(stringResource(R.string.how_pause), SwitchboardType.lead, color = colors.onSurface)
                Sentence(stringResource(R.string.how_emergency), SwitchboardType.lead, color = colors.onSurface)
            }
            Section {
                SectionHeading(stringResource(R.string.how_stopped_heading))
                Sentence(stringResource(R.string.how_stopped), SwitchboardType.lead, color = colors.onSurface)
            }
            Section {
                SectionHeading(stringResource(R.string.how_automatic_heading))
                Sentence(stringResource(R.string.how_automatic), SwitchboardType.lead, color = colors.onSurface)
            }
            MainKey(stringResource(R.string.done), onClick = onDone, modifier = Modifier.padding(top = 4.dp), tag = "how-done")
        }
    }
}

/** The lever's three stops on a plate like the lever's own: each engraved name, then what it does. */
@Composable
private fun StopsPlate(stops: List<Pair<String, String>>) {
    val colors = MaterialTheme.colorScheme
    Plate {
        stops.forEachIndexed { index, (name, sentence) ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (index > 0) {
                            Modifier.drawBehind { drawRect(colors.outlineVariant, Offset(16.dp.toPx(), 0f), Size(size.width - 16.dp.toPx(), 1.dp.toPx())) }
                        } else {
                            Modifier
                        },
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .semantics(mergeDescendants = true) { },
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                CapsText(name, SwitchboardType.lever, color = colors.onSurface, maxLines = 1)
                Sentence(sentence, SwitchboardType.lead, color = colors.onSurfaceVariant)
            }
        }
    }
}
