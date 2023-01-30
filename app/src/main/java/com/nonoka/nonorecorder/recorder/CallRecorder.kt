package com.nonoka.nonorecorder.recorder

import android.content.Context

interface CallRecorder {
    fun startCallRecording(context: Context, audioSource: Int)
    fun stopCallRecording(context: Context)
    fun destroy()
    val isRecording: Boolean
}