package com.github.pion.android_sfu_wsx_example.webrtc

import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun PeerConnection.setRemoteDescription(sessionDescription: RTCSessionDescription): Result<Unit> {
    return suspendCoroutine { cont ->
        setRemoteDescription(
            object : SdpObserver {
                override fun onCreateSuccess(localSdp: SessionDescription?) {
                    /* no-op */
                }

                override fun onSetSuccess() {
                    cont.resume(Result.success(Unit))
                }

                override fun onCreateFailure(error: String?) {
                    /* no-op */
                }

                override fun onSetFailure(error: String?) {
                    cont.resume(Result.failure(IllegalStateException(error)))
                }

            }, SessionDescription(
                SessionDescription.Type.fromCanonicalForm(sessionDescription.type),
                sessionDescription.sdp
            )
        )
    }
}

suspend fun PeerConnection.createAnswer(
    mediaConstraints: MediaConstraints = MediaConstraints()
): Result<RTCSessionDescription> {
    return suspendCoroutine { cont ->
        createAnswer(
            object : SimpleSdpObserver() {
                override fun onCreateSuccess(localSdp: SessionDescription?) {
                    cont.resume(
                        when (localSdp) {
                            null -> Result.failure(NullPointerException("[createAnswer] description is null"))
                            else -> Result.success(
                                RTCSessionDescription(
                                    type = localSdp.type.canonicalForm(),
                                    sdp = localSdp.description
                                )
                            )
                        }
                    )
                }

                override fun onCreateFailure(error: String?) {
                    cont.resume(Result.failure(IllegalStateException(error)))
                }
            }, mediaConstraints
        )
    }
}

suspend fun PeerConnection.setLocalDescription(sessionDescription: RTCSessionDescription): Result<Unit> {
    return suspendCoroutine { cont ->
        setLocalDescription(
            object : SdpObserver {
                override fun onCreateSuccess(localSdp: SessionDescription?) {
                    /* no-op */
                }

                override fun onSetSuccess() {
                    cont.resume(Result.success(Unit))
                }

                override fun onCreateFailure(error: String?) {
                    /* no-op */
                }

                override fun onSetFailure(error: String?) {
                    cont.resume(Result.failure(IllegalStateException(error)))
                }

            }, SessionDescription(
                SessionDescription.Type.fromCanonicalForm(sessionDescription.type),
                sessionDescription.sdp
            )
        )
    }
}