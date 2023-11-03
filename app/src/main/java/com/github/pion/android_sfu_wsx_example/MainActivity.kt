package com.github.pion.android_sfu_wsx_example

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.view.iterator
import androidx.lifecycle.lifecycleScope
import com.github.pion.android_sfu_wsx_example.logging.logI
import com.github.pion.android_sfu_wsx_example.logging.logV
import com.github.pion.android_sfu_wsx_example.logging.logW
import com.github.pion.android_sfu_wsx_example.utils.toRTC
import com.github.pion.android_sfu_wsx_example.utils.toWS
import com.github.pion.android_sfu_wsx_example.webrtc.AudioErrorLogger
import com.github.pion.android_sfu_wsx_example.webrtc.Consts
import com.github.pion.android_sfu_wsx_example.webrtc.InjectableLogger
import com.github.pion.android_sfu_wsx_example.webrtc.LoggableRendererEvents
import com.github.pion.android_sfu_wsx_example.webrtc.PeerConnectionObserver
import com.github.pion.android_sfu_wsx_example.webrtc.createAnswer
import com.github.pion.android_sfu_wsx_example.webrtc.createVideoCapturer
import com.github.pion.android_sfu_wsx_example.webrtc.setLocalDescription
import com.github.pion.android_sfu_wsx_example.webrtc.setRemoteDescription
import com.github.pion.android_sfu_wsx_example.ws.LoggableWebSocketListener
import com.github.pion.android_sfu_wsx_example.ws.WsEvent
import com.github.pion.android_sfu_wsx_example.ws.WsIceCandidate
import com.github.pion.android_sfu_wsx_example.ws.WsSessionDescription
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import io.getstream.webrtc.DefaultVideoDecoderFactory
import io.getstream.webrtc.EglBase
import io.getstream.webrtc.HardwareVideoEncoderFactory
import io.getstream.webrtc.Logging
import io.getstream.webrtc.MediaConstraints
import io.getstream.webrtc.MediaStream
import io.getstream.webrtc.PeerConnection
import io.getstream.webrtc.PeerConnectionFactory
import io.getstream.webrtc.SimulcastVideoEncoderFactory
import io.getstream.webrtc.SoftwareVideoEncoderFactory
import io.getstream.webrtc.SurfaceTextureHelper
import io.getstream.webrtc.SurfaceViewRenderer
import io.getstream.webrtc.VideoDecoderFactory
import io.getstream.webrtc.VideoEncoderFactory
import io.getstream.webrtc.audio.JavaAudioDeviceModule

private const val TAG = "Main-View"

class MainActivity : AppCompatActivity() {

    private val localRendererLayout by lazy { findViewById<ViewGroup>(R.id.localRendererLayout) }
    private val remoteRendererLayout by lazy { findViewById<ViewGroup>(R.id.remoteRendererLayout) }

    private val eglBase: EglBase by lazy { EglBase.create() }
    private val videoDecoderFactory by lazy { buildVideoDecoderFactory(eglBase) }
    private val videoEncoderFactory by lazy { buildVideoEncoderFactory(eglBase) }
    private val peerConnectionFactory by lazy { buildPeerConnectionFactory(videoEncoderFactory, videoDecoderFactory) }
    private val surfaceTextureHelper by lazy { SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext) }

    private val videoW by lazy { resources.getDimensionPixelSize(R.dimen.video_w) }
    private val videoH by lazy { resources.getDimensionPixelSize(R.dimen.video_h) }

    private lateinit var webSocket: WebSocket

    private val mutex = Mutex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rtcConfig = PeerConnection.RTCConfiguration(emptyList())
        val peerConnectionObserver = PeerConnectionObserver()
        val peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, peerConnectionObserver)
            ?: error("no peerConnection")

        val localRenderer = SurfaceViewRenderer(applicationContext).apply {
            setMirror(true)
            setZOrderMediaOverlay(true)
            setEnableHardwareScaler(true)
        }
        localRenderer.init(eglBase.eglBaseContext, LoggableRendererEvents(type = "local"))

        val manager = getSystemService<AudioManager>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            manager?.allowedCapturePolicy = AudioAttributes.ALLOW_CAPTURE_BY_ALL
        }

        val optionalAudioConstraints = listOf(
            MediaConstraints.KeyValuePair("googNoiseSuppression", Consts.TRUE),
            MediaConstraints.KeyValuePair("googEchoCancellation", Consts.TRUE),
            MediaConstraints.KeyValuePair("echoCancellation", Consts.TRUE),
            MediaConstraints.KeyValuePair("googEchoCancellation2", Consts.TRUE),
            MediaConstraints.KeyValuePair("googDAEchoCancellation", Consts.TRUE),
            MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true")
        )
        val audioConstraints = MediaConstraints().apply {
            optional.addAll(optionalAudioConstraints)
        }
        val audioSource = peerConnectionFactory.createAudioSource(audioConstraints)
        val audioTrack = peerConnectionFactory.createAudioTrack(Consts.AUDIO_TRACK_ID, audioSource).apply {
            setEnabled(true)
        }

        val videoCapturer = createVideoCapturer() ?: error("no video capturer")
        val videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast)
        videoCapturer.initialize(surfaceTextureHelper, this, videoSource.capturerObserver)
        videoCapturer.startCapture(videoW, videoH, 30)
        val videoTrack = peerConnectionFactory.createVideoTrack(Consts.VIDEO_TRACK_ID, videoSource).apply {
            setEnabled(true)
        }
        videoTrack.addSink(localRenderer)

        peerConnection.addTrack(audioTrack, Consts.MEDIA_STREAM_LABELS)
        peerConnection.addTrack(videoTrack, Consts.MEDIA_STREAM_LABELS)

        localRendererLayout.addView(localRenderer)

        peerConnectionObserver.onIceCandidate = { rtcCandidate ->
            lifecycleScope.launch {
                mutex.withLock {
                    val msgCandidate = Json.encodeToString(
                        WsEvent(
                            event = WsEvent.CANDIDATE,
                            data = Json.encodeToString(rtcCandidate.toWS())
                        )
                    )
                    logI(TAG) { "[sendIceCandidate] candidate: $msgCandidate" }
                    webSocket.send(msgCandidate)
                }
            }
        }

        peerConnectionObserver.onAddTrack = { _, mediaStreams ->
            lifecycleScope.launch {
                logStreams(methodName = "onAddTrack", mediaStreams)
                val mediaStream = mediaStreams.find { it.videoTracks.isNotEmpty() } ?: run {
                    logW(TAG) { "[onAddTrack] rejected (no mediaStreams with videoTracks found)" }
                    return@launch
                }
                val remoteVideoTrack = mediaStream.videoTracks?.firstOrNull() ?: error("no remote videoTrack")
                val remoteRenderer = SurfaceViewRenderer(applicationContext).apply {
                    tag = mediaStream.id
                    setEnableHardwareScaler(true)
                }
                remoteRenderer.init(eglBase.eglBaseContext, LoggableRendererEvents(type = "remote"))
                remoteVideoTrack.addSink(remoteRenderer)
                remoteRendererLayout.addView(remoteRenderer)
            }
        }
        peerConnectionObserver.onRemoveStream = { mediaStream ->
            val mediaStreamId = mediaStream.id
            lifecycleScope.launch {
                logI(TAG) { "[onRemoveStream] mediaStream.id: $mediaStreamId" }
                val iterator = remoteRendererLayout.iterator()
                while (iterator.hasNext()) {
                    val child = iterator.next() as? SurfaceViewRenderer
                    val childMediaStreamId = child?.tag as? String ?: ""
                    if (childMediaStreamId == mediaStreamId) {
                        logV(TAG) { "[onRemoveStream] found child: $child" }
                        child?.release()
                        iterator.remove()
                    }
                }
            }
        }

        val httpClient = OkHttpClient.Builder().build()
        val request = Request
            .Builder()
            .url("ws://192.168.1.73:8080/websocket")
            .build()
        webSocket = httpClient.newWebSocket(request, object : LoggableWebSocketListener() {
            override fun onMessage(webSocket: WebSocket, raw: String) {
                super.onMessage(webSocket, raw)
                lifecycleScope.launch {
                    mutex.withLock {
                        logV(TAG) { "[onMessage] raw: $raw" }
                        val msg = Json.decodeFromString<WsEvent>(raw)
                        when (msg.event) {
                            WsEvent.CANDIDATE -> {
                                logI(TAG) { "[handleCandidate] data: ${msg.data}" }
                                val wsIceCandidate = Json.decodeFromString<WsIceCandidate>(msg.data)
                                peerConnection.addIceCandidate(wsIceCandidate.toRTC())
                            }
                            WsEvent.OFFER -> {
                                logI(TAG) { "[handleOffer] data: ${msg.data}" }
                                val wsOffer = Json.decodeFromString<WsSessionDescription>(msg.data)
                                peerConnection.setRemoteDescription(wsOffer.toRTC())
                                val rtcAnswer = peerConnection.createAnswer(MediaConstraints().apply {
                                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                                }).getOrNull() ?: error("no answer created")
                                peerConnection.setLocalDescription(rtcAnswer)

                                val msgAnswer = Json.encodeToString(
                                    WsEvent(
                                        event = WsEvent.ANSWER,
                                        data = Json.encodeToString(rtcAnswer.toWS())
                                    )
                                )
                                logI(TAG) { "[sendAnswer] answer: $msgAnswer" }
                                webSocket.send(msgAnswer)
                            }
                        }
                    }
                }
            }
        })
    }

}

private fun buildVideoEncoderFactory(eglBase: EglBase): SimulcastVideoEncoderFactory {
    val hardwareEncoder = HardwareVideoEncoderFactory(eglBase.eglBaseContext, true, true)
    return SimulcastVideoEncoderFactory(hardwareEncoder, SoftwareVideoEncoderFactory())
}

private fun buildVideoDecoderFactory(eglBase: EglBase) = DefaultVideoDecoderFactory(
    eglBase.eglBaseContext
)

private fun Context.buildPeerConnectionFactory(
    videoEncoderFactory: VideoEncoderFactory,
    videoDecoderFactory: VideoDecoderFactory,
): PeerConnectionFactory {
    return PeerConnectionFactory.builder()
        .setVideoDecoderFactory(videoDecoderFactory)
        .setVideoEncoderFactory(videoEncoderFactory)
        .setAudioDeviceModule(
            JavaAudioDeviceModule
                .builder(applicationContext)
                .setUseHardwareAcousticEchoCanceler(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                .setUseHardwareNoiseSuppressor(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                .setAudioRecordErrorCallback(AudioErrorLogger)
                .setAudioTrackErrorCallback(AudioErrorLogger)
                .setAudioRecordStateCallback(AudioErrorLogger)
                .setAudioTrackStateCallback(AudioErrorLogger)
                .createAudioDeviceModule().also {
                    it.setMicrophoneMute(false)
                    it.setSpeakerMute(false)
                }
        )
        .createPeerConnectionFactory()
}

private fun logStreams(methodName: String, mediaStreams: Array<out MediaStream>) {
    logI(TAG) { "[$methodName] mediaStreams.size: ${mediaStreams.size}" }
    for (stream in mediaStreams) {
        logV(TAG) {
            "[$methodName] stream.id: ${stream.id}, videoTracks.size: ${stream.videoTracks.size}," +
                ", audioTracks.size: ${stream.audioTracks.size}"
        }
        stream.videoTracks.forEach { videoTrack ->
            logV(TAG) {
                "[$methodName] vTrack.kind: ${videoTrack.kind()}, vTrack.id: ${videoTrack.id()}," +
                    " vTrack.enabled: ${videoTrack.enabled()}"
            }
        }
    }
}