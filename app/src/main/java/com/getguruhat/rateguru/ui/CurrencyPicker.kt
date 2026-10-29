package com.getguruhat.rateguru.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.getguruhat.rateguru.data.Currencies

/** Full-height sheet: search by code, name or country ("japan" finds JPY). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPicker(
    title: String,
    selected: String,
    all: Collection<String>,
    suggested: List<String>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
    /** Shown as "Remove from list" when the row can be removed. */
    onRemove: (() -> Unit)? = null,
) {
    val p = LocalPalette.current
    var query by rememberSaveable { mutableStateOf("") }
    val sorted = remember(all) { all.sortedBy { Currencies.info(it).name } }
    val q = query.trim().lowercase()
    val results = if (q.isEmpty()) sorted else sorted.filter { code ->
        val info = Currencies.info(code)
        code.lowercase().startsWith(q) ||
            info.name.lowercase().contains(q) ||
            Currencies.countryNames[code].orEmpty().any { it.lowercase().contains(q) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = p.background,
    ) {
        Column(Modifier.fillMaxHeight(0.92f).imePadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = p.label, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (onRemove != null) {
                    Text(
                        "Remove from list",
                        color = Color(0xFFFF3B30),
                        fontSize = 15.sp,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onRemove).padding(6.dp),
                    )
                }
            }
            Row(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(p.field)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = p.secondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = p.label, fontSize = 17.sp),
                    cursorBrush = SolidColor(p.accent),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (query.isEmpty()) Text("Currency or country", color = p.secondary, fontSize = 17.sp)
                        inner()
                    },
                )
            }
            LazyColumn(Modifier.fillMaxWidth()) {
                if (q.isEmpty() && suggested.isNotEmpty()) {
                    item { Header("Suggested") }
                    item { Group(suggested.distinct(), selected, onPick) }
                    item { Header("All currencies") }
                }
                item { Group(results, selected, onPick) }
                item { Spacer(Modifier.padding(bottom = 24.dp)) }
            }
        }
    }
}

@Composable
private fun Header(text: String) {
    Text(
        text.uppercase(),
        color = LocalPalette.current.secondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 32.dp, top = 12.dp, bottom = 6.dp),
    )
}

/** An inset grouped list, like iOS Settings. */
@Composable
private fun Group(codes: List<String>, selected: String, onPick: (String) -> Unit) {
    val p = LocalPalette.current
    if (codes.isEmpty()) {
        Text("No matches", color = p.secondary, modifier = Modifier.padding(32.dp))
        return
    }
    Column(Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(14.dp)).background(p.card)) {
        codes.forEachIndexed { i, code ->
            val info = Currencies.info(code)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onPick(code) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(info.flag, fontSize = 28.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(code, color = p.label, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text(info.name, color = p.secondary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (code == selected) Icon(Icons.Rounded.Check, contentDescription = "Selected", tint = p.accent)
            }
            if (i < codes.lastIndex) {
                androidx.compose.material3.HorizontalDivider(color = p.separator, thickness = 0.5.dp, modifier = Modifier.padding(start = 58.dp))
            }
        }
    }
}
