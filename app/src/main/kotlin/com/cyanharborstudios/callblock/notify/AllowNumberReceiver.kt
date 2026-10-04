package com.cyanharborstudios.callblock.notify

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cyanharborstudios.callblock.CallBlockApp
import com.cyanharborstudios.callblock.core.numbers.PhoneNumber
import com.cyanharborstudios.callblock.data.AllowedNumberEntity
import kotlinx.coroutines.launch

/**
 * The one action a stopped-call notification carries: let that number ring for an
 * hour, without opening the app. Not exported; only the app's own PendingIntent
 * reaches it. The number travels inside that intent and nowhere else.
 */
class AllowNumberReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val raw = intent.getStringExtra(EXTRA_NUMBER_RAW) ?: return
        val key = intent.getStringExtra(EXTRA_NUMBER_KEY)?.takeIf { it.isNotEmpty() } ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val container = (context.applicationContext as CallBlockApp).container
        val pending = goAsync()
        container.applicationScope.launch {
            try {
                val now = System.currentTimeMillis()
                container.allowedNumbers.upsert(
                    AllowedNumberEntity(numberKey = key, numberRaw = raw, addedAtMillis = now, expiresAtMillis = now + ONE_HOUR_MILLIS),
                )
                container.settingsStore.setAllowListEnabled(true)
                context.getSystemService(NotificationManager::class.java).cancel(notificationId)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val ACTION = "com.cyanharborstudios.callblock.ALLOW_FOR_AN_HOUR"
        private const val EXTRA_NUMBER_RAW = "number_raw"
        private const val EXTRA_NUMBER_KEY = "number_key"
        private const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val ONE_HOUR_MILLIS = 60 * 60_000L

        /** An explicit, immutable intent to this receiver for [number], one per notification. */
        fun pendingIntent(context: Context, number: PhoneNumber, notificationId: Int): PendingIntent {
            val intent = Intent(context, AllowNumberReceiver::class.java)
                .setAction(ACTION)
                .putExtra(EXTRA_NUMBER_RAW, number.raw)
                .putExtra(EXTRA_NUMBER_KEY, number.key)
                .putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            return PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}
