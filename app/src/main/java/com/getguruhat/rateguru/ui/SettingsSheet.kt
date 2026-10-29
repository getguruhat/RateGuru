package com.getguruhat.rateguru.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.getguruhat.rateguru.BuildConfig
import com.getguruhat.rateguru.R
import com.getguruhat.rateguru.data.Currencies

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    home: String,
    detected: String?,
    autoDetect: Boolean,
    theme: String,
    onPickHome: () -> Unit,
    onAutoDetect: (Boolean) -> Unit,
    onTheme: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val p = LocalPalette.current
    val uri = LocalUriHandler.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = p.background,
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = 24.dp)) {
            Text("Settings", color = p.label, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))

            Section("Currencies")
            Card {
                val h = Currencies.info(home)
                Item("Home currency", Modifier.clickable(onClick = onPickHome)) {
                    Text("${h.flag} $home", color = p.secondary, fontSize = 17.sp)
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = p.secondary)
                }
                Divider()
                Item("Detect the country I'm in") {
                    Switch(
                        checked = autoDetect,
                        onCheckedChange = onAutoDetect,
                        colors = SwitchDefaults.colors(checkedTrackColor = p.accent),
                    )
                }
            }
            Footer(
                if (detected != null) "Right now your phone's network says ${Currencies.info(detected).flag} $detected. " +
                    "When you land somewhere new, RateGuru switches to the local currency on its own."
                else "When you land somewhere new, RateGuru switches to the local currency on its own, using the mobile network (no location access).",
            )

            Section("Appearance")
            Card {
                Row(Modifier.padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("system" to "Automatic", "light" to "Light", "dark" to "Dark").forEach { (key, label) ->
                        val on = key == theme
                        Text(
                            label,
                            color = if (on) p.onAccent else p.label,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (on) p.accent else p.field)
                                .clickable { onTheme(key) }
                                .padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }

            Section("About")
            Card {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.about_icon),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("RateGuru ${BuildConfig.VERSION_NAME}", color = p.label, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Text("No ads. No account. Works offline.", color = p.secondary, fontSize = 14.sp)
                    }
                }
                Divider()
                Item("Made by GuruHat", Modifier.clickable { uri.openUri("https://guruhat.com") }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = p.secondary)
                }
                Divider()
                Item("Source code", Modifier.clickable { uri.openUri("https://github.com/${BuildConfig.UPDATE_REPO}") }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = p.secondary)
                }
                Divider()
                Item("Rates by Exchange Rate API", Modifier.clickable { uri.openUri("https://www.exchangerate-api.com") }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = p.secondary)
                }
            }
            Footer("Rates are mid-market and update once a day. Banks, cards and exchange booths add their own margin.")
        }
    }
}

@Composable
private fun Section(text: String) {
    Text(
        text.uppercase(),
        color = LocalPalette.current.secondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 32.dp, top = 20.dp, bottom = 6.dp),
    )
}

@Composable
private fun Footer(text: String) {
    Text(
        text,
        color = LocalPalette.current.secondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 6.dp),
    )
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(LocalPalette.current.card),
    ) { content() }
}

@Composable
private fun Divider() {
    HorizontalDivider(color = LocalPalette.current.separator, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
}

@Composable
private fun Item(label: String, modifier: Modifier = Modifier, trailing: @Composable () -> Unit) {
    Row(
        modifier.fillMaxWidth().height(52.dp).padding(start = 16.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = LocalPalette.current.label, fontSize = 17.sp, modifier = Modifier.weight(1f))
        trailing()
    }
}
