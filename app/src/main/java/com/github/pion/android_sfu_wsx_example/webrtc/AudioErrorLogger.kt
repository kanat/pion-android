package com.github.pion.android_sfu_wsx_example.webrtc

import android.util.Log
import org.webrtc.audio.JavaAudioDeviceModule

object AudioErrorLogger : JavaAudioDeviceModule.AudioRecordErrorCallback,
    JavaAudioDeviceModule.AudioTrackErrorCallback,
    JavaAudioDeviceModule.AudioRecordStateCallback,
    JavaAudioDeviceModule.AudioTrackStateCallback {

    private const val TAG = "Pion-AudioError"

    override fun onWebRtcAudioRecordInitError(p0: String?) {
        Log.w(TAG, "[onWebRtcAudioRecordInitError] $p0")
    }

    override fun onWebRtcAudioRecordStartError(
        p0: JavaAudioDeviceModule.AudioRecordStartErrorCode?,
        p1: String?
    ) {
        Log.w(TAG, "[onWebRtcAudioRecordInitError] $p1")
    }

    override fun onWebRtcAudioRecordError(p0: String?) {
        Log.w(TAG, "[onWebRtcAudioRecordError] $p0")
    }

    override fun onWebRtcAudioTrackInitError(p0: String?) {
        Log.w(TAG, "[onWebRtcAudioTrackInitError] $p0")
    }

    override fun onWebRtcAudioTrackStartError(
        p0: JavaAudioDeviceModule.AudioTrackStartErrorCode?,
        p1: String?
    ) {
        Log.w(TAG, "[onWebRtcAudioTrackStartError] $p0")
    }

    override fun onWebRtcAudioTrackError(p0: String?) {
        Log.w(TAG, "[onWebRtcAudioTrackError] $p0")
    }

    override fun onWebRtcAudioRecordStart() {
        Log.d(TAG, "[onWebRtcAudioRecordStart] no args")
    }

    override fun onWebRtcAudioRecordStop() {
        Log.d(TAG, "[onWebRtcAudioRecordStop] no args")
    }

    override fun onWebRtcAudioTrackStart() {
        Log.d(TAG, "[onWebRtcAudioTrackStart] no args")
    }

    override fun onWebRtcAudioTrackStop() {
        Log.d(TAG, "[onWebRtcAudioTrackStop] no args")
    }

}