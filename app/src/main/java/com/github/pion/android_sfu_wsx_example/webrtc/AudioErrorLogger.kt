package com.github.pion.android_sfu_wsx_example.webrtc

import com.github.pion.android_sfu_wsx_example.logging.logD
import com.github.pion.android_sfu_wsx_example.logging.logW
import org.webrtc.audio.JavaAudioDeviceModule

object AudioErrorLogger : JavaAudioDeviceModule.AudioRecordErrorCallback,
    JavaAudioDeviceModule.AudioTrackErrorCallback,
    JavaAudioDeviceModule.AudioRecordStateCallback,
    JavaAudioDeviceModule.AudioTrackStateCallback {

    private const val TAG = "Pion-AudioError"

    override fun onWebRtcAudioRecordInitError(p0: String?) {
        logW(TAG) { "[onWebRtcAudioRecordInitError] $p0" }
    }

    override fun onWebRtcAudioRecordStartError(
        p0: JavaAudioDeviceModule.AudioRecordStartErrorCode?,
        p1: String?
    ) {
        logW(TAG) { "[onWebRtcAudioRecordInitError] $p1" }
    }

    override fun onWebRtcAudioRecordError(p0: String?) {
        logW(TAG) { "[onWebRtcAudioRecordError] $p0" }
    }

    override fun onWebRtcAudioTrackInitError(p0: String?) {
        logW(TAG) { "[onWebRtcAudioTrackInitError] $p0" }
    }

    override fun onWebRtcAudioTrackStartError(
        p0: JavaAudioDeviceModule.AudioTrackStartErrorCode?,
        p1: String?
    ) {
        logW(TAG) { "[onWebRtcAudioTrackStartError] $p0" }
    }

    override fun onWebRtcAudioTrackError(p0: String?) {
        logW(TAG) { "[onWebRtcAudioTrackError] $p0" }
    }

    override fun onWebRtcAudioRecordStart() {
        logD(TAG) { "[onWebRtcAudioRecordStart] no args" }
    }

    override fun onWebRtcAudioRecordStop() {
        logD(TAG) { "[onWebRtcAudioRecordStop] no args" }
    }

    override fun onWebRtcAudioTrackStart() {
        logD(TAG) { "[onWebRtcAudioTrackStart] no args" }
    }

    override fun onWebRtcAudioTrackStop() {
        logD(TAG) { "[onWebRtcAudioTrackStop] no args" }
    }

}