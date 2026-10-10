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
 *
 * Android also shows it each call the user makes to such a number. There is nothing to
 * answer then. The number is noted so that it can ring when it calls back, and a call
 * to an emergency number pauses the app for a day.
 */
class ScreeningService : CallScreeningService() {

    override fun onScreenCall(details: Call.Details) {
        val container = (application as CallBlockApp).container
        val receivedAtMillis = System.currentTimeMillis()
        val rawNumber = details.handle?.schemeSpecificPart

        if (details.callDirection != Call.Details.DIRECTION_INCOMING) {
            container.applicationScope.launch {
                // The pause first, and each step on its own: one failing must not cost the other.
                val emergency = try {
                    container.emergencyCallPause.onOutgoingCall(rawNumber, receivedAtMillis)
                } catch (e: Exception) {
                    Log.w(TAG, "Could not pause after an emergency call", e)
                    false
                }
                // An emergency number is not kept: nobody is called back from the number they dialled.
                if (!emergency) {
                    try {
                        container.dialledNumberRecorder.record(rawNumber, receivedAtMillis)
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not note the outgoing call", e)
                    }
                }
                // Debug builds only, and never the number: emulator verification reads this line.
                if (BuildConfig.DEBUG) Log.i(TAG, "outgoing call seen emergency=$emergency")
            }
            return
        }

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
                if (screened != null) noteAndWatch(container, screened)
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
            noteAndWatch(container, screened)
        }
    }

    /**
     * After the answer has gone: the call joins the record the frequent callers are found
     * in, and the record is looked at again. Neither step touches the call, and each is on
     * its own: one failing must not cost the other.
     */
    private suspend fun noteAndWatch(container: com.cyanharborstudios.callblock.AppContainer, screened: ScreenedCall) {
        try {
            container.seenCallRecorder.note(screened)
        } catch (e: Exception) {
            Log.w(TAG, "Could not note the call", e)
        }
        try {
            container.frequentCallerWatch.afterCall(screened.receivedAtMillis)
        } catch (e: Exception) {
            Log.w(TAG, "Could not look for frequent callers", e)
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
