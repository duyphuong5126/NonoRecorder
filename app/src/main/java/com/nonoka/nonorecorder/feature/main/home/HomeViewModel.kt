package com.nonoka.nonorecorder.feature.main.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class HomeViewModel : ViewModel() {
    var canDrawOverlay by mutableStateOf(false)
        private set

    var canRecordAudio by mutableStateOf(false)
        private set

    var hasAccessibilityPermission by mutableStateOf(false)
        private set

    var showAudioPermissionRationale by mutableStateOf(false)

    var showPostNotificationPermissionRationale by mutableStateOf(false)

    var showDrawOverlayPermissionRationale by mutableStateOf(false)

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