package com.nonoka.nonorecorder.recorder

import android.content.Context

interface CallRecorder {
    fun startCallRecording(context: Context)
    fun stopCallRecording(context: Context)
    fun destroy()
}