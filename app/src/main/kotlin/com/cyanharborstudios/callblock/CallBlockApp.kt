package com.cyanharborstudios.callblock

import android.app.Application
import androidx.work.Configuration

class CallBlockApp : Application(), Configuration.Provider {

    /** Created on first use. Nothing slow happens in onCreate: a call may be waiting. */
    val container: AppContainer by lazy { AppContainer(this) }

    /**
     * WorkManager is set up the first time something asks for it, not at every process
     * start (its start-up initializer is removed in the manifest). An incoming call can
     * start this process, and that path should not pay for the report job.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()
}
