package com.osmartapps.yallanghanee.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily

val BrandRed = Color(0xFF7F0400)
val BrandRedDeep = Color(0xFF4B0200)
val BrandGold = Color(0xFFE6A442)
val BrandGoldLight = Color(0xFFFFD08A)
val BrandCream = Color(0xFFFFF3E0)
val BrandError = Color(0xFFFF8A80)

private val YallaColors = darkColorScheme(
    primary = BrandGold,
    onPrimary = BrandRedDeep,
    secondary = BrandGoldLight,
    onSecondary = BrandRedDeep,
    background = BrandRedDeep,
    onBackground = BrandCream,
    surface = BrandRed,
    onSurface = BrandCream,
    error = BrandError,
)

@Composable
fun YallanghanniTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val arabicFont = remember(context) {
        runCatching {
            FontFamily(Typeface.createFromAsset(context.assets, "fonts/Kharabeesh-Font.ttf"))
        }.getOrDefault(FontFamily.Default)
    }
    val base = Typography()
    val typography = base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = arabicFont),
        displayMedium = base.displayMedium.copy(fontFamily = arabicFont),
        displaySmall = base.displaySmall.copy(fontFamily = arabicFont),
        headlineLarge = base.headlineLarge.copy(fontFamily = arabicFont),
        headlineMedium = base.headlineMedium.copy(fontFamily = arabicFont),
        headlineSmall = base.headlineSmall.copy(fontFamily = arabicFont),
        titleLarge = base.titleLarge.copy(fontFamily = arabicFont),
        titleMedium = base.titleMedium.copy(fontFamily = arabicFont),
        titleSmall = base.titleSmall.copy(fontFamily = arabicFont),
        labelLarge = base.labelLarge.copy(fontFamily = arabicFont),
        labelMedium = base.labelMedium.copy(fontFamily = arabicFont),
        labelSmall = base.labelSmall.copy(fontFamily = arabicFont),
    )
    MaterialTheme(colorScheme = YallaColors, typography = typography, content = content)
}
