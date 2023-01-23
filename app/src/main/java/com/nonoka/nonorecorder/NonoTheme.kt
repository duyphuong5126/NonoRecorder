package com.nonoka.nonorecorder

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.nonoka.nonorecorder.constant.Colors.getColorScheme
import com.nonoka.nonorecorder.constant.Colors.isInDarkTheme
import com.nonoka.nonorecorder.constant.brandTypography

@Composable
fun NonoTheme(
    onThemeRendering: (Boolean) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val isInDarkTheme = isInDarkTheme()
    onThemeRendering(isInDarkTheme)
    MaterialTheme(
        colorScheme = getColorScheme(isInDarkTheme = isInDarkTheme),
        typography = MaterialTheme.brandTypography(),
        content = content
    )
}