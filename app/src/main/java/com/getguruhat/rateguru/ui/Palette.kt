package com.getguruhat.rateguru.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * GuruHat colours. Light mode puts the whole screen on the icon's orange gradient so the
 * white cards and keys stand out; dark mode is GuruHat navy with orange accents.
 */
data class Palette(
    /** Main screen backdrop, top to bottom. */
    val backdropTop: Color,
    val backdropBottom: Color,
    /** Text and icons drawn straight on the backdrop (title, status, tip). */
    val onBackdrop: Color,
    /** Bottom sheets (currency list, settings). */
    val sheet: Color,
    val card: Color,
    val label: Color,
    val secondary: Color,
    val separator: Color,
    val accent: Color,
    val onAccent: Color,
    val digitKey: Color,
    val functionKey: Color,
    val onFunctionKey: Color,
    /** The tall calculator key. */
    val calcKey: Color,
    val field: Color,
)

private val Navy = Color(0xFF0B1D38)

private val Light = Palette(
    backdropTop = Color(0xFFFF8B3D),
    backdropBottom = Color(0xFFF2600E),
    onBackdrop = Color(0xFFFFFFFF),
    sheet = Color(0xFFF2F4F8),
    card = Color(0xFFFFFFFF),
    label = Navy,
    secondary = Color(0xFF6E7B96),
    separator = Color(0xFFECEEF3),
    // Darker than the brand orange so text on white stays readable.
    accent = Color(0xFFE5580B),
    onAccent = Color(0xFFFFFFFF),
    digitKey = Color(0xFFFFFFFF),
    functionKey = Color(0x3DFFFFFF),
    onFunctionKey = Color(0xFFFFFFFF),
    calcKey = Navy,
    field = Color(0xFFE7EAF1),
)

private val Dark = Palette(
    backdropTop = Color(0xFF10264A),
    backdropBottom = Color(0xFF07122A),
    onBackdrop = Color(0xFFFFFFFF),
    sheet = Color(0xFF0B1D38),
    card = Color(0xFF16294D),
    label = Color(0xFFFFFFFF),
    secondary = Color(0xFF8A9BC2),
    separator = Color(0xFF233A66),
    accent = Color(0xFFFF7625),
    onAccent = Color(0xFFFFFFFF),
    digitKey = Color(0xFF1C3460),
    functionKey = Color(0xFF2A4677),
    onFunctionKey = Color(0xFFFFFFFF),
    calcKey = Color(0xFFFF7625),
    field = Color(0xFF1C3460),
)

val LocalPalette = staticCompositionLocalOf { Light }

@Composable
fun RateGuruTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val p = if (dark) Dark else Light
    val scheme = if (dark) {
        darkColorScheme(primary = p.accent, onPrimary = p.onAccent, background = p.sheet, surface = p.card, surfaceContainerLow = p.card)
    } else {
        lightColorScheme(primary = p.accent, onPrimary = p.onAccent, background = p.sheet, surface = p.card, surfaceContainerLow = p.card)
    }
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
