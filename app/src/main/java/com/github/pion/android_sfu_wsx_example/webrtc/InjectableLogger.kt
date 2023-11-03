package com.github.pion.android_sfu_wsx_example.webrtc

import com.github.pion.android_sfu_wsx_example.logging.logD
import com.github.pion.android_sfu_wsx_example.logging.logE
import com.github.pion.android_sfu_wsx_example.logging.logI
import com.github.pion.android_sfu_wsx_example.logging.logV
import com.github.pion.android_sfu_wsx_example.logging.logW
import io.getstream.webrtc.Loggable
import io.getstream.webrtc.Logging

object InjectableLogger : Loggable {

    private const val TAG = "Pion-WebRTC"

    private const val ENABLED = false

    override fun onLogMessage(message: String, severity: Logging.Severity, label: String) {
        if (!ENABLED) return
        when (severity) {
            Logging.Severity.LS_VERBOSE -> logV(TAG) { "[onLogMessage] #$label; message: $message" }
            Logging.Severity.LS_INFO -> logI(TAG) { "[onLogMessage] #$label; message: $message" }
            Logging.Severity.LS_WARNING -> logW(TAG) { "[onLogMessage] #$label; message: $message" }
            Logging.Severity.LS_ERROR -> logE(TAG) { "[onLogMessage] #$label; message: $message" }
            Logging.Severity.LS_NONE -> logD(TAG) { "[onLogMessage] #$label; message: $message" }
        }
    }
}