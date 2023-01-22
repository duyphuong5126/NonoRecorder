package com.nonoka.nonorecorder

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.nonoka.nonorecorder.constant.Colors.getColorScheme
import com.nonoka.nonorecorder.constant.Colors.isInDarkTheme
import com.nonoka.nonorecorder.constant.brandTypography

@Composable
fun NonoTheme(
    context: Context,
    onThemeRendering: (Boolean) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val nightModeSetting = (context.applicationContext as App).nightModeSetting
    onThemeRendering(isInDarkTheme(nightModeSetting))
    MaterialTheme(
        colorScheme = getColorScheme(nightMode = nightModeSetting),
        typography = MaterialTheme.brandTypography(),
        content = content
    )
}