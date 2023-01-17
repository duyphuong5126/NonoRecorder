package com.nonoka.nonorecorder

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.nonoka.nonorecorder.constant.Colors.getColorScheme
import com.nonoka.nonorecorder.constant.brandTypography

@Composable
fun NonoTheme(
    context: Context,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = getColorScheme(nightMode = (context.applicationContext as App).nightModeSetting),
        typography = MaterialTheme.brandTypography(),
        content = content
    )
}