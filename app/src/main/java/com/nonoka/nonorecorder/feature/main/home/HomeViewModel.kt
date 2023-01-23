package com.nonoka.nonorecorder.feature.main.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class HomeViewModel : ViewModel() {
    var canDrawOverlay by mutableStateOf(false)

    var canRecordAudio by mutableStateOf(false)

    var hasAccessibilityPermission by mutableStateOf(false)

    var showAudioPermissionRationale by mutableStateOf(false)

    var showPostNotificationPermissionRationale by mutableStateOf(false)

    var showDrawOverlayPermissionRationale by mutableStateOf(false)
}