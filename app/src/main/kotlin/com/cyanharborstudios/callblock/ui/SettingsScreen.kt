package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyanharborstudios.callblock.BuildConfig
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.stats.ReportFrequency

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onOpenLicences: () -> Unit,
    /** Opens Google's privacy-choices form, or null when it is not required for this user. */
    onOpenPrivacyChoices: (() -> Unit)?,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val whenNotificationsAllowed = rememberNotificationGate(viewModel, snackbar)

    AppScreen(
        title = stringResource(R.string.settings),
        onBack = onBack,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        val current = settings ?: return@AppScreen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionHeading(stringResource(R.string.report_heading))
            Section(Modifier.selectableGroup()) {
                val choices = listOf(
                    ReportFrequency.OFF to R.string.report_off,
                    ReportFrequency.WEEKLY to R.string.report_weekly,
                    ReportFrequency.MONTHLY to R.string.report_monthly,
                )
                choices.forEachIndexed { index, (frequency, label) ->
                    if (index > 0) HorizontalDivider()
                    RadioRow(stringResource(label), current.reportFrequency == frequency, "report-${frequency.name}") {
                        if (frequency == ReportFrequency.OFF) {
                            viewModel.setReportFrequency(frequency)
                        } else {
                            whenNotificationsAllowed { viewModel.setReportFrequency(frequency) }
                        }
                    }
                }
            }
            Text(
                stringResource(R.string.report_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Section(Modifier.padding(top = 8.dp)) {
                LinkRow(stringResource(R.string.privacy_policy), null, "privacy-policy") { Links.open(context, Links.PRIVACY_POLICY) }
                if (onOpenPrivacyChoices != null) {
                    HorizontalDivider()
                    LinkRow(stringResource(R.string.privacy_choices), null, "privacy-choices", onOpenPrivacyChoices)
                }
                HorizontalDivider()
                LinkRow(stringResource(R.string.licences), null, "licences", onOpenLicences)
                HorizontalDivider()
                LinkRow(stringResource(R.string.contact), null, "contact") { Links.email(context) }
            }

            Section {
                LinkRow(stringResource(R.string.share_app), null, "share-app") { Links.shareText(context, Links.STORE_PAGE) }
                HorizontalDivider()
                LinkRow(stringResource(R.string.rate_app), null, "rate-app") { Links.openStorePage(context) }
            }

            // Selectable, so it can be copied into a bug report.
            SelectionContainer {
                Text(
                    stringResource(R.string.build_stamp, BuildConfig.VERSION_NAME, BuildConfig.BUILD_DATE),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(4.dp).testTag("build-stamp"),
                )
            }
        }
    }
}
