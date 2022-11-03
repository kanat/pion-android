package com.github.pion.android_sfu_wsx_example.logging

import android.util.Log

@PublishedApi
internal const val ENABLED = true

inline fun logV(tag: String, message: () -> String) {
    if (ENABLED) {
        Log.v(tag, message())
    }
}

inline fun logD(tag: String, message: () -> String) {
    if (ENABLED) {
        Log.d(tag, message())
    }
}

inline fun logI(tag: String, message: () -> String) {
    if (ENABLED) {
        Log.i(tag, message())
    }
}

inline fun logW(tag: String, message: () -> String) {
    if (ENABLED) {
        Log.w(tag, message())
    }
}

inline fun logE(tag: String, message: () -> String) {
    if (ENABLED) {
        Log.e(tag, message())
    }
}

inline fun logE(tag: String, t: Throwable, message: () -> String) {
    if (ENABLED) {
        Log.e(tag, message(), t)
    }
}