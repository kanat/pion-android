package com.github.pion.android_sfu_wsx_example.webrtc

import com.github.pion.android_sfu_wsx_example.logging.logD
import io.getstream.webrtc.CandidatePairChangeEvent
import io.getstream.webrtc.DataChannel
import io.getstream.webrtc.IceCandidate
import io.getstream.webrtc.IceCandidateErrorEvent
import io.getstream.webrtc.MediaStream
import io.getstream.webrtc.PeerConnection
import io.getstream.webrtc.RtpReceiver
import io.getstream.webrtc.RtpTransceiver

private const val TAG = "Pion-ConnObserver"

class PeerConnectionObserver : PeerConnection.Observer {

    var onAddTrack: (RtpReceiver, Array<out MediaStream>) -> Unit = { _, _ -> }
    var onRemoveStream: (MediaStream) -> Unit = {}
    var onIceCandidate: (IceCandidate) -> Unit = {}

    override fun onSignalingChange(state: PeerConnection.SignalingState) {
        logD(TAG) { "[onSignalingChange] state: $state" }
    }

    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
        logD(TAG) { "[onIceConnectionChange] state: $state" }
    }

    override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
        logD(TAG) { "[onStandardizedIceConnectionChange] newState: $newState" }
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
        logD(TAG) { "[onConnectionChange] newState: $newState" }
    }

    override fun onIceConnectionReceivingChange(receiving: Boolean) {
        logD(TAG) { "[onIceConnectionReceivingChange] receiving: $receiving" }
    }

    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
        logD(TAG) { "[onIceGatheringChange] state: $state" }
    }

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        logD(TAG) { "[iceCandidate] iceCandidate: $iceCandidate" }
        onIceCandidate.invoke(iceCandidate)
    }

    override fun onIceCandidateError(event: IceCandidateErrorEvent?) {
        logD(TAG) { "[onIceCandidateError] event: $event" }
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<out IceCandidate>) {
        logD(TAG) { "[onIceCandidatesRemoved] iceCandidates: $iceCandidates" }
    }

    override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent?) {
        logD(TAG) { "[onIceCandidatesRemoved] event: $event" }
    }

    override fun onAddStream(stream: MediaStream) {
        logD(TAG) { "[onAddStream] stream: $stream" }
    }

    override fun onRemoveStream(stream: MediaStream) {
        logD(TAG) { "[onRemoveStream] stream: $stream" }
        onRemoveStream.invoke(stream)
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        logD(TAG) { "[onDataChannel] dataChannel: $dataChannel" }
    }

    override fun onRenegotiationNeeded() {
        logD(TAG) { "[onRenegotiationNeeded] no args" }
    }

    override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
        logD(TAG) { "[onAddTrack] receiver: $receiver, mediaStreams: $mediaStreams" }
        if (receiver != null && mediaStreams != null) {
            onAddTrack.invoke(receiver, mediaStreams)
        }
    }

    override fun onRemoveTrack(receiver: RtpReceiver?) {
        logD(TAG) { "[onRemoveTrack] receiver: $receiver" }
    }

    override fun onTrack(transceiver: RtpTransceiver?) {
        logD(TAG) { "[onTrack] transceiver: $transceiver" }
    }
}