package com.github.pion.android_sfu_wsx_example.webrtc

import android.content.Context
import com.github.pion.android_sfu_wsx_example.logging.logD
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.VideoCapturer

private const val TAG = "Video-Capturer"

fun Context.createVideoCapturer(): VideoCapturer? {
    return if (Camera2Enumerator.isSupported(this)) {
        logD(TAG) { "[createVideoCapturer] use camera2 API" }
        Camera2Enumerator(this).createCameraCapturer()
    } else {
        logD(TAG) { "[createVideoCapturer] use camera1 API" }
        Camera1Enumerator(true).createCameraCapturer()
    }
}

private fun CameraEnumerator.createCameraCapturer(): CameraVideoCapturer? {
    return deviceNames.filter { isFrontFacing(it) }.firstNotNullOfOrNull { createCapturer(it, null) }
        ?: deviceNames.filter { !isFrontFacing(it) }.firstNotNullOfOrNull { createCapturer(it, null) }

}