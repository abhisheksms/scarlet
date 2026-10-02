package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cyanharborstudios.callblock.R

/** An open-source library the app ships, and who holds its copyright. All are Apache 2.0. */
private data class Library(val name: String, val holder: String)

// Keep in step with gradle/libs.versions.toml. Google's ads SDKs are not open source and are not listed.
private val LIBRARIES = listOf(
    Library("Android Jetpack: Activity, Compose, Core, DataStore, Lifecycle, Navigation, Room, WorkManager", "The Android Open Source Project"),
    Library("Material Components and Material Icons", "Google LLC"),
    Library("Kotlin and kotlinx.coroutines", "JetBrains s.r.o. and Kotlin Programming Language contributors"),
    Library("libphonenumber", "Google LLC"),
)

@Composable
fun LicencesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    AppScreen(title = stringResource(R.string.licences), onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.licences_intro))
            for (library in LIBRARIES) {
                Column {
                    Text(library.name, style = MaterialTheme.typography.titleSmall)
                    Text("© ${library.holder}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.licence_apache), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            TextButton(onClick = { Links.open(context, Links.APACHE_LICENSE) }) {
                Text(stringResource(R.string.licence_text_link))
            }
        }
    }
}
