package com.getguruhat.rateguru.ui

import android.text.format.DateUtils
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.getguruhat.rateguru.Calc
import com.getguruhat.rateguru.ConverterState
import com.getguruhat.rateguru.data.Currencies

@Composable
fun ConverterScreen(
    state: ConverterState,
    onKey: (String) -> Unit,
    onActivate: (top: Boolean) -> Unit,
    onSwap: () -> Unit,
    onPickTop: () -> Unit,
    onPickBottom: () -> Unit,
    onQuickAmount: (Double) -> Unit,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
) {
    val p = LocalPalette.current
    Column(
        Modifier
            .fillMaxSize()
            .background(p.background)
            .systemBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("RateGuru", color = p.label, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = p.secondary)
            }
        }
        Spacer(Modifier.height(8.dp))

        // The two currencies, with the swap button sitting on the line between them.
        Box {
            Column(Modifier.clip(RoundedCornerShape(22.dp)).background(p.card)) {
                CurrencyRow(state, top = true, onActivate, onPickTop)
                HorizontalDivider(color = p.separator, thickness = 0.5.dp, modifier = Modifier.padding(start = 72.dp))
                CurrencyRow(state, top = false, onActivate, onPickBottom)
            }
            var turns by remember { mutableFloatStateOf(0f) }
            val angle by animateFloatAsState(turns * 180f, label = "swap")
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 18.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(p.background)
                    .clickable { turns += 1f; onSwap() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.SwapVert, contentDescription = "Swap currencies", tint = p.accent, modifier = Modifier.rotate(angle))
            }
        }

        RateLine(state, onRefresh)
        QuickMath(state)
        QuickAmounts(state, onQuickAmount)

        Spacer(Modifier.height(12.dp))
        Keypad(onKey, Modifier.fillMaxWidth().weight(1f).heightIn(max = 470.dp).padding(bottom = 12.dp))
    }
}

@Composable
private fun CurrencyRow(state: ConverterState, top: Boolean, onActivate: (Boolean) -> Unit, onPick: () -> Unit) {
    val p = LocalPalette.current
    val code = if (top) state.top else state.bottom
    val info = Currencies.info(code)
    val active = state.topActive == top
    val text: String
    val sub: String?
    if (active) {
        text = Calc.display(state.expr)
        // While doing a sum, show its total under it.
        sub = if (Calc.hasOperator(state.expr)) Calc.evaluate(state.expr)?.let { "= " + Calc.format(it, info.decimals) } else null
    } else {
        text = state.converted?.let { Calc.format(it, info.decimals) } ?: "—"
        sub = null
    }
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .clickable { onActivate(top) }
            .padding(start = 16.dp, end = 18.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Flag and code open the currency list; the rest of the row makes it the one you type into.
        Row(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onPick)
                .padding(vertical = 6.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(info.flag, fontSize = 34.sp)
            Spacer(Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(code, color = p.label, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Change currency", tint = p.secondary, modifier = Modifier.size(20.dp))
                }
                Text(
                    info.name,
                    color = p.secondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(110.dp),
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(
                text,
                color = if (active) p.label else p.secondary,
                fontSize = when {
                    text.length > 18 -> 20.sp
                    text.length > 14 -> 26.sp
                    text.length > 10 -> 32.sp
                    else -> 40.sp
                },
                fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                textAlign = TextAlign.End,
                overflow = TextOverflow.Ellipsis,
            )
            if (sub != null) Text(sub, color = p.secondary, fontSize = 15.sp, maxLines = 1)
            if (active) {
                Box(Modifier.padding(top = 4.dp).width(28.dp).height(3.dp).clip(CircleShape).background(p.accent))
            }
        }
    }
}

@Composable
private fun RateLine(state: ConverterState, onRefresh: () -> Unit) {
    val p = LocalPalette.current
    val r = state.rates.convert(1.0, state.top, state.bottom) ?: return
    // Say it the way people think: "1 USD = 25,834 VND", never "1 VND = 0.0000387 USD".
    val (from, to, rate) = if (r >= 1) Triple(state.top, state.bottom, r) else Triple(state.bottom, state.top, 1 / r)
    val ago = DateUtils.getRelativeTimeSpanString(state.rates.updated * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
    val status = when {
        state.refreshing -> "Updating…"
        state.offline -> "Offline · rates $ago"
        else -> "Updated $ago"
    }
    Row(
        Modifier.fillMaxWidth().padding(top = 12.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "1 $from = ${Calc.format(rate, Currencies.info(to).decimals.coerceAtLeast(2).takeIf { rate < 100 } ?: 0)} $to",
                color = p.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(status, color = p.secondary, fontSize = 13.sp)
        }
        IconButton(onClick = onRefresh, enabled = !state.refreshing) {
            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh rates", tint = p.secondary)
        }
    }
}

@Composable
private fun QuickMath(state: ConverterState) {
    val p = LocalPalette.current
    val rate = state.rates.convert(1.0, state.input, state.output) ?: return
    val tip = Calc.mentalMath(rate) ?: return
    Row(
        Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = p.accent, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            "In your head: $tip",
            color = p.secondary,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Ready answers for common amounts, so most of the time there's nothing to type. */
@Composable
private fun QuickAmounts(state: ConverterState, onPick: (Double) -> Unit) {
    val p = LocalPalette.current
    val perUsd = state.rates.perUsd[state.input] ?: return
    val out = Currencies.info(state.output)
    val inInfo = Currencies.info(state.input)
    LazyRow(
        Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
    ) {
        items(Calc.quickAmounts(perUsd)) { amount ->
            val result = state.rates.convert(amount, state.input, state.output) ?: return@items
            Column(
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(p.card)
                    .clickable { onPick(amount) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Text("${inInfo.symbol}${Calc.compact(amount)}", color = p.label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("${out.symbol}${Calc.format(result, out.decimals)}", color = p.secondary, fontSize = 12.sp)
            }
        }
    }
}
