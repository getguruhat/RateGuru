package com.getguruhat.rateguru.ui

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.getguruhat.rateguru.Calc
import com.getguruhat.rateguru.ConverterState
import com.getguruhat.rateguru.MAX_CURRENCIES
import com.getguruhat.rateguru.data.Currencies

@Composable
fun ConverterScreen(
    state: ConverterState,
    onKey: (String) -> Unit,
    onActivate: (String) -> Unit,
    onPick: (index: Int) -> Unit,
    onAdd: () -> Unit,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
) {
    val p = LocalPalette.current
    var mathMode by rememberSaveable { mutableStateOf(false) }
    // Charcoal backdrop under everything; the icon's blue behind the header, running under the
    // status bar, with the currency card overlapping its bottom edge.
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(p.backdropTop, p.backdropBottom)))) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(p.headerTop, p.headerBottom)))
                .statusBarsPadding()
                .height(112.dp),
        )
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            // The keypad takes a fixed share of the screen; the list scrolls in what's left.
            val keypadHeight = min(maxHeight * 0.42f, 370.dp)
            Column(Modifier.fillMaxSize()) {
                Header(state, onSettings)

                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Column(Modifier.clip(RoundedCornerShape(20.dp)).background(p.card)) {
                        state.codes.forEachIndexed { i, code ->
                            if (i > 0) HorizontalDivider(color = p.separator, thickness = 0.5.dp, modifier = Modifier.padding(start = 64.dp))
                            CurrencyRow(state, code, onActivate = { onActivate(code) }, onPick = { onPick(i) })
                        }
                        if (state.codes.size < MAX_CURRENCIES) {
                            HorizontalDivider(color = p.separator, thickness = 0.5.dp, modifier = Modifier.padding(start = 64.dp))
                            Row(
                                Modifier.fillMaxWidth().clickable(onClick = onAdd).padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, tint = p.accent)
                                }
                                Spacer(Modifier.width(14.dp))
                                Text("Add currency", color = p.accent, fontSize = 17.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                AnimatedVisibility(mathMode) {
                    MathRow(onKey, Modifier.padding(bottom = 10.dp))
                }
                Keypad(
                    onKey = onKey,
                    mathMode = mathMode,
                    onToggleMath = { mathMode = !mathMode },
                    refreshing = state.refreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxWidth().height(keypadHeight).padding(bottom = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun Header(state: ConverterState, onSettings: () -> Unit) {
    val p = LocalPalette.current
    val now = System.currentTimeMillis()
    // The source publishes once a day, so "checked" (when this phone last asked) is the
    // number that tells you the app is current; the built-in snapshot only has its publish date.
    val checked = state.rates.fetchedAt.takeIf { it > 0 }
    val status = when {
        state.refreshing -> "Updating rates…"
        state.offline || checked == null ->
            "Offline · rates from " + DateUtils.getRelativeTimeSpanString(state.rates.updated * 1000, now, DateUtils.MINUTE_IN_MILLIS)
        now - checked < 60_000 -> "Rates checked just now"
        else -> "Rates checked " + DateUtils.getRelativeTimeSpanString(checked, now, DateUtils.MINUTE_IN_MILLIS)
    }
    Row(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("RateGuru", color = p.onBackdrop, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(status, color = p.onBackdrop.copy(alpha = 0.8f), fontSize = 13.sp)
            QuickMath(state)
        }
        IconButton(onClick = onSettings) {
            Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = p.onBackdrop)
        }
    }
}

@Composable
private fun CurrencyRow(state: ConverterState, code: String, onActivate: () -> Unit, onPick: () -> Unit) {
    val p = LocalPalette.current
    val info = Currencies.info(code)
    val active = state.active == code
    val text = if (active) Calc.display(state.expr) else state.valueOf(code)?.let { Calc.format(it, info.decimals) } ?: "—"
    // While doing a sum, show its total under it.
    val sub = if (active && Calc.hasOperator(state.expr)) Calc.evaluate(state.expr)?.let { "= " + Calc.format(it, info.decimals) } else null
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (active) p.accent.copy(alpha = 0.10f) else p.card)
            // A bar on the left edge marks the row you're typing in.
            .drawBehind { if (active) drawRect(p.accent, size = Size(4.dp.toPx(), size.height)) }
            .clickable(onClick = onActivate)
            .heightIn(min = 56.dp)
            .padding(start = 12.dp, end = 16.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Flag and code change this row's currency; the rest of the row types into it.
        Row(
            Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onPick).padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(info.flag, fontSize = 26.sp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(code, color = p.label, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    info.name,
                    color = p.secondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(96.dp),
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(
                text,
                color = if (active) p.accent else p.label,
                fontSize = when {
                    text.length > 18 -> 18.sp
                    text.length > 14 -> 22.sp
                    text.length > 10 -> 26.sp
                    else -> 30.sp
                },
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                textAlign = TextAlign.End,
                overflow = TextOverflow.Ellipsis,
            )
            if (sub != null) Text(sub, color = p.secondary, fontSize = 13.sp, maxLines = 1)
        }
    }
}

/** A rule for converting the active currency to your home currency without the phone. */
@Composable
private fun QuickMath(state: ConverterState) {
    val p = LocalPalette.current
    if (state.active == state.home) return
    val rate = state.rates.convert(1.0, state.active, state.home) ?: return
    val tip = Calc.mentalMath(rate) ?: return
    Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = p.onBackdrop, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            "${state.active} → ${state.home}: ${tip.replaceFirstChar { it.lowercase() }}",
            color = p.onBackdrop,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

