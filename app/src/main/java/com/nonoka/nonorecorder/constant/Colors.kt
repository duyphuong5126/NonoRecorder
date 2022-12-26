package com.nonoka.nonorecorder.constant

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Colors {
    val white = Color(0xFFFFFFFF)
    val grey500 = Color(0xFF9E9E9E)
    val grey900 = Color(0xFF212121)

    val transparency = Color(0x00000000)

    @Composable
    fun getColorScheme() =
        if (isSystemInDarkTheme()) getDarkColorScheme() else getLightColorScheme()

    @Composable
    private fun getLightColorScheme(): ColorScheme {
        return MaterialTheme.colorScheme.copy(
            primary = Color(0xFF266C94),
            onPrimary = Color(0xFFDDEBF3),
            primaryContainer = Color(0xFFDDEBF3),
            onPrimaryContainer = Color(0xFF0A3953),

            secondary = Color(0xFF756391),
            onSecondary = Color(0xFFDED4ED),
            secondaryContainer = Color(0xFFDED4ED),
            onSecondaryContainer = Color(0xFF473563),

            tertiary = Color(0xFF537951),
            onTertiary = Color(0xFFB8E6B5),
            tertiaryContainer = Color(0xFFB8E6B5),
            onTertiaryContainer = Color(0xFF325830),

            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF2B2B2B),

            surface = Color(0xFF266C94),
            onSurface = Color(0xFFFFFFFF),
        )
    }

    @Composable
    private fun getDarkColorScheme(): ColorScheme {
        return MaterialTheme.colorScheme.copy(
            primary = Color(0xFF266C94),
            onPrimary = Color(0xFFDDEBF3),
            primaryContainer = Color(0xFFDDEBF3),
            onPrimaryContainer = Color(0xFF0A3953),

            secondary = Color(0xFF756391),
            onSecondary = Color(0xFFDED4ED),
            secondaryContainer = Color(0xFFDED4ED),
            onSecondaryContainer = Color(0xFF473563),

            tertiary = Color(0xFF537951),
            onTertiary = Color(0xFFB8E6B5),
            tertiaryContainer = Color(0xFFB8E6B5),
            onTertiaryContainer = Color(0xFF325830),

            background = Color(0xFF2B2B2B),
            onBackground = Color(0xFFFFFFFF),

            surface = Color(0xFF266C94),
            onSurface = Color(0xFFFFFFFF),
        )
    }
}