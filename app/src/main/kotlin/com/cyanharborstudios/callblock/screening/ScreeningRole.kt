package com.cyanharborstudios.callblock.screening

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent

/** The app can only screen calls while the user has chosen it as the phone's call-screening app. */
class ScreeningRole(context: Context) {

    private val roleManager: RoleManager? = context.getSystemService(RoleManager::class.java)

    /** False on a device that has no call-screening role at all (no telephony). */
    fun isAvailable(): Boolean = roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true

    fun isHeld(): Boolean = roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true

    /** The intent that shows Android's own "set as call screening app" prompt. */
    fun requestIntent(): Intent? = roleManager?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
}
