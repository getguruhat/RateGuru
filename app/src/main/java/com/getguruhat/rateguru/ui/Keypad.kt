package com.getguruhat.rateguru.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
                Key(Modifier.height(row), p.clearKey, onClick = { onKey("AC") }) {
                    Text("C", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                }
                Key(Modifier.height(row), p.refreshKey, onClick = onRefresh) {
                    val spin by rememberInfiniteTransition(label = "refresh").animateFloat(
                        0f, 360f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart), label = "spin",
                    )
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = "Refresh rates",
                        tint = Color.White,
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
                                    Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = "Delete", tint = Color.White)
                                } else {
                                    Text(key, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Medium)
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
    Row(modifier.fillMaxWidth().height(50.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("÷", "×", "−", "+", "%", "=").forEach { key ->
            val equals = key == "="
            Key(Modifier.weight(1f), if (equals) p.calcKey else p.digitKey, onClick = { onKey(key) }) {
                Text(key, color = if (equals) Color.White else p.calcKey, fontSize = 24.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/**
 * A raised key: a darker base underneath, a face with a top-lit gradient and a thin highlight
 * on its upper edge. Pressing pushes the face down onto the base.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Key(
    modifier: Modifier,
    color: Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val depth = 4.dp
    val sink by animateDpAsState(if (pressed) depth - 1.dp else 0.dp, tween(60), label = "sink")
    Box(
        modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .shadow(if (pressed) 1.dp else 5.dp, KeyShape, clip = false)
            .clip(KeyShape)
            .background(shade(color, -0.42f))
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
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = sink, bottom = depth - sink)
                .clip(KeyShape)
                .background(
                    Brush.verticalGradient(
                        if (pressed) listOf(shade(color, -0.08f), shade(color, -0.16f))
                        else listOf(shade(color, 0.16f), color, shade(color, -0.10f)),
                    ),
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.02f))),
                    KeyShape,
                ),
            contentAlignment = Alignment.Center,
        ) { content() }
    }
}

/** Lighter (amount > 0, towards white) or darker (amount < 0, towards black). */
private fun shade(c: Color, amount: Float): Color {
    val target = if (amount > 0) 1f else 0f
    val t = kotlin.math.abs(amount)
    return Color(
        red = c.red + (target - c.red) * t,
        green = c.green + (target - c.green) * t,
        blue = c.blue + (target - c.blue) * t,
        alpha = 1f,
    )
}
