package com.github.pion.android_sfu_wsx_example.webrtc

import android.content.Context
import android.util.Log
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.VideoCapturer

private const val TAG = "Video-Capturer"

fun Context.createVideoCapturer(): VideoCapturer? {
    return if (Camera2Enumerator.isSupported(this)) {
        Log.d(TAG, "Creating capturer using camera2 API.")
        Camera2Enumerator(this).createCameraCapturer()
    } else {
        Log.d(TAG, "Creating capturer using camera1 API.")
        Camera1Enumerator(true).createCameraCapturer()
    }
}

private fun CameraEnumerator.createCameraCapturer(): CameraVideoCapturer? {
    // First, try to find front facing camera
    /*Log.d(TAG, "Looking for front facing cameras.")
    for (deviceName in deviceNames) {
        if (isFrontFacing(deviceName)) {
            Log.d(TAG, "Creating front facing camera capturer.")
            val videoCapturer = createCapturer(deviceName, null)
            if (videoCapturer != null) {
                return videoCapturer
            }
        }
    }*/

    // Front facing camera not found, try something else
    /*Log.d(TAG, "Looking for other cameras.")
    for (deviceName in deviceNames) {
        if (!isFrontFacing(deviceName)) {
            Log.d(TAG, "Creating other camera capturer.")
            val videoCapturer = createCapturer(deviceName, null)
            if (videoCapturer != null) {
                return videoCapturer
            }
        }
    }
    return null*/

    return deviceNames.filter { isFrontFacing(it) }.firstNotNullOfOrNull { createCapturer(it, null) }
        ?: deviceNames.filter { !isFrontFacing(it) }.firstNotNullOfOrNull { createCapturer(it, null) }

}