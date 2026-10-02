package com.cyanharborstudios.callblock.screening

import com.cyanharborstudios.callblock.core.stats.Milestones
import com.cyanharborstudios.callblock.data.HandledCallDao
import com.cyanharborstudios.callblock.data.HandledCallEntity
import com.cyanharborstudios.callblock.data.SettingsStore
import com.cyanharborstudios.callblock.notify.Notifier

/** Writes a blocked or silenced call to the app's log and tells the user, if they asked to be told. */
class HandledCallRecorder(
    private val handledCalls: HandledCallDao,
    private val settingsStore: SettingsStore,
    private val notifier: Notifier,
) {
    /** Stores the call. Returns how many handled calls the log now holds. */
    suspend fun store(call: ScreenedCall): Int {
        handledCalls.insert(
            HandledCallEntity(
                numberRaw = call.number.raw,
                numberKey = call.number.key,
                atMillis = call.receivedAtMillis,
                action = call.decision.action.name,
                ruleId = call.decision.ruleId,
            ),
        )
        return handledCalls.count()
    }

    /** Posts the per-call notification when it is switched on, and a milestone one when this call reached it. */
    suspend fun announce(call: ScreenedCall, totalHandled: Int) {
        val settings = settingsStore.current()
        if (settings.notifyHandledCalls) {
            notifier.handledCall(call.number, call.decision.action, call.receivedAtMillis)
        }
        val milestone = Milestones.crossed(before = totalHandled - 1, after = totalHandled)
        if (milestone != null && milestone > settings.highestMilestoneAnnounced) {
            settingsStore.setHighestMilestoneAnnounced(milestone)
            notifier.milestone(milestone)
        }
    }
}
