package com.github.pion.android_sfu_wsx_example.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias WsEventType = String

@Serializable
data class WsEvent(
    @SerialName("event") val event: WsEventType,
    @SerialName("data") val data: String
) {
    companion object {
        const val CANDIDATE: WsEventType = "candidate"
        const val ANSWER: WsEventType = "answer"
        const val OFFER: WsEventType = "offer"
    }
}

@Serializable
data class WsIceCandidate(
    @SerialName("sdpMLineIndex") val sdpMLineIndex: Int,
    @SerialName("sdpMid") val sdpMid: String,
    @SerialName("candidate") val candidate: String,
    @SerialName("usernameFragment") val usernameFragment: String? = null
)

@Serializable
data class WsSessionDescription(
    @SerialName("type") val type: String,
    @SerialName("sdp") val sdp: String,
)
