package com.cyanharborstudios.callblock.screening

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Locale

/**
 * The phone's own country as two letters ("IN"): the SIM's, else the network's, else the
 * locale's. None of these needs a permission.
 */
fun homeRegion(context: Context): String {
    val telephony = context.getSystemService(TelephonyManager::class.java)
    return listOf(telephony?.simCountryIso, telephony?.networkCountryIso, Locale.getDefault().country)
        .firstOrNull { !it.isNullOrBlank() }
        .orEmpty()
        .uppercase(Locale.ROOT)
}
