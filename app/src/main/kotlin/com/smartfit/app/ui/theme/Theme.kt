package com.smartfit.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ════════════════════════════════════════════════════════════════════
// COLOR PALETTE — Slate & Emerald / Professional Fitness
// ════════════════════════════════════════════════════════════════════

// Dark theme base (Slate)
val Slate950     = Color(0xFF0F172A)
val Slate900     = Color(0xFF1E293B)
val Slate800     = Color(0xFF334155)
val Black0       = Slate950
val Black1       = Slate900
val Black2       = Slate800
val Black3       = Color(0xFF020617)

// Emerald accent family
val Emerald50    = Color(0xFFECFDF5)
val Emerald100   = Color(0xFFD1FAE5)
val Emerald200   = Color(0xFFA7F3D0)
val Emerald400   = Color(0xFF34D399)
val Emerald500   = Color(0xFF10B981)  // PRIMARY
val Emerald600   = Color(0xFF059669)
val Emerald700   = Color(0xFF047857)
val Emerald800   = Color(0xFF065F46)
val Teal500      = Color(0xFF14B8A6)
val Cyan500      = Color(0xFF06B6D4)

// Real Orange colors
val Orange500    = Color(0xFFF97316)
val Orange600    = Color(0xFFEA580C)
val Orange700    = Color(0xFFC2410C)
val Orange400    = Color(0xFFFB923C)
val Orange100    = Color(0xFFFFEDD5)
val Orange200    = Color(0xFFFED7AA)

// Compatibility Aliases
val Coral500     = Teal500
val Purple500    = Color(0xFF8B5CF6) // Real purple
val Purple400    = Color(0xFFA78BFA)
val Purple600    = Color(0xFF7C3AED)
val Purple700    = Color(0xFF6D28D9)
val Purple800    = Color(0xFF5B21B6)

// Refined Light Mode Backgrounds
val Lavender0    = Color(0xFFF8FAFC) // Very light slate
val Lavender1    = Color(0xFFF1F5F9)
val White0       = Color(0xFFFFFFFF)
val OrangeCal    = Orange500

// Glass surfaces - Increased opacity for better visibility in Dark Mode
val GlassDark    = Color(0x26FFFFFF) // Increased from 0x18 (~15%)
val GlassDarkMd  = Color(0x1EFFFFFF) // Increased from 0x10 (~12%)
val GlassDarkBdr = Color(0x33FFFFFF) // Increased from 0x22 (~20%)
val GlassWarm    = Color(0x2EF97316) // Increased from 0x1E
val GlassPurBdr  = Color(0x4D8B5CF6) // Increased from 0x38
val GlassPurple  = Color(0x2E8B5CF6) // Increased from 0x1E
val GlassLight   = Color(0xD9FFFFFF)
val GlassLightBd = Color(0x15000000)
val GlassLightOr = Color(0x15F97316)
val GlassLightPu = Color(0x158B5CF6)

// Semantic colors
val GreenSuccess = Color(0xFF10B981)
val AmberWarn    = Color(0xFFF59E0B)
val RedError     = Color(0xFFEF4444)
val BlueInfo     = Color(0xFF3B82F6)

// Gradient sets
val GradCalorie  = listOf(Orange500, Orange600)
val GradSteps    = listOf(Color(0xFF06B6D4), Color(0xFF0891B2))
val GradActive   = listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
val GradFood     = listOf(Emerald500, Emerald600) // Changed to Emerald to distinguish from Activity
val GradWater    = listOf(Color(0xFF0EA5E9), Color(0xFF0284C7))
val GradNet      = listOf(Color(0xFFF59E0B), Color(0xFFD97706))

// ════════════════════════════════════════════════════════════════════
// TYPOGRAPHY
// ════════════════════════════════════════════════════════════════════
val AppTypography = Typography(
    displayLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Black,   fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.ExtraBold,fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,    fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,    fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium= TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,    fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold,fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge    = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold,fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold,fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall    = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,  fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge     = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,  fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium    = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,  fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall     = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,  fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge    = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold,fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium   = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,  fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,  fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)

// ════════════════════════════════════════════════════════════════════
// COLOR SCHEMES
// ════════════════════════════════════════════════════════════════════
val DarkColorScheme = darkColorScheme(
    primary            = Emerald500,
    onPrimary          = Color.White,
    primaryContainer   = Emerald700,
    onPrimaryContainer = Emerald100,
    secondary          = Teal500,
    onSecondary        = Color.Black,
    secondaryContainer = Color(0xFF134E4A),
    background         = Slate950,
    onBackground       = Color(0xFFF1F5F9),
    surface            = Slate900,
    onSurface          = Color(0xFFF1F5F9),
    surfaceVariant     = Slate800,
    onSurfaceVariant   = Color(0xFFCBD5E1),
    outline            = GlassDarkBdr,
    error              = RedError,
    onError            = Color.White,
    tertiary           = Cyan500,
    onTertiary         = Color.White,
)

val LightColorScheme = lightColorScheme(
    primary            = Emerald600,
    onPrimary          = Color.White,
    primaryContainer   = Emerald100,
    onPrimaryContainer = Emerald700,
    secondary          = Teal500,
    onSecondary        = Color.White,
    secondaryContainer = Emerald50,
    background         = Lavender0,
    onBackground       = Color(0xFF0F172A),
    surface            = White0,
    onSurface          = Color(0xFF0F172A),
    surfaceVariant     = Lavender1,
    onSurfaceVariant   = Emerald700,
    outline            = Color(0x1F000000),
    error              = RedError,
    onError            = Color.White,
    tertiary           = GreenSuccess,
    onTertiary         = Color.White,
)

@Composable
fun SmartFitTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
