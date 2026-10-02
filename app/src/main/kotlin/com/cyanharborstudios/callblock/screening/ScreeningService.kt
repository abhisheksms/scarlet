package com.cyanharborstudios.callblock.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.cyanharborstudios.callblock.BuildConfig
import com.cyanharborstudios.callblock.CallBlockApp
import com.cyanharborstudios.callblock.core.rules.Action
import kotlinx.coroutines.launch

/**
 * Android binds to this service for each incoming call from a number that is not in the
 * user's contacts, before the phone rings, and waits up to five seconds for an answer.
 *
 * The service holds no rules. It asks [CallScreener] for a decision, turns the decision
 * into the answer Android expects, and records what it did. If anything fails, the call
 * is allowed.
 */
class ScreeningService : CallScreeningService() {

    override fun onScreenCall(details: Call.Details) {
        if (details.callDirection != Call.Details.DIRECTION_INCOMING) return

        val container = (application as CallBlockApp).container
        val receivedAtMillis = System.currentTimeMillis()
        val rawNumber = details.handle?.schemeSpecificPart

        // The application's scope, not one tied to this service: Android unbinds the
        // service as soon as it has its answer, and the log entry must still be written.
        container.applicationScope.launch {
            val screened = container.callScreener.screenOrGiveUp(rawNumber, receivedAtMillis, DECISION_TIMEOUT_MILLIS)
            val action = screened?.decision?.action ?: Action.ALLOW
            if (BuildConfig.DEBUG) {
                // Debug builds only, and never the number: emulator verification reads this line.
                Log.i(TAG, "decision=$action rule=${screened?.decision?.ruleId}")
            }

            if (screened == null || action == Action.ALLOW) {
                respondToCall(details, responseFor(Action.ALLOW))
                return@launch
            }

            // Write the log entry first (a few milliseconds), so a handled call is never
            // missing from the history, then answer, then notify.
            val totalHandled = try {
                container.handledCallRecorder.store(screened)
            } catch (e: Exception) {
                Log.w(TAG, "Could not store the handled call", e)
                null
            }
            respondToCall(details, responseFor(action))
            if (totalHandled != null) {
                try {
                    container.handledCallRecorder.announce(screened, totalHandled)
                } catch (e: Exception) {
                    Log.w(TAG, "Could not post the notification", e)
                }
            }
        }
    }

    private fun responseFor(action: Action): CallResponse = when (action) {
        Action.ALLOW -> CallResponse.Builder().build()
        // The call still reaches the dialer, without a ringtone.
        Action.SILENCE -> CallResponse.Builder().setSilenceCall(true).build()
        // Rejected as if the user had declined. No missed-call notification: the app's own
        // optional notification says what happened. Android still logs the call as blocked.
        Action.BLOCK -> CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipNotification(true)
            .build()
    }

    private companion object {
        const val TAG = "ScreeningService"

        /** Android allows five seconds. Stop a second early and let the call ring. */
        const val DECISION_TIMEOUT_MILLIS = 4_000L
    }
}
