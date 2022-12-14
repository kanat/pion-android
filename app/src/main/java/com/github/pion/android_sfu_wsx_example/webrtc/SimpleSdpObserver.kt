package com.github.pion.android_sfu_wsx_example.webrtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

abstract class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(localSdp: SessionDescription?) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(error: String?) {}
    override fun onSetFailure(error: String?) {}
}