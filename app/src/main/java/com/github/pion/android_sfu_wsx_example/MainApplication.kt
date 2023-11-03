package com.github.pion.android_sfu_wsx_example

import android.app.Application
import com.github.pion.android_sfu_wsx_example.webrtc.InjectableLogger
import io.getstream.webrtc.Logging
import io.getstream.webrtc.PeerConnectionFactory

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .setInjectableLogger(InjectableLogger, Logging.Severity.LS_VERBOSE)
                .createInitializationOptions()
        )
    }
}