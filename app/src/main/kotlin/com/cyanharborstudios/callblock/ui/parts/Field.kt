package com.cyanharborstudios.callblock.ui.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.cyanharborstudios.callblock.ui.theme.SwitchboardType
import com.cyanharborstudios.callblock.ui.theme.switchboard

private val FieldShape = RoundedCornerShape(4.dp)

/** A recessed display you can type a phone number into. An error puts a ring around it. */
@Composable
fun RecessedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    spoken: String,
    modifier: Modifier = Modifier,
    error: Boolean = false,
    errorText: String? = null,
    tag: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val extra = switchboard
    val style = SwitchboardType.number.copy(color = colors.inverseOnSurface, letterSpacing = 0.02.em)
    val highlight = colors.surfaceContainerHighest
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = style,
        singleLine = true,
        cursorBrush = SolidColor(colors.inverseOnSurface),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = modifier
            .then(if (tag != null) Modifier.testTag(tag) else Modifier)
            .fillMaxWidth()
            .semantics {
                contentDescription = spoken
                if (error && errorText != null) this.error(errorText)
            },
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .drawBehind { drawRect(highlight, Offset(0f, size.height - 1.dp.toPx()), Size(size.width, 1.dp.toPx())) }
                    .background(colors.inverseSurface, FieldShape)
                    .then(if (error) Modifier.border(2.dp, colors.onSurface, FieldShape) else Modifier)
                    .drawBehind { drawRect(Color.Black, Offset(2.dp.toPx(), 0f), Size(size.width - 4.dp.toPx(), 2.dp.toPx())) }
                    .defaultMinSize(minHeight = 56.dp)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(placeholder, style = SwitchboardType.number.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.W400), color = extra.inverseOnSurfaceVariant)
                }
                inner()
            }
        },
    )
}

/** The line under a field that refused what was typed. */
@Composable
fun ErrorLine(text: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(10.dp).background(colors.onSurface))
        Text(text, style = SwitchboardType.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.W500), color = colors.onSurface)
    }
}
