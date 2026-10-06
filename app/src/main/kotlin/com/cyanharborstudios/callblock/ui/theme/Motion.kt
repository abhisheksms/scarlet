package com.cyanharborstudios.callblock.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/*
 * Motion, as the design spec names it (design/prototype/SPEC.md, "Motion"). Motion
 * communicates a change of state and nothing else; the mode changing is the one
 * moment that earns it. With reduced motion every duration is 0: handle, lamp and
 * sentence change in the same frame, sheets and dialogs appear and leave at once.
 */
object Motion {
    /** About how long the handle takes to reach a new stop. One haptic tick as it seats. */
    const val LEVER_TRAVEL = 180

    /**
     * How the handle moves to a stop: a spring with no bounce, which seats in about
     * [LEVER_TRAVEL]. A spring, not a fixed curve, because it starts from the speed the
     * handle already has: let go mid-drag, or sent somewhere else mid-way, the handle never
     * stops dead and starts again. The handle's position is counted in stops, so it has
     * arrived within a hundredth of a stop.
     */
    val leverTravel: AnimationSpec<Float> = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium, visibilityThreshold = 0.01f)

    /** The new lamp warms up, starting once the handle is halfway. */
    const val LAMP_WARM = 240
    const val LAMP_WARM_DELAY = 120

    /** The sentence in the display cross-fades. After a lever move it waits for the handle. */
    const val DISPLAY_SWAP = 120
    const val DISPLAY_SWAP_AFTER_LEVER = LEVER_TRAVEL

    /** On a device that cannot screen, the handle gives 7 dp towards Silence and goes back. */
    const val LEVER_LOCKED = 160

    /** A key's face drops 3 dp while pressed. */
    const val KEY_PRESS = 60

    /** The panel switch's handle crosses 20 dp. */
    const val SWITCH_SLIDE = 120

    /** A sheet rises from the bottom edge, and leaves faster. A dialog fades in and out with the scrim. */
    const val SHEET_RISE = 220
    const val SHEET_FALL = 160
    const val SCRIM_FADE = 160

    val emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val decelerate: Easing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
    val accelerate: Easing = CubicBezierEasing(0.3f, 0f, 1f, 1f)
    val linear: Easing = LinearEasing
}

/**
 * True when the phone's animations are switched off (the "Remove animations"
 * accessibility setting, or a developer setting). Read again whenever the app returns
 * to the front, because it is changed in system settings, not here.
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    fun read() = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    var reduced by remember { mutableStateOf(read()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { reduced = read() }
    return reduced
}

/** A duration, or 0 with reduced motion. */
fun duration(millis: Int, reduced: Boolean): Int = if (reduced) 0 else millis
