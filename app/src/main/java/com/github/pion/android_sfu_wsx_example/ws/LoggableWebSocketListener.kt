package com.github.pion.android_sfu_wsx_example.ws

import com.github.pion.android_sfu_wsx_example.logging.logD
import com.github.pion.android_sfu_wsx_example.logging.logE
import com.github.pion.android_sfu_wsx_example.logging.logI
import com.github.pion.android_sfu_wsx_example.logging.logV
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

private const val TAG = "Loggable-WS"

abstract class LoggableWebSocketListener : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logD(TAG) { "[onOpen] response: $response" }
    }

    override fun onMessage(webSocket: WebSocket, raw: String) {
        logV(TAG) { "[onMessage] rawText: $raw" }
    }

    override fun onMessage(webSocket: WebSocket, raw: ByteString) {
        logV(TAG) { "[onMessage] rawBytes: $raw" }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        logI(TAG) { "[onClosing] code: $code, reason: $reason" }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        logI(TAG) { "[onClosed] code: $code, reason: $reason" }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logE(TAG, t) { "[onFailure] failure: $t, response: $response" }
    }
}