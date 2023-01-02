package com.nonoka.nonorecorder.constant

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nonoka.nonorecorder.R

val exo2FontFamily = FontFamily(
    Font(R.font.exo2_bold, weight = FontWeight.Bold),
)

val mulishFontFamily = FontFamily(
    Font(R.font.montserrat_thin, weight = FontWeight.Thin),
    Font(R.font.montserrat_thinitalic, weight = FontWeight.Thin, style = FontStyle.Italic),
    Font(R.font.mulish_extralight, weight = FontWeight.ExtraLight),
    Font(
        R.font.mulish_extraightitalic,
        weight = FontWeight.ExtraLight,
        style = FontStyle.Italic
    ),
    Font(R.font.mulish_light, weight = FontWeight.Light),
    Font(R.font.mulish_lightitalic, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(R.font.mulish_regular, weight = FontWeight.Normal),
    Font(R.font.mulish_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(R.font.mulish_semibold, weight = FontWeight.SemiBold),
    Font(R.font.mulish_semibolditalic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
    Font(R.font.mulish_medium, weight = FontWeight.Medium),
    Font(R.font.mulish_mediumitalic, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(R.font.mulish_bold, weight = FontWeight.Bold),
    Font(R.font.mulish_bolditalic, weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(R.font.mulish_extrabold, weight = FontWeight.ExtraBold),
    Font(
        R.font.mulish_extrabolditalic,
        weight = FontWeight.ExtraBold,
        style = FontStyle.Italic
    ),
)

val Typography.titleAppBar: TextStyle
    get() = titleMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        fontFamily = exo2FontFamily,
    )

@Composable
fun MaterialTheme.brandTypography() = typography.copy(
    displayLarge = TextStyle(
        lineHeight = 64.sp,
        fontSize = 57.sp,
        letterSpacing = 0.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = mulishFontFamily,
    ),
    displayMedium = TextStyle(
        lineHeight = 52.sp,
        fontSize = 45.sp,
        letterSpacing = 0.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = mulishFontFamily,
    ),
    displaySmall = TextStyle(
        lineHeight = 44.sp,
        fontSize = 36.sp,
        letterSpacing = 0.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = mulishFontFamily,
    ),
    headlineLarge = TextStyle(
        lineHeight = 40.sp,
        fontSize = 32.sp,
        letterSpacing = 0.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = mulishFontFamily,
    ),
    headlineMedium = TextStyle(
        lineHeight = 36.sp,
        fontSize = 28.sp,
        letterSpacing = 0.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = mulishFontFamily,
    ),
    headlineSmall = TextStyle(
        lineHeight = 32.sp,
        fontSize = 24.sp,
        letterSpacing = 0.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = mulishFontFamily,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        lineHeight = 28.sp,
        fontSize = 22.sp,
        letterSpacing = 0.sp,
        fontFamily = mulishFontFamily,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp,
        fontSize = 16.sp,
        letterSpacing = 0.15.sp,
        fontFamily = mulishFontFamily,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp,
        fontFamily = mulishFontFamily,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp,
        fontFamily = mulishFontFamily,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp,
        fontFamily = mulishFontFamily,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp,
        fontFamily = mulishFontFamily,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp,
        fontFamily = mulishFontFamily,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp,
        fontFamily = mulishFontFamily,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        fontFamily = mulishFontFamily,
    ),
)