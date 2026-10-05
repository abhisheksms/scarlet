package com.cyanharborstudios.callblock.screening

import android.content.Context
import android.telephony.TelephonyManager

/**
 * After the user calls an emergency number, the app pauses itself for a day (core's
 * EmergencyPause says why and what that changes). Android shows a screening app that
 * outgoing call like any other, and says whether a number is an emergency number
 * without any permission.
 */
class EmergencyCallPause(
    private val isEmergencyNumber: (String) -> Boolean,
    private val pause: suspend (atMillis: Long) -> Unit,
) {
    /** Called for each outgoing call the app is shown. True when it was to an emergency number. */
    suspend fun onOutgoingCall(rawNumber: String?, dialledAtMillis: Long): Boolean {
        if (rawNumber.isNullOrBlank() || !isEmergencyNumber(rawNumber)) return false
        pause(dialledAtMillis)
        return true
    }
}

/**
 * Android's own answer for this phone: its SIMs, its network and its list of emergency
 * numbers. False when the phone cannot say (no telephony, or its process is not up).
 */
fun isEmergencyNumber(context: Context, number: String): Boolean =
    try {
        context.getSystemService(TelephonyManager::class.java)?.isEmergencyNumber(number) == true
    } catch (e: RuntimeException) {
        false
    }
