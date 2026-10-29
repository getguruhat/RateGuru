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
 * Colours taken from the app icon: its royal-blue gradient for the header and accent,
 * the arrow's gold for the calculator key, the ¥ coin's red for C. Light mode sits on a
 * pale blue-grey, dark mode on navy rather than black.
 */
data class Palette(
    val background: Color,
    val card: Color,
    val label: Color,
    val secondary: Color,
    val separator: Color,
    val accent: Color,
    val onAccent: Color,
    val digitKey: Color,
    val functionKey: Color,
    val onFunctionKey: Color,
    val field: Color,
    val headerTop: Color,
    val headerBottom: Color,
    val gold: Color,
    val danger: Color,
)

private val Light = Palette(
    background = Color(0xFFEEF3FB),
    card = Color(0xFFFFFFFF),
    label = Color(0xFF0A1530),
    secondary = Color(0xFF6E7B96),
    separator = Color(0xFFE3E9F4),
    accent = Color(0xFF0B63F3),
    onAccent = Color(0xFFFFFFFF),
    digitKey = Color(0xFFFFFFFF),
    functionKey = Color(0xFFDCE5F5),
    onFunctionKey = Color(0xFF0A1530),
    field = Color(0xFFE3EAF6),
    headerTop = Color(0xFF0B63F3),
    headerBottom = Color(0xFF0039A6),
    gold = Color(0xFFFF9F0A),
    danger = Color(0xFFE5342B),
)

private val Dark = Palette(
    background = Color(0xFF0A1530),
    card = Color(0xFF14244A),
    label = Color(0xFFFFFFFF),
    secondary = Color(0xFF8A9BC2),
    separator = Color(0xFF22355F),
    accent = Color(0xFF3B8BFF),
    onAccent = Color(0xFFFFFFFF),
    digitKey = Color(0xFF1C2F5C),
    functionKey = Color(0xFF2A3F6E),
    onFunctionKey = Color(0xFFFFFFFF),
    field = Color(0xFF1C2F5C),
    headerTop = Color(0xFF0B4FD0),
    headerBottom = Color(0xFF072A7A),
    gold = Color(0xFFFF9F0A),
    danger = Color(0xFFFF5A50),
)

val LocalPalette = staticCompositionLocalOf { Light }

@Composable
fun RateGuruTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val p = if (dark) Dark else Light
    val scheme = if (dark) {
        darkColorScheme(primary = p.accent, onPrimary = p.onAccent, background = p.background, surface = p.card, surfaceContainerLow = p.card)
    } else {
        lightColorScheme(primary = p.accent, onPrimary = p.onAccent, background = p.background, surface = p.card, surfaceContainerLow = p.card)
    }
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
