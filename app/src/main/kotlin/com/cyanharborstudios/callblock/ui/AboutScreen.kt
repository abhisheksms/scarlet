package com.cyanharborstudios.callblock.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.ui.parts.Header
import com.cyanharborstudios.callblock.ui.parts.Strip
import com.cyanharborstudios.callblock.ui.parts.SwitchboardIcons
import com.cyanharborstudios.callblock.ui.parts.Trail

/**
 * The pages nobody opens twice, one level down from Settings: the privacy policy, the
 * open-source licences, and who to write to. The privacy row appears once its page exists.
 */
@Composable
fun AboutScreen(onBack: () -> Unit, onOpenLicences: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Box(Modifier.padding(horizontal = 16.dp)) { Header(stringResource(R.string.about), onBack) }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        ) {
            if (Links.PRIVACY_PAGE_LIVE) {
                Strip(stringResource(R.string.privacy_policy), icon = SwitchboardIcons.shield, trail = Trail.Out, onClick = { Links.open(context, Links.PRIVACY_POLICY) }, tag = "privacy-policy")
            }
            Strip(stringResource(R.string.licences), icon = SwitchboardIcons.document, trail = Trail.Chevron, onClick = onOpenLicences, tag = "licences")
            Strip(stringResource(R.string.contact), icon = SwitchboardIcons.mail, trail = Trail.Out, onClick = { Links.email(context) }, tag = "contact")
        }
    }
}
