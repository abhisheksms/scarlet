package com.cyanharborstudios.callblock.notify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import java.text.NumberFormat
import com.cyanharborstudios.callblock.MainActivity
import com.cyanharborstudios.callblock.R
import com.cyanharborstudios.callblock.core.numbers.PhoneNumber
import com.cyanharborstudios.callblock.core.rules.Action
import com.cyanharborstudios.callblock.core.stats.Counts
import com.cyanharborstudios.callblock.core.stats.ReportFrequency

/**
 * The app's three notifications: a handled call, a reached milestone, a periodic report.
 * All three are quiet (no sound): an app whose job is fewer interruptions should not add one.
 */
class Notifier(private val context: Context) {

    private val manager = context.getSystemService(NotificationManager::class.java)

    /** False when the user has not allowed (or has switched off) this app's notifications. */
    fun canNotify(): Boolean = manager.areNotificationsEnabled()

    fun handledCall(number: PhoneNumber, action: Action, atMillis: Long) {
        if (!canNotify()) return
        ensureChannel(HANDLED_CALLS, R.string.channel_handled_calls)
        val title = context.getString(
            if (action == Action.SILENCE) R.string.notification_call_silenced else R.string.notification_call_blocked,
        )
        // One notification per call; the id only has to differ between calls.
        val id = (atMillis % Int.MAX_VALUE).toInt()
        // On a locked screen only the title shows: a caller's number is personal data, and so
        // is the action that names it.
        val withoutNumber = builder(HANDLED_CALLS, title, atMillis).build()
        val notification = builder(HANDLED_CALLS, title, atMillis)
            .setContentText(number.display.ifEmpty { context.getString(R.string.no_number) })
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .setPublicVersion(withoutNumber)
            .setContentIntent(openApp(MainActivity.OPEN_HISTORY))
            .apply {
                // The courier case without opening the app: let this number ring for an hour.
                if (number.key.isNotEmpty()) {
                    addAction(
                        Notification.Action.Builder(
                            Icon.createWithResource(context, R.drawable.ic_notification),
                            context.getString(R.string.notification_allow_hour),
                            AllowNumberReceiver.pendingIntent(context, number, id),
                        ).build(),
                    )
                }
            }
            .build()
        manager.notify(id, notification)
    }

    fun milestone(callsHandled: Int) {
        if (!canNotify()) return
        ensureChannel(MILESTONES, R.string.channel_milestones)
        val title = context.resources.getQuantityString(R.plurals.notification_milestone, callsHandled, count(callsHandled))
        val notification = builder(MILESTONES, title, System.currentTimeMillis())
            .setContentIntent(openApp(MainActivity.OPEN_STATISTICS))
            .build()
        manager.notify(MILESTONE_ID, notification)
    }

    fun report(frequency: ReportFrequency, counts: Counts) {
        if (!canNotify()) return
        ensureChannel(REPORTS, R.string.channel_reports)
        val title = context.getString(
            if (frequency == ReportFrequency.MONTHLY) R.string.notification_report_month else R.string.notification_report_week,
        )
        val text = context.getString(R.string.notification_report_counts, count(counts.blocked), count(counts.silenced))
        val notification = builder(REPORTS, title, System.currentTimeMillis())
            .setContentText(text)
            .setContentIntent(openApp(MainActivity.OPEN_STATISTICS))
            .build()
        manager.notify(REPORT_ID, notification)
    }

    /** Counts grouped the way the phone's language groups them: "3,412". */
    private fun count(n: Int): String = NumberFormat.getIntegerInstance(context.resources.configuration.locales[0]).format(n)

    private fun builder(channelId: String, title: String, atMillis: Long): Notification.Builder =
        Notification.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setWhen(atMillis)
            .setShowWhen(true)
            .setAutoCancel(true)

    private fun ensureChannel(id: String, nameRes: Int) {
        // Creating a channel that already exists does nothing, so this is safe to repeat.
        manager.createNotificationChannel(
            NotificationChannel(id, context.getString(nameRes), NotificationManager.IMPORTANCE_LOW),
        )
    }

    private fun openApp(destination: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_OPEN, destination)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            context,
            destination.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    companion object {
        const val HANDLED_CALLS = "handled_calls"
        const val MILESTONES = "milestones"
        const val REPORTS = "reports"
        private const val MILESTONE_ID = 1
        private const val REPORT_ID = 2
    }
}
