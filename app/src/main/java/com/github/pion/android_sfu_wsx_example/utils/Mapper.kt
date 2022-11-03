package com.github.pion.android_sfu_wsx_example.utils

import com.github.pion.android_sfu_wsx_example.webrtc.RtcSessionDescription
import com.github.pion.android_sfu_wsx_example.ws.WsIceCandidate
import com.github.pion.android_sfu_wsx_example.ws.WsSessionDescription
import org.webrtc.IceCandidate as RtcIceCandidate

fun RtcSessionDescription.toWS() = WsSessionDescription(
    type = type,
    sdp = sdp
)

fun WsSessionDescription.toRTC() = RtcSessionDescription(
    type = type,
    sdp = sdp
)

fun WsIceCandidate.toRTC() = RtcIceCandidate(
    sdpMid, sdpMLineIndex, candidate
)

fun RtcIceCandidate.toWS() = WsIceCandidate(
    sdpMid = sdpMid, sdpMLineIndex = sdpMLineIndex, candidate = sdp
)
