package com.getguruhat.rateguru.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Gap = 10.dp
private val KeyShape = RoundedCornerShape(22.dp)

/**
 * Four rows, like a classic converter: C / refresh / a tall calculator key on the left,
 * digits on the right. The calculator key shows a row of + − × ÷ % = only when you need it.
 */
@Composable
fun Keypad(
    onKey: (String) -> Unit,
    mathMode: Boolean,
    onToggleMath: () -> Unit,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = LocalPalette.current
    BoxWithConstraints(modifier) {
        // Explicit row height so the tall key lines up exactly with two digit rows.
        val row = (maxHeight - Gap * 3) / 4
        Row(horizontalArrangement = Arrangement.spacedBy(Gap)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Gap)) {
                Key(Modifier.height(row), p.functionKey, onClick = { onKey("AC") }) {
                    Text("C", color = p.onFunctionKey, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                }
                Key(Modifier.height(row), p.functionKey, onClick = onRefresh) {
                    val spin by rememberInfiniteTransition(label = "refresh").animateFloat(
                        0f, 360f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart), label = "spin",
                    )
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = "Refresh rates",
                        tint = p.onFunctionKey,
                        modifier = Modifier.size(30.dp).rotate(if (refreshing) spin else 0f),
                    )
                }
                // Spans the last two rows.
                Key(Modifier.height(row * 2 + Gap), p.calcKey, onClick = onToggleMath) {
                    Icon(
                        if (mathMode) Icons.Rounded.Calculate else Icons.Outlined.Calculate,
                        contentDescription = if (mathMode) "Hide calculator" else "Calculator",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
            Column(Modifier.weight(3f), verticalArrangement = Arrangement.spacedBy(Gap)) {
                listOf(listOf("7", "8", "9"), listOf("4", "5", "6"), listOf("1", "2", "3"), listOf("0", ".", "⌫")).forEach { keys ->
                    Row(Modifier.height(row), horizontalArrangement = Arrangement.spacedBy(Gap)) {
                        keys.forEach { key ->
                            Key(
                                Modifier.weight(1f),
                                p.digitKey,
                                onClick = { onKey(key) },
                                // Hold 0 for 000 (big notes), hold delete to clear.
                                onLongClick = when (key) {
                                    "0" -> ({ onKey("000") })
                                    "⌫" -> ({ onKey("AC") })
                                    else -> null
                                },
                            ) {
                                if (key == "⌫") {
                                    Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = "Delete", tint = p.label)
                                } else {
                                    Text(key, color = p.label, fontSize = 30.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** The slim operator row shown above the keypad in calculator mode. */
@Composable
fun MathRow(onKey: (String) -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Row(modifier.fillMaxWidth().height(46.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("÷", "×", "−", "+", "%", "=").forEach { key ->
            val equals = key == "="
            Key(Modifier.weight(1f), if (equals) p.calcKey else p.digitKey, onClick = { onKey(key) }) {
                Text(key, color = if (equals) Color.White else p.accent, fontSize = 24.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Key(
    modifier: Modifier,
    color: Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val p = LocalPalette.current
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(KeyShape)
            .background(if (pressed) lerp(color, p.label, 0.15f) else color)
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                onLongClick = onLongClick?.let { long ->
                    {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        long()
                    }
                },
            ),
        contentAlignment = Alignment.Center,
    ) { content() }
}

private fun lerp(a: Color, b: Color, t: Float) = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = 1f,
)

