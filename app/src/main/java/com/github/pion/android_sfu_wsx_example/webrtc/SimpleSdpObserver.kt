package com.github.pion.android_sfu_wsx_example.webrtc

import io.getstream.webrtc.SdpObserver
import io.getstream.webrtc.SessionDescription

abstract class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(localSdp: SessionDescription?) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(error: String?) {}
    override fun onSetFailure(error: String?) {}
}