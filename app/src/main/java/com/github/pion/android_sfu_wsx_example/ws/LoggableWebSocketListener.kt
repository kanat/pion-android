package com.github.pion.android_sfu_wsx_example.ws

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

private const val TAG = "Loggable-WS"

abstract class LoggableWebSocketListener : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "[onOpen] response: $response")
    }

    override fun onMessage(webSocket: WebSocket, raw: String) {
        Log.v(TAG, "[onMessage] rawText: $raw")
    }

    override fun onMessage(webSocket: WebSocket, raw: ByteString) {
        Log.v(TAG, "[onMessage] rawBytes: $raw")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.i(TAG, "[onClosing] code: $code, reason: $reason")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.i(TAG, "[onClosed] code: $code, reason: $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "[onFailure] failure: $t, response: $response", t)
    }
}