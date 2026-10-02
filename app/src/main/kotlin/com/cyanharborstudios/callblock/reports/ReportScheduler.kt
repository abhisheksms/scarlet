package com.cyanharborstudios.callblock.reports

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cyanharborstudios.callblock.core.stats.ReportFrequency
import java.util.concurrent.TimeUnit

/** Keeps the daily report check scheduled while reports are on, and removes it when they are off. */
class ReportScheduler(private val context: Context) {

    fun sync(frequency: ReportFrequency) {
        val workManager = WorkManager.getInstance(context)
        if (frequency == ReportFrequency.OFF) {
            workManager.cancelUniqueWork(WORK_NAME)
        } else {
            // KEEP: if the check is already scheduled, leave its timing alone.
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<ReportWorker>(1, TimeUnit.DAYS).build(),
            )
        }
    }

    private companion object {
        const val WORK_NAME = "report-check"
    }
}
