package com.cyanharborstudios.callblock.ui

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.ui.parts.Header
import com.cyanharborstudios.callblock.ui.parts.Strip
import com.cyanharborstudios.callblock.ui.parts.Strips
import com.cyanharborstudios.callblock.ui.parts.Trail
import com.cyanharborstudios.callblock.ui.parts.ruleBelow
import com.cyanharborstudios.callblock.ui.parts.tieWidows
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType

/** An open-source work the app ships, who holds its copyright, and its licence. */
private data class Work(val name: String, val holder: String, val licence: Int)

// Keep in step with gradle/libs.versions.toml and res/font. Google's ads SDKs are not open source and are not listed.
private val WORKS = listOf(
    Work("Android Jetpack: Activity, Compose, Core, DataStore, Lifecycle, Navigation, Room, WorkManager", "The Android Open Source Project", R.string.licence_apache),
    Work("Material Components and Material Icons", "Google LLC", R.string.licence_apache),
    Work("Kotlin and kotlinx.coroutines", "JetBrains s.r.o. and Kotlin Programming Language contributors", R.string.licence_apache),
    Work("libphonenumber", "Google LLC", R.string.licence_apache),
    Work("Hanken Grotesk", "The Hanken Grotesk Project Authors", R.string.licence_ofl),
)

@Composable
fun LicencesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Box(Modifier.padding(horizontal = 16.dp)) { Header(stringResource(R.string.licences), onBack) }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column {
                for (work in WORKS) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .ruleBelow(colors.outlineVariant)
                            .padding(vertical = 10.dp)
                            .semantics(mergeDescendants = true) { },
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(tieWidows(work.name), style = SwitchboardType.licenceName, color = colors.onSurface)
                        Text(tieWidows("© ${work.holder}"), style = SwitchboardType.body, color = colors.onSurfaceVariant)
                        Text(stringResource(work.licence), style = SwitchboardType.body, color = colors.onSurfaceVariant)
                    }
                }
            }
            Strips {
                Strip(stringResource(R.string.licence_text_link), trail = Trail.Out, onClick = { Links.open(context, Links.APACHE_LICENSE) })
                Strip(stringResource(R.string.licence_ofl_link), trail = Trail.Out, onClick = { Links.open(context, Links.OPEN_FONT_LICENSE) })
            }
        }
    }
}
