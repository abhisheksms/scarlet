package com.cyanharborstudios.callblock.reports

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cyanharborstudios.callblock.CallBlockApp
import com.cyanharborstudios.callblock.core.stats.ReportPlanner
import com.cyanharborstudios.callblock.core.stats.Statistics
import java.time.LocalDate
import java.time.ZoneId

/**
 * Runs about once a day. If a week or a month has finished since the last report, posts
 * one notification with that period's counts. A period with no handled calls is marked as
 * done without a notification: there is nothing to say.
 */
class ReportWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as CallBlockApp).container
        val settings = container.settingsStore.current()
        val zone = ZoneId.systemDefault()
        val period = ReportPlanner.dueReport(settings.reportFrequency, LocalDate.now(zone), settings.lastReportedPeriodKey)
            ?: return Result.success()

        val calls = container.handledCalls.all().map { it.toHandledCall() }
        val counts = Statistics.countBetween(calls, period.firstDay, period.lastDay, zone)
        container.settingsStore.setLastReportedPeriodKey(period.key)
        if (counts.total > 0) {
            container.notifier.report(settings.reportFrequency, counts)
        }
        return Result.success()
    }
}
