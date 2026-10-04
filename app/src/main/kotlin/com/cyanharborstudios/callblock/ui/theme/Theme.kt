package com.cyanharborstudios.callblock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * The Switchboard palette, as the design spec gives it (design/prototype/SPEC.md,
 * "Colour, by Material 3 role"). A fixed palette of our own: nothing comes from the
 * wallpaper, so the app looks the same on every phone. The display window, the lever
 * slot and the field are "inverse" surfaces: dark in both themes.
 */

private val LightColors: ColorScheme = lightColorScheme(
    surface = Color(0xFFD2D6D1),
    onSurface = Color(0xFF16181A),
    background = Color(0xFFD2D6D1),
    onBackground = Color(0xFF16181A),
    surfaceContainerLowest = Color(0xFFC3C8C3),
    surfaceContainerLow = Color(0xFFC3C8C3),
    surfaceContainer = Color(0xFFE7EAE6),
    surfaceContainerHigh = Color(0xFFE7EAE6),
    surfaceContainerHighest = Color(0xFFF4F6F3),
    surfaceVariant = Color(0xFFE7EAE6),
    onSurfaceVariant = Color(0xFF3E4447),
    surfaceDim = Color(0xFFD2D6D1),
    surfaceBright = Color(0xFFD2D6D1),
    outline = Color(0xFF6B726E),
    outlineVariant = Color(0xFFA7ADA8),
    primary = Color(0xFF16181A),
    onPrimary = Color(0xFFF0EDE4),
    primaryContainer = Color(0xFFE7EAE6),
    onPrimaryContainer = Color(0xFF16181A),
    secondary = Color(0xFF16181A),
    onSecondary = Color(0xFFF0EDE4),
    secondaryContainer = Color(0xFFE7EAE6),
    onSecondaryContainer = Color(0xFF16181A),
    tertiary = Color(0xFF16181A),
    onTertiary = Color(0xFFF0EDE4),
    tertiaryContainer = Color(0xFFE7EAE6),
    onTertiaryContainer = Color(0xFF16181A),
    inverseSurface = Color(0xFF0E0F11),
    inverseOnSurface = Color(0xFFF0EDE4),
    inversePrimary = Color(0xFFF0EDE4),
    error = Color(0xFFFF8A5B),
    onError = Color(0xFF16181A),
    errorContainer = Color(0xFFE7EAE6),
    onErrorContainer = Color(0xFF16181A),
    scrim = Color(0xFF000000),
)

private val DarkColors: ColorScheme = darkColorScheme(
    surface = Color(0xFF1C1E21),
    onSurface = Color(0xFFECE9E0),
    background = Color(0xFF1C1E21),
    onBackground = Color(0xFFECE9E0),
    surfaceContainerLowest = Color(0xFF131416),
    surfaceContainerLow = Color(0xFF131416),
    surfaceContainer = Color(0xFF282B2F),
    surfaceContainerHigh = Color(0xFF282B2F),
    surfaceContainerHighest = Color(0xFF34383D),
    surfaceVariant = Color(0xFF282B2F),
    onSurfaceVariant = Color(0xFFA9ADB1),
    surfaceDim = Color(0xFF1C1E21),
    surfaceBright = Color(0xFF1C1E21),
    outline = Color(0xFF70757B),
    outlineVariant = Color(0xFF0E0F10),
    primary = Color(0xFFECE9E0),
    onPrimary = Color(0xFF16181A),
    primaryContainer = Color(0xFF282B2F),
    onPrimaryContainer = Color(0xFFECE9E0),
    secondary = Color(0xFFECE9E0),
    onSecondary = Color(0xFF16181A),
    secondaryContainer = Color(0xFF282B2F),
    onSecondaryContainer = Color(0xFFECE9E0),
    tertiary = Color(0xFFECE9E0),
    onTertiary = Color(0xFF16181A),
    tertiaryContainer = Color(0xFF282B2F),
    onTertiaryContainer = Color(0xFFECE9E0),
    inverseSurface = Color(0xFF0E0F11),
    inverseOnSurface = Color(0xFFF0EDE4),
    inversePrimary = Color(0xFF16181A),
    error = Color(0xFFFF8A5B),
    onError = Color(0xFF16181A),
    errorContainer = Color(0xFF282B2F),
    onErrorContainer = Color(0xFFECE9E0),
    scrim = Color(0xFF000000),
)

/** The colours the design needs beyond Material's roles (SPEC.md, "Outside Material 3"). */
data class SwitchboardColors(
    /** Secondary text in the display window. */
    val inverseOnSurfaceVariant: Color,
    /** Rules inside the display window; a drum's edge. */
    val inverseOutlineVariant: Color,
    /** One use: the square beside the role-missing sentence. */
    val attention: Color,
    /** A lamp that is not lit, and its highlight. */
    val lampGlass: Color,
    val lampGlassHigh: Color,
    /** The highlight of a lit lamp; its body is onSurface. */
    val lampOnHigh: Color,
    /** The 5 dp ring around a lit lamp. */
    val lampHalo: Color,
    /** The lever handle and the switch handle, their top edge, and the ridges on the lever handle. */
    val handle: Color,
    val handleHigh: Color,
    val handleRidge: Color,
    /** The 3 dp drop under the handle and the main key. */
    val handleDrop: Color,
    /** A counter drum, top and bottom of its gradient. */
    val drumHigh: Color,
    val drumLow: Color,
    /** The tint a row, strip or tile takes while pressed. */
    val press: Color,
    /** The scrim behind a sheet or a dialog. */
    val scrim: Color,
)

private val LightSwitchboard = SwitchboardColors(
    inverseOnSurfaceVariant = Color(0xFFA9ADB1),
    inverseOutlineVariant = Color(0xFF2C2F33),
    attention = Color(0xFFFF8A5B),
    lampGlass = Color(0xFF9AA19C),
    lampGlassHigh = Color(0xFFC9CEC9),
    lampOnHigh = Color(0xFF5B6368),
    lampHalo = Color(0x2916181A),
    handle = Color(0xFF1D2023),
    handleHigh = Color(0xFF42474B),
    handleRidge = Color(0xFF6A7075),
    handleDrop = Color(0x61000000),
    drumHigh = Color(0xFF1D2023),
    drumLow = Color(0xFF121315),
    press = Color(0x0F16181A),
    scrim = Color(0x80000000),
)

private val DarkSwitchboard = SwitchboardColors(
    inverseOnSurfaceVariant = Color(0xFFA9ADB1),
    inverseOutlineVariant = Color(0xFF2C2F33),
    attention = Color(0xFFFF8A5B),
    lampGlass = Color(0xFF3A3E44),
    lampGlassHigh = Color(0xFF5A5F66),
    lampOnHigh = Color(0xFFFFFFFF),
    lampHalo = Color(0x33ECE9E0),
    handle = Color(0xFFD9D5CB),
    handleHigh = Color(0xFFF6F3EA),
    handleRidge = Color(0xFF8C8A84),
    handleDrop = Color(0x99000000),
    drumHigh = Color(0xFF1D2023),
    drumLow = Color(0xFF121315),
    press = Color(0x12ECE9E0),
    scrim = Color(0x9E000000),
)

val LocalSwitchboardColors = staticCompositionLocalOf { LightSwitchboard }

/** True when the phone's animations are switched off; every duration is then 0. */
val LocalReducedMotion = compositionLocalOf { false }

/** The extra colours of the current theme. */
val switchboard: SwitchboardColors
    @Composable get() = LocalSwitchboardColors.current

@Composable
fun CallBlockTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    CompositionLocalProvider(
        LocalSwitchboardColors provides if (dark) DarkSwitchboard else LightSwitchboard,
        LocalReducedMotion provides rememberReducedMotion(),
    ) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = SwitchboardTypography,
            content = content,
        )
    }
}
