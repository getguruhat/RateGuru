package com.getguruhat.rateguru.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Like a classic converter: charcoal backdrop, the icon's blue header, white cards and
 * sheets, raised keys in red (C), steel blue (refresh), GuruHat orange (calculator) and graphite.
 */
data class Palette(
    val backdropTop: Color,
    val backdropBottom: Color,
    val headerTop: Color,
    val headerBottom: Color,
    /** Text and icons drawn on the header and backdrop. */
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
    val clearKey: Color,
    val refreshKey: Color,
    /** The tall calculator key, and "=". */
    val calcKey: Color,
    val field: Color,
)

private val Charcoal = Palette(
    backdropTop = Color(0xFF232428),
    backdropBottom = Color(0xFF161719),
    headerTop = Color(0xFF0B63F3),
    headerBottom = Color(0xFF0039A6),
    onBackdrop = Color(0xFFFFFFFF),
    sheet = Color(0xFFF2F4F8),
    card = Color(0xFFFFFFFF),
    label = Color(0xFF0B1D38),
    secondary = Color(0xFF6E7B96),
    separator = Color(0xFFE6E9EF),
    accent = Color(0xFF0B63F3),
    onAccent = Color(0xFFFFFFFF),
    digitKey = Color(0xFF4B4E55),
    clearKey = Color(0xFFD9473C),
    refreshKey = Color(0xFF4A7DB8),
    calcKey = Color(0xFFF2802E),
    field = Color(0xFFE7EAF1),
)

val LocalPalette = staticCompositionLocalOf { Charcoal }

@Composable
fun RateGuruTheme(content: @Composable () -> Unit) {
    val p = Charcoal
    val scheme = lightColorScheme(primary = p.accent, onPrimary = p.onAccent, background = p.sheet, surface = p.card, surfaceContainerLow = p.card)
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
