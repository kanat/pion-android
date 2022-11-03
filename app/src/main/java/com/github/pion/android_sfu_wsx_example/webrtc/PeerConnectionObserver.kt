package com.github.pion.android_sfu_wsx_example.webrtc

import android.util.Log
import org.webrtc.CandidatePairChangeEvent
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.IceCandidateErrorEvent
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver

private const val TAG = "Pion-ConnObserver"

class PeerConnectionObserver : PeerConnection.Observer {

    var onAddTrack: (RtpReceiver, Array<out MediaStream>) -> Unit = { _, _ -> }
    var onRemoveStream: (MediaStream) -> Unit = {}
    var onIceCandidate: (IceCandidate) -> Unit = {}

    override fun onSignalingChange(state: PeerConnection.SignalingState) {
        Log.d(TAG, "[onSignalingChange] state: $state")
    }

    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
        Log.d(TAG, "[onIceConnectionChange] state: $state")
    }

    override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
        Log.d(TAG, "[onStandardizedIceConnectionChange] newState: $newState")
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
        Log.d(TAG, "[onConnectionChange] newState: $newState")
    }

    override fun onIceConnectionReceivingChange(receiving: Boolean) {
        Log.d(TAG, "[onIceConnectionReceivingChange] receiving: $receiving")
    }

    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
        Log.d(TAG, "[onIceGatheringChange] state: $state")
    }

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        Log.d(TAG, "[iceCandidate] iceCandidate: $iceCandidate")
        onIceCandidate.invoke(iceCandidate)
    }

    override fun onIceCandidateError(event: IceCandidateErrorEvent?) {
        Log.d(TAG, "[onIceCandidateError] event: $event")
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<out IceCandidate>) {
        Log.d(TAG, "[onIceCandidatesRemoved] iceCandidates: $iceCandidates")
    }

    override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent?) {
        Log.d(TAG, "[onIceCandidatesRemoved] event: $event")
    }

    override fun onAddStream(stream: MediaStream) {
        Log.d(TAG, "[onAddStream] stream: $stream")
    }

    override fun onRemoveStream(stream: MediaStream) {
        Log.d(TAG, "[onRemoveStream] stream: $stream")
        onRemoveStream.invoke(stream)
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        Log.d(TAG, "[onDataChannel] dataChannel: $dataChannel")
    }

    override fun onRenegotiationNeeded() {
        Log.d(TAG, "[onRenegotiationNeeded] no args")
    }

    override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
        Log.d(TAG, "[onAddTrack] receiver: $receiver, mediaStreams: $mediaStreams")
        if (receiver != null && mediaStreams != null) {
            onAddTrack.invoke(receiver, mediaStreams)
        }
    }

    override fun onRemoveTrack(receiver: RtpReceiver?) {
        Log.d(TAG, "[onRemoveTrack] receiver: $receiver")
    }

    override fun onTrack(transceiver: RtpTransceiver?) {
        Log.d(TAG, "[onTrack] transceiver: $transceiver")
    }
}