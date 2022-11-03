package com.github.pion.android_sfu_wsx_example.webrtc

import com.github.pion.android_sfu_wsx_example.logging.logD
import org.webrtc.RendererCommon

private const val TAG = "Loggable-RE"

class LoggableRendererEvents(val type: String) : RendererCommon.RendererEvents {
    override fun onFirstFrameRendered() {
        logD(TAG) { "[onFirstFrameRendered] #$type; no args" }
    }

    override fun onFrameResolutionChanged(width: Int, height: Int, rotation: Int) {
        logD(TAG) { "[onFrameResolutionChanged] #$type; surface($width, $height), rotation: $rotation" }
    }
}