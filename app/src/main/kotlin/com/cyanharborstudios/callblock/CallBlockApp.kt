package com.cyanharborstudios.callblock

import android.app.Application

class CallBlockApp : Application() {

    /** Created on first use. Nothing slow happens in onCreate: a call may be waiting. */
    val container: AppContainer by lazy { AppContainer(this) }
}
