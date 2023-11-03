package com.github.pion.android_sfu_wsx_example.webrtc

import io.getstream.webrtc.MediaConstraints
import io.getstream.webrtc.PeerConnection
import io.getstream.webrtc.SdpObserver
import io.getstream.webrtc.SessionDescription
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun PeerConnection.setRemoteDescription(sessionDescription: RtcSessionDescription): Result<Unit> {
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
): Result<RtcSessionDescription> {
    return suspendCoroutine { cont ->
        createAnswer(
            object : SimpleSdpObserver() {
                override fun onCreateSuccess(localSdp: SessionDescription?) {
                    cont.resume(
                        when (localSdp) {
                            null -> Result.failure(NullPointerException("[createAnswer] description is null"))
                            else -> Result.success(
                                RtcSessionDescription(
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

suspend fun PeerConnection.setLocalDescription(sessionDescription: RtcSessionDescription): Result<Unit> {
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