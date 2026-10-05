package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.BuildConfig
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.stats.ReportFrequency
import com.cyanharborstudios.callblock.ui.parts.CapsText
import com.cyanharborstudios.callblock.ui.parts.Header
import com.cyanharborstudios.callblock.ui.parts.KeyChoice
import com.cyanharborstudios.callblock.ui.parts.LatchingKeys
import com.cyanharborstudios.callblock.ui.parts.Section
import com.cyanharborstudios.callblock.ui.parts.Sentence
import com.cyanharborstudios.callblock.ui.parts.Strip
import com.cyanharborstudios.callblock.ui.parts.Strips
import com.cyanharborstudios.callblock.ui.parts.TallestOf
import com.cyanharborstudios.callblock.ui.parts.Trail
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType

/** How It Works, the summary notification, the way into About, and the build stamp. Calm and boring in the best way. */
@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onOpenHowItWorks: () -> Unit,
    onOpenAbout: () -> Unit,
    /** Opens Google's privacy-choices form, or null when it is not required for this user. */
    onOpenPrivacyChoices: (() -> Unit)?,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val access = rememberNotificationAccess(viewModel)
    val whenNotificationsAllowed = rememberNotificationRequest(viewModel)

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Box(Modifier.padding(horizontal = 16.dp)) { Header(stringResource(R.string.settings), onBack) }
        val current = settings ?: return
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Strip(stringResource(R.string.how_it_works), trail = Trail.Chevron, onClick = onOpenHowItWorks, tag = "how-it-works")

            Section(first = true) {
                CapsText(stringResource(R.string.report_heading), SwitchboardType.strip, color = colors.onSurface)
                if (access == NotificationAccess.BLOCKED) {
                    Strips {
                        Strip(
                            title = stringResource(R.string.notifications),
                            detail = stringResource(R.string.notifications_blocked),
                            trail = Trail.Out,
                            onClick = { openNotificationSettings(context) },
                            spoken = "${stringResource(R.string.notifications_blocked)}. ${stringResource(R.string.open_settings)}",
                            tag = "notifications",
                        )
                    }
                } else {
                    val heading = stringResource(R.string.report_heading)
                    LatchingKeys(
                        choices = listOf(
                            KeyChoice(ReportFrequency.OFF, stringResource(R.string.report_off)),
                            KeyChoice(ReportFrequency.WEEKLY, stringResource(R.string.report_weekly)),
                            KeyChoice(ReportFrequency.MONTHLY, stringResource(R.string.report_monthly)),
                        ),
                        selected = current.reportFrequency,
                        onSelect = { frequency ->
                            if (frequency == ReportFrequency.OFF) {
                                viewModel.setReportFrequency(frequency)
                            } else {
                                whenNotificationsAllowed { viewModel.setReportFrequency(frequency) }
                            }
                        },
                        modifier = Modifier.semantics { contentDescription = heading },
                        tag = { "report-${it.name}" },
                    )
                    // What the chosen key will do, in a space as tall as the longest of the three, so the rows below hold still.
                    val details = mapOf(
                        ReportFrequency.OFF to stringResource(R.string.report_off_detail),
                        ReportFrequency.WEEKLY to stringResource(R.string.report_weekly_detail),
                        ReportFrequency.MONTHLY to stringResource(R.string.report_monthly_detail),
                    )
                    TallestOf(
                        candidates = details.values.map { text -> { Sentence(text, SwitchboardType.body) } },
                        fillHeight = false,
                    ) {
                        Sentence(details.getValue(current.reportFrequency), SwitchboardType.body, Modifier.testTag("report-detail"), color = colors.onSurfaceVariant)
                    }
                }
            }

            Strips {
                if (onOpenPrivacyChoices != null) {
                    Strip(stringResource(R.string.privacy_choices), trail = Trail.Chevron, onClick = onOpenPrivacyChoices, tag = "privacy-choices")
                }
                if (Links.STORE_PAGE_LIVE) {
                    Strip(stringResource(R.string.share_app), trail = Trail.Out, onClick = { Links.shareText(context, Links.STORE_PAGE) }, tag = "share-app")
                    Strip(stringResource(R.string.rate_app), trail = Trail.Out, onClick = { Links.openStorePage(context) }, tag = "rate-app")
                }
                Strip(stringResource(R.string.about), trail = Trail.Chevron, onClick = onOpenAbout, tag = "about")
            }

            // Selectable, so it can be copied into a bug report.
            val spokenStamp = stringResource(R.string.build_stamp_spoken, BuildConfig.VERSION_NAME, BuildConfig.BUILD_DATE)
            SelectionContainer {
                Text(
                    stringResource(R.string.build_stamp, BuildConfig.VERSION_NAME, BuildConfig.BUILD_DATE),
                    style = SwitchboardType.note,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp).testTag("build-stamp").semantics { contentDescription = spokenStamp },
                )
            }
        }
    }
}
