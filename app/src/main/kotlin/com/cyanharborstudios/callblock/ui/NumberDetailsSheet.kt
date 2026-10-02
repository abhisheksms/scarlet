package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.numbers.PhoneNumber
import com.cyanharborstudios.callblock.core.stats.NumberDetails
import com.cyanharborstudios.callblock.core.time.TimeText

/** Everything the app knows about one number, and the choice to let it through. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberDetailsSheet(
    number: PhoneNumber,
    details: NumberDetails,
    isAllowed: Boolean,
    timeText: TimeText,
    onAllow: () -> Unit,
    onRemoveAllowed: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, modifier = Modifier.exposeTestTags()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .testTag("number-details"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                number.display.ifEmpty { stringResource(R.string.no_number) },
                style = MaterialTheme.typography.headlineSmall,
            )
            DetailRow(stringResource(R.string.blocked), details.counts.blocked.toString())
            DetailRow(stringResource(R.string.silenced), details.counts.silenced.toString())
            DetailRow(stringResource(R.string.total), details.counts.total.toString())
            DetailRow(stringResource(R.string.first_handled), timeText.dateAndTime(details.firstHandledAtMillis))
            DetailRow(stringResource(R.string.last_handled), timeText.dateAndTime(details.lastHandledAtMillis))

            if (number.key.isNotEmpty()) {
                if (isAllowed) {
                    Text(stringResource(R.string.on_allow_list), color = MaterialTheme.colorScheme.primary)
                    OutlinedButton(onClick = onRemoveAllowed) { Text(stringResource(R.string.remove_from_allow_list)) }
                } else {
                    Button(onClick = onAllow, modifier = Modifier.testTag("allow-this-number")) {
                        Text(stringResource(R.string.allow_this_number))
                    }
                }
            }
        }
    }
}

/** A label on the left, its value on the right, read by TalkBack as one phrase. */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = "$label: $value" },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
