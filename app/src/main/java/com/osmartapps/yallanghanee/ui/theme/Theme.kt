package com.osmartapps.yallanghanee.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Night Majlis tokens. Contrast verified in docs/phase0/ui-ux-audit.md.
val NightBg = Color(0xFF120D0A)
val NightSurface = Color(0xFF1E1612)
val NightSurfaceHigh = Color(0xFF2A1F19)
val NightOutline = Color(0xFF7A6353)
val NightOutlineSoft = Color(0xFF4A3A30)
val NightText = Color(0xFFF7EAD7)
val NightTextMuted = Color(0xFFC9B8A3)
val NightGold = Color(0xFFD9A94F)
val NightGoldPressed = Color(0xFFB8862F)
val NightSuccess = Color(0xFF7FB18A)
val NightError = Color(0xFFE07A6A)

// Per-maqam accents from the content spec. Use as fills and sigils, not body text.
val MaqamRast = Color(0xFFE2A45C)
val MaqamBayati = Color(0xFF5BC8A6)
val MaqamHijaz = Color(0xFFC97B5A)
val MaqamSaba = Color(0xFF8E6BAE)
val MaqamSabaText = Color(0xFFB49AD4)
val MaqamKurd = Color(0xFF4FA3A5)
val MaqamNahawand = Color(0xFFC77E9B)
val MaqamAjam = Color(0xFFD4AF6A)
val MaqamSikah = Color(0xFF7FAE6B)
val MaqamHuzam = Color(0xFF5B7FBF)

fun maqamColor(id: String): Color = when (id) {
    "rast" -> MaqamRast
    "bayati" -> MaqamBayati
    "hijaz" -> MaqamHijaz
    "saba" -> MaqamSaba
    "kurd" -> MaqamKurd
    "nahawand" -> MaqamNahawand
    "ajam" -> MaqamAjam
    "sikah" -> MaqamSikah
    "huzam" -> MaqamHuzam
    else -> NightGold
}

private val NightColors = darkColorScheme(
    primary = NightGold,
    onPrimary = NightBg,
    secondary = NightGoldPressed,
    onSecondary = NightBg,
    background = NightBg,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = NightTextMuted,
    outline = NightOutline,
    outlineVariant = NightOutlineSoft,
    error = NightError,
    onError = NightBg,
)

private val NightShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
private fun nightTypography(): Typography {
    val assets = LocalContext.current.assets
    val ui = remember(assets) {
        FontFamily(
            Font("fonts/IBMPlexSansArabic-Regular.ttf", assets, FontWeight.Normal),
            Font("fonts/IBMPlexSansArabic-SemiBold.ttf", assets, FontWeight.SemiBold),
        )
    }
    val display = remember(assets) {
        FontFamily(
            Font("fonts/ArefRuqaa-Bold.ttf", assets, FontWeight.Bold),
        )
    }
    return remember(ui, display) {
        Typography(
            displayLarge = TextStyle(
                fontFamily = display,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                lineHeight = 46.sp,
            ),
            displayMedium = TextStyle(
                fontFamily = display,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 40.sp,
            ),
            displaySmall = TextStyle(
                fontFamily = display,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 36.sp,
            ),
            headlineLarge = TextStyle(
                fontFamily = ui,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                lineHeight = 38.sp,
            ),
            headlineMedium = TextStyle(
                fontFamily = ui,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 34.sp,
            ),
            headlineSmall = TextStyle(
                fontFamily = ui,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 32.sp,
            ),
            titleLarge = TextStyle(
                fontFamily = ui,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 30.sp,
            ),
            titleMedium = TextStyle(
                fontFamily = ui,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                lineHeight = 27.sp,
            ),
            titleSmall = TextStyle(
                fontFamily = ui,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                lineHeight = 23.sp,
            ),
            bodyLarge = TextStyle(
                fontFamily = ui,
                fontWeight = FontWeight.Normal,
                fontSize = 17.sp,
                lineHeight = 27.sp,
            ),
            bodyMedium = TextStyle(
                fontFamily = ui,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 25.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = ui,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 21.sp,
            ),
            labelLarge = TextStyle(
                fontFamily = ui,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                lineHeight = 23.sp,
            ),
            labelMedium = TextStyle(
                fontFamily = ui,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = 21.sp,
            ),
            labelSmall = TextStyle(
                fontFamily = ui,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            ),
        )
    }
}

@Composable
fun NightMajlisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NightColors,
        typography = nightTypography(),
        shapes = NightShapes,
        content = content,
    )
}
