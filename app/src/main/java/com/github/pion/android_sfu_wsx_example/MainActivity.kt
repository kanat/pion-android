package com.github.pion.android_sfu_wsx_example

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.github.pion.android_sfu_wsx_example.webrtc.AudioErrorLogger
import com.github.pion.android_sfu_wsx_example.webrtc.Consts
import com.github.pion.android_sfu_wsx_example.webrtc.InjectableLogger
import com.github.pion.android_sfu_wsx_example.webrtc.PeerConnectionObserver
import com.github.pion.android_sfu_wsx_example.webrtc.RTCSessionDescription
import com.github.pion.android_sfu_wsx_example.webrtc.createAnswer
import com.github.pion.android_sfu_wsx_example.webrtc.createVideoCapturer
import com.github.pion.android_sfu_wsx_example.ws.LoggableWebSocketListener
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.EglBase
import org.webrtc.HardwareVideoEncoderFactory
import org.webrtc.IceCandidate
import org.webrtc.Logging
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RendererCommon.RendererEvents
import org.webrtc.SimulcastVideoEncoderFactory
import org.webrtc.SoftwareVideoEncoderFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoDecoderFactory
import org.webrtc.VideoEncoderFactory
import org.webrtc.audio.JavaAudioDeviceModule
import com.github.pion.android_sfu_wsx_example.webrtc.setRemoteDescription
import com.github.pion.android_sfu_wsx_example.webrtc.setLocalDescription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.webrtc.MediaStreamTrack

private const val TAG = "Main-View"

class MainActivity : AppCompatActivity() {

    private val localRendererLayout by lazy { findViewById<FrameLayout>(R.id.localRendererLayout) }
    private val remoteRendererLayout by lazy { findViewById<FrameLayout>(R.id.remoteRendererLayout) }

    private val eglBase: EglBase by lazy { EglBase.create() }
    private val videoDecoderFactory by lazy { buildVideoDecoderFactory(eglBase) }
    private val videoEncoderFactory by lazy { buildVideoEncoderFactory(eglBase) }
    private val peerConnectionFactory by lazy { buildPeerConnectionFactory(videoDecoderFactory, videoEncoderFactory) }
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
        val constraints = MediaConstraints().apply {
            optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
        }
        val peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, constraints, peerConnectionObserver)
            ?: error("no peerConnection")

        val localRenderer = SurfaceViewRenderer(applicationContext).apply {
            setMirror(true)
            setZOrderMediaOverlay(true)
            setEnableHardwareScaler(true)
        }
        localRenderer.init(eglBase.eglBaseContext, object : RendererEvents {
            override fun onFirstFrameRendered() {
                Log.d(TAG, "[onLocalFirstFrameRendered] no args")
            }
            override fun onFrameResolutionChanged(width: Int, height: Int, rotation: Int) {
                Log.d(TAG, "[onLocalFrameResolutionChanged] surface($width, $height)")
            }
        })
        val remoteRenderer = SurfaceViewRenderer(this).apply {
            setEnableHardwareScaler(true)
        }
        remoteRenderer.init(eglBase.eglBaseContext, object : RendererEvents {
            override fun onFirstFrameRendered() {
                Log.d(TAG, "[onRemoteFirstFrameRendered] no args")
            }
            override fun onFrameResolutionChanged(width: Int, height: Int, rotation: Int) {
                Log.d(TAG, "[onRemoteFrameResolutionChanged] surface($width, $height)")
            }
        })

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
        remoteRendererLayout.addView(remoteRenderer)

        peerConnectionObserver.onIceCandidate = {
            lifecycleScope.launch {
                mutex.withLock {
                    val candidate = JsonObject(
                        mapOf(
                            "event" to JsonPrimitive(value = "candidate"),
                            "data" to JsonPrimitive(
                                value = Json.encodeToString(
                                    JsonObject(
                                        mapOf(
                                            "sdpMLineIndex" to JsonPrimitive(it.sdpMLineIndex),
                                            "sdpMid" to JsonPrimitive(it.sdpMid),
                                            "candidate" to JsonPrimitive(it.sdp)
                                        )
                                    )
                                )
                            )
                        )
                    )
                    val candidateMsg = Json.encodeToString(candidate)
                    Log.i(TAG, "[sendIceCandidate] candidate: $candidateMsg")
                    webSocket.send(candidateMsg)
                }
            }
        }

        peerConnectionObserver.onAddTrack = { receiver, mediaStreams ->
            lifecycleScope.launch {
                Log.i(TAG, "[onAddTrack] mediaStreams.size: ${mediaStreams.size}")
                for (stream in mediaStreams) {

                    Log.v(TAG, "[onAddTrack] stream.id: ${stream.id}, videoTracks.size: ${stream.videoTracks.size}," +
                        ", audioTracks.size: ${stream.audioTracks.size}")
                    stream.videoTracks.forEach { videoTrack ->
                        Log.v(TAG, "[onAddTrack] vTrack.kind: ${videoTrack.kind()}, vTrack.id: ${videoTrack.id()}, vTrack.enabled: ${videoTrack.enabled()}")
                    }
                    val remoteVideoTrack = stream.videoTracks.firstOrNull() ?: error("no remote videoTrack")
                    remoteVideoTrack.addSink(remoteRenderer)
                }
            }
        }
        peerConnectionObserver.onRemoveStream = { mediaStream ->
            lifecycleScope.launch {

            }
        }

        val httpClient = OkHttpClient.Builder().build()
        val request = Request
            .Builder()
            .url("ws://192.168.1.94:8080/websocket")
            .build()
        webSocket = httpClient.newWebSocket(request, object : LoggableWebSocketListener() {
            override fun onMessage(webSocket: WebSocket, raw: String) {
                super.onMessage(webSocket, raw)
                lifecycleScope.launch {
                    mutex.withLock {
                        Log.v(TAG, "[onMessage] raw: $raw")
                        val msg = Json.decodeFromString<JsonObject>(raw)
                        when (msg["event"]?.jsonPrimitive?.content) {
                            "candidate" -> {
                                val data = msg["data"]?.jsonPrimitive?.content ?: error("no data")
                                Log.i(TAG, "[handleCandidate] data: $data")
                                val parsed = Json.decodeFromString<JsonObject>(data)
                                val candidate: String = parsed["candidate"]?.jsonPrimitive?.content ?: error("no candidate")
                                peerConnection.addIceCandidate(IceCandidate("", 0, candidate))
                            }
                            "offer" -> {
                                val data = msg["data"]?.jsonPrimitive?.content ?: error("no data")
                                Log.i(TAG, "[handleOffer] data: $data")
                                val offer = Json.decodeFromString<JsonObject>(data)
                                val type = offer["type"]?.jsonPrimitive?.content ?: error("no type")
                                val sdp = offer["sdp"]?.jsonPrimitive?.content ?: error("no sdp")
                                peerConnection.setRemoteDescription(RTCSessionDescription(type = type, sdp = sdp))
                                val answer = peerConnection.createAnswer(mediaConstraints = MediaConstraints().apply {
                                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                                }).getOrNull() ?: error("no answer created")
                                peerConnection.setLocalDescription(answer)


                                val reply = JsonObject(
                                    mapOf(
                                        "event" to JsonPrimitive(value = "answer"),
                                        "data" to JsonPrimitive(
                                            value = Json.encodeToString(
                                                JsonObject(
                                                    mapOf(
                                                        "type" to JsonPrimitive(answer.type),
                                                        "sdp" to JsonPrimitive(answer.sdp)
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                                val replyMsg = Json.encodeToString(reply)
                                Log.i(TAG, "[sendAnswer] answer: $replyMsg")
                                webSocket.send(replyMsg)
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
    videoDecoderFactory: VideoDecoderFactory,
    videoEncoderFactory: VideoEncoderFactory,
): PeerConnectionFactory {
    PeerConnectionFactory.initialize(
        PeerConnectionFactory.InitializationOptions.builder(applicationContext)
            //TODO .setInjectableLogger(InjectableLogger, Logging.Severity.LS_VERBOSE)
            .createInitializationOptions()
    )

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