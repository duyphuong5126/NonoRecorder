package com.nonoka.nonorecorder.constant

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.nonoka.nonorecorder.theme.NightMode

object Colors {
    val white = Color(0xFFFFFFFF)

    val transparency = Color(0x00000000)

    val successColor = Color(0xFF4CAF50)

    val switchUnselectedColor = Color(0xFFE0E0E0)

    @Composable
    fun isInDarkTheme(nightMode: NightMode): Boolean {
        return when (nightMode) {
            NightMode.Light -> false
            NightMode.Dark -> true
            else -> isSystemInDarkTheme()
        }
    }

    @Composable
    fun getColorScheme(nightMode: NightMode): ColorScheme {
        return when (nightMode) {
            NightMode.Light -> getLightColorScheme()
            NightMode.Dark -> getDarkColorScheme()
            else -> if (isSystemInDarkTheme()) getDarkColorScheme() else getLightColorScheme()
        }
    }

    @Composable
    private fun getLightColorScheme(): ColorScheme {
        return MaterialTheme.colorScheme.copy(
            primary = Color(0xFF537951),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFB8E6B5),
            onPrimaryContainer = Color(0xFF325830),

            secondary = Color(0xFF756391),
            onSecondary = Color(0xFFDED4ED),
            secondaryContainer = Color(0xFFDED4ED),
            onSecondaryContainer = Color(0xFF473563),

            tertiary = Color(0xFF266C94),
            onTertiary = Color(0xFFDDEBF3),
            tertiaryContainer = Color(0xFFDDEBF3),
            onTertiaryContainer = Color(0xFF0A3953),

            background = Color(0xFFEEEEEE),
            onBackground = Color(0xFF212121),

            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF212121),

            error = Color(0xFFB71C1C),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFCDD2),
            onErrorContainer = Color(0xFFD32F2F),

            outline = Color(0xFFE0E0E0)
        )
    }

    @Composable
    private fun getDarkColorScheme(): ColorScheme {
        return MaterialTheme.colorScheme.copy(
            primary = Color(0xFF66B362),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFB8E6B5),
            onPrimaryContainer = Color(0xFF325830),

            secondary = Color(0xFF756391),
            onSecondary = Color(0xFFDED4ED),
            secondaryContainer = Color(0xFFDED4ED),
            onSecondaryContainer = Color(0xFF473563),

            tertiary = Color(0xFF5ABDF7),
            onTertiary = Color(0xFFDDEBF3),
            tertiaryContainer = Color(0xFFDDEBF3),
            onTertiaryContainer = Color(0xFF1D5777),

            background = Color(0xFF000000),
            onBackground = Color(0xFFFFFFFF),

            surface = Color(0xFF212121),
            onSurface = Color(0xFFFFFFFF),

            error = Color(0xFFF55959),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFCDD2),
            onErrorContainer = Color(0xFFD32F2F),

            outline = Color(0xFF616161)
        )
    }
}