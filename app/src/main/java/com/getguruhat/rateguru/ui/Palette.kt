package com.getguruhat.rateguru.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** iOS-style system colours: grouped grey background, white cards, one blue accent (the icon's blue). */
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
)

private val Light = Palette(
    background = Color(0xFFF2F2F7),
    card = Color(0xFFFFFFFF),
    label = Color(0xFF000000),
    secondary = Color(0xFF8E8E93),
    separator = Color(0xFFE5E5EA),
    accent = Color(0xFF007AFF),
    onAccent = Color(0xFFFFFFFF),
    digitKey = Color(0xFFFFFFFF),
    functionKey = Color(0xFFE5E5EA),
    onFunctionKey = Color(0xFF000000),
    field = Color(0xFFE9E9EE),
)

private val Dark = Palette(
    background = Color(0xFF000000),
    card = Color(0xFF1C1C1E),
    label = Color(0xFFFFFFFF),
    secondary = Color(0xFF8E8E93),
    separator = Color(0xFF38383A),
    accent = Color(0xFF0A84FF),
    onAccent = Color(0xFFFFFFFF),
    digitKey = Color(0xFF2C2C2E),
    functionKey = Color(0xFF636366),
    onFunctionKey = Color(0xFFFFFFFF),
    field = Color(0xFF2C2C2E),
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
