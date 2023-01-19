package com.nonoka.nonorecorder

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.nonoka.nonorecorder.theme.NightMode.Dark
import com.nonoka.nonorecorder.theme.NightMode.System

@Composable
fun isDarkTheme(): Boolean {
    val context = LocalContext.current
    val nightModeSetting = (context.applicationContext as App).nightModeSetting
    return nightModeSetting == Dark || (nightModeSetting == System && isSystemInDarkTheme())
}