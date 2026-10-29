package com.getguruhat.rateguru.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val rows = listOf(
    listOf("AC", "⌫", "%", "÷"),
    listOf("7", "8", "9", "×"),
    listOf("4", "5", "6", "−"),
    listOf("1", "2", "3", "+"),
    listOf("000", "0", ".", "="),
)

/** Calculator keys in the iOS style: rounded, flat, operators in the accent colour. */
@Composable
fun Keypad(onKey: (String) -> Unit, modifier: Modifier = Modifier) {
    val gap = 10.dp
    Column(modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { key -> Key(key, onKey, Modifier.weight(1f).fillMaxHeight()) }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Key(key: String, onKey: (String) -> Unit, modifier: Modifier) {
    val p = LocalPalette.current
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val isOperator = key in "÷×−+="
    val isFunction = key in listOf("AC", "⌫", "%")
    val bg = when {
        isOperator -> p.accent
        isFunction -> p.functionKey
        else -> p.digitKey
    }
    val fg = when {
        isOperator -> p.onAccent
        isFunction -> p.onFunctionKey
        else -> p.label
    }
    Box(
        modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (pressed) lerp(bg, fg, 0.18f) else bg)
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKey(key)
                },
                // Hold delete to clear everything.
                onLongClick = if (key == "⌫") {
                    {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKey("AC")
                    }
                } else null,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (key == "⌫") {
            Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = "Delete", tint = fg)
        } else {
            Text(
                key,
                color = fg,
                fontSize = when {
                    key == "000" || key == "AC" -> 22.sp
                    isOperator -> 32.sp
                    else -> 28.sp
                },
                fontWeight = if (isOperator) FontWeight.Normal else FontWeight.Medium,
            )
        }
    }
}

private fun lerp(a: Color, b: Color, t: Float) = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = 1f,
)
