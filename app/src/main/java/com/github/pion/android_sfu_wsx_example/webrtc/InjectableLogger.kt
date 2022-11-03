package com.github.pion.android_sfu_wsx_example.webrtc

import android.util.Log
import org.webrtc.Loggable
import org.webrtc.Logging

object InjectableLogger : Loggable {

    private const val TAG = "Pion-WebRTC"

    override fun onLogMessage(message: String, severity: Logging.Severity, label: String) {
        when (severity) {
            Logging.Severity.LS_VERBOSE -> Log.v(TAG, "[onLogMessage] #$label; message: $message")
            Logging.Severity.LS_INFO -> Log.i(TAG, "[onLogMessage] #$label; message: $message")
            Logging.Severity.LS_WARNING -> Log.i(TAG, "[onLogMessage] #$label; message: $message")
            Logging.Severity.LS_ERROR -> Log.i(TAG, "[onLogMessage] #$label; message: $message")
            Logging.Severity.LS_NONE -> Log.d(TAG, "[onLogMessage] #$label; message: $message")
        }
    }
}