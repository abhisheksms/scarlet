package com.cyanharborstudios.callblock.tile

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.text.format.DateFormat
import com.cyanharborstudios.callblock.CallBlockApp
import com.cyanharborstudios.callblock.MainActivity
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.time.DayRelation
import com.cyanharborstudios.callblock.core.time.TimeText
import com.cyanharborstudios.callblock.data.AppSettings
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The Quick Settings tile. Lit while calls are filtered; one tap pauses filtering for an
 * hour, the next tap resumes it. When nothing is being filtered (the lever at Off, the role
 * lost, a device that cannot screen) a tap opens the app instead. The system binds this
 * service while the tile is on screen; nothing here runs at any other time.
 */
class PauseTileService : TileService() {

    private val container get() = (application as CallBlockApp).container
    private var showing: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        showing?.cancel()
        showing = container.applicationScope.launch {
            container.settingsStore.settings.collect { show(it) }
        }
    }

    override fun onStopListening() {
        showing?.cancel()
        showing = null
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        container.applicationScope.launch {
            val now = System.currentTimeMillis()
            val face = face(container.settingsStore.current(), now)
            when (face.tap) {
                TileTap.PAUSE -> container.settingsStore.startTimer(Mode.OFF, PauseTile.pauseEnd(now))
                TileTap.RESUME -> container.settingsStore.endTimer()
                TileTap.OPEN_APP -> withContext(Dispatchers.Main) { openApp() }
            }
        }
    }

    private fun face(settings: AppSettings, now: Long): TileFace {
        val role = container.screeningRole
        return PauseTile.face(settings.screening, role.isAvailable(), role.isHeld(), now, ZoneId.systemDefault())
    }

    private suspend fun show(settings: AppSettings) = withContext(Dispatchers.Main) {
        val tile = qsTile ?: return@withContext
        val now = System.currentTimeMillis()
        val face = face(settings, now)
        val until = untilText(settings.screening.timerUntilMillis, now)
        // The tile is narrow: "Until 6:33 PM" fits where "Paused until 6:33 PM" is cut off.
        // TalkBack gets the full sentence.
        val shown = statusText(face.status, until, spoken = false)
        val spoken = statusText(face.status, until, spoken = true)
        tile.state = if (face.filtering) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.app_name)
        tile.icon = Icon.createWithResource(this@PauseTileService, R.drawable.ic_notification)
        tile.subtitle = shown
        tile.contentDescription = "${tile.label}, $spoken"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) tile.stateDescription = spoken
        tile.updateTile()
    }

    private fun statusText(status: TileStatus, until: String, spoken: Boolean): String = when (status) {
        TileStatus.CANNOT_SCREEN -> getString(R.string.tile_cannot_screen)
        TileStatus.OFF -> getString(R.string.mode_off)
        TileStatus.ROLE_MISSING -> getString(R.string.tile_not_screening)
        TileStatus.PAUSED -> getString(if (spoken) R.string.tile_paused_until_spoken else R.string.tile_paused_until, until)
        TileStatus.SILENCE -> getString(R.string.tile_silencing)
        TileStatus.BLOCK -> getString(R.string.tile_blocking)
    }

    /** The same words Home uses for when a pause ends, from the phone's own clock setting. */
    private fun untilText(atMillis: Long, now: Long): String {
        val timeText = TimeText(DateFormat.is24HourFormat(this), resources.configuration.locales[0], ZoneId.systemDefault())
        return when (timeText.dayRelation(atMillis, now)) {
            DayRelation.TODAY -> timeText.time(atMillis)
            DayRelation.TOMORROW -> getString(R.string.until_tomorrow, timeText.time(atMillis))
            DayRelation.OTHER -> timeText.dateAndTime(atMillis)
        }
    }

    // The Intent form is the only one that exists below Android 14; the branch above it is checked.
    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            startActivityAndCollapse(pending)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
