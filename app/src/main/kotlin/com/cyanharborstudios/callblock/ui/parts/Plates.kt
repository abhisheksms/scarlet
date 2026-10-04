package com.cyanharborstudios.callblock.ui.parts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.ui.exposeTestTags
import com.cyanharborstudios.callblock.ui.theme.LocalReducedMotion
import com.cyanharborstudios.callblock.ui.theme.Motion
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import com.cyanharborstudios.callblock.ui.theme.duration
import com.cyanharborstudios.callblock.ui.theme.switchboard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SheetShape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)

/**
 * A plate that rises from the bottom edge over a scrim, covering the ad slot too. It
 * leaves when the scrim is tapped, on back, or when [SheetScope.close] is called.
 */
@Composable
fun Sheet(label: String, onDismiss: () -> Unit, tag: String? = null, content: @Composable SheetScope.() -> Unit) {
    val reduced = LocalReducedMotion.current
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }
    var closing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val close: () -> Unit = {
        if (!closing) {
            closing = true
            if (reduced) {
                onDismiss()
            } else {
                scope.launch {
                    visible = false
                    delay(Motion.SHEET_FALL.toLong())
                    onDismiss()
                }
            }
        }
    }
    val sheetScope = remember { SheetScope(close) }
    Dialog(onDismissRequest = close, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        // A dialog is its own window: it needs the test tags exposed as the activity does.
        Box(Modifier.fillMaxSize().exposeTestTags()) {
            Scrim(visible, close)
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(tween(duration(Motion.SHEET_RISE, reduced), easing = Motion.emphasized)) { it },
                exit = slideOutVertically(tween(duration(Motion.SHEET_FALL, reduced), easing = Motion.accelerate)) { it },
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                val colors = MaterialTheme.colorScheme
                val highlight = colors.surfaceContainerHighest
                Column(
                    Modifier
                        .then(if (tag != null) Modifier.testTag(tag) else Modifier)
                        .semantics { paneTitle = label }
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 40.dp)
                        .background(colors.surfaceContainerHigh, SheetShape)
                        .drawBehind { drawRect(highlight, Offset(0f, 0f), Size(size.width, 1.dp.toPx())) }
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(Modifier.align(Alignment.CenterHorizontally).size(32.dp, 4.dp).background(colors.outline, RoundedCornerShape(2.dp)))
                    sheetScope.content()
                }
            }
        }
    }
}

/** Lets what is on a sheet close it, with its animation. */
class SheetScope(val close: () -> Unit)

/**
 * One use: Delete All. A plate over the scrim with a question and two keys of equal
 * weight. The first key is the way out.
 */
@Composable
fun ConfirmDialog(
    question: String,
    body: String,
    cancelLabel: String,
    confirmLabel: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmTag: String? = null,
) {
    val reduced = LocalReducedMotion.current
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }
    var closing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    fun leave(then: () -> Unit) {
        if (closing) return
        closing = true
        if (reduced) {
            then()
        } else {
            scope.launch {
                visible = false
                delay(Motion.SHEET_FALL.toLong())
                then()
            }
        }
    }
    Dialog(onDismissRequest = { leave(onCancel) }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(Modifier.fillMaxSize().exposeTestTags(), contentAlignment = Alignment.Center) {
            Scrim(visible) { leave(onCancel) }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(duration(Motion.SCRIM_FADE, reduced), easing = Motion.linear)),
                exit = fadeOut(tween(duration(Motion.SHEET_FALL, reduced), easing = Motion.linear)),
            ) {
                val colors = MaterialTheme.colorScheme
                Column(
                    Modifier
                        .padding(horizontal = 24.dp)
                        .semantics { paneTitle = question }
                        .drawBehind {
                            drawRoundRect(colors.outlineVariant, Offset(0f, 3.dp.toPx()), Size(size.width, size.height - 3.dp.toPx()), CornerRadius(6.dp.toPx()))
                        }
                        .padding(bottom = 3.dp)
                        .background(colors.surfaceContainerHigh, PlateShape)
                        .border(1.dp, colors.outlineVariant, PlateShape)
                        .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(question, style = SwitchboardType.display, color = colors.onSurface)
                    Text(tieWidows(body), style = SwitchboardType.body, color = colors.onSurface)
                    Spacer(Modifier.height(0.dp))
                    KeyStrip {
                        Key(cancelLabel, onClick = { leave(onCancel) }, onPlate = true)
                        Key(confirmLabel, onClick = { leave(onConfirm) }, onPlate = true, tag = confirmTag)
                    }
                }
            }
        }
    }
}

@Composable
private fun Scrim(visible: Boolean, onTap: () -> Unit) {
    val reduced = LocalReducedMotion.current
    val closeLabel = stringResource(R.string.close)
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(duration(Motion.SCRIM_FADE, reduced), easing = Motion.linear)),
        exit = fadeOut(tween(duration(Motion.SHEET_FALL, reduced), easing = Motion.linear)),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(switchboard.scrim)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onTap)
                .semantics { contentDescription = closeLabel },
        )
    }
}
