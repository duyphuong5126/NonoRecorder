package com.nonoka.nonorecorder

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainViewModel : ViewModel() {
    var canDrawOverlay by mutableStateOf(false)
        private set

    var canRecordAudio by mutableStateOf(false)
        private set

    var hasAccessibilityPermission by mutableStateOf(false)
        private set

    fun initPermission(
        canDrawOverlay: Boolean,
        canRecordAudio: Boolean,
        hasAccessibilityPermission: Boolean
    ) {
        this.canDrawOverlay = canDrawOverlay
        this.canRecordAudio = canRecordAudio
        this.hasAccessibilityPermission = hasAccessibilityPermission
    }

    fun drawOverlayPermissionStateChange(canDrawOverlay: Boolean) {
        this.canDrawOverlay = canDrawOverlay
    }

    fun recordAudioPermissionStateChange(canRecordAudio: Boolean) {
        this.canRecordAudio = canRecordAudio
    }

    fun accessibilityPermissionPermissionStateChange(hasAccessibilityPermission: Boolean) {
        this.hasAccessibilityPermission = hasAccessibilityPermission
    }


}