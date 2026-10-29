package com.getguruhat.rateguru

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.getguruhat.rateguru.ui.ConverterScreen
import com.getguruhat.rateguru.ui.CurrencyPicker
import com.getguruhat.rateguru.ui.RateGuruTheme
import com.getguruhat.rateguru.ui.SettingsSheet
import com.getguruhat.rateguru.update.UpdateDialog
import com.getguruhat.rateguru.update.UpdateViewModel

class MainActivity : ComponentActivity() {

    private val converter: ConverterViewModel by viewModels()
    private val updates: UpdateViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppTheme.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RateGuruTheme {
                val state by converter.state.collectAsStateWithLifecycle()
                val update by updates.state.collectAsStateWithLifecycle()
                // Saveable, so the sheets come back after a theme change recreates the screen.
                var picking by rememberSaveable { mutableStateOf<Slot?>(null) }
                var settings by rememberSaveable { mutableStateOf(false) }

                ConverterScreen(
                    state = state,
                    onKey = converter::press,
                    onActivate = converter::activate,
                    onSwap = converter::swap,
                    onPickTop = { picking = Slot.Top },
                    onPickBottom = { picking = Slot.Bottom },
                    onQuickAmount = converter::setAmount,
                    onRefresh = converter::refresh,
                    onSettings = { settings = true },
                )

                if (settings) {
                    SettingsSheet(
                        home = state.home,
                        detected = state.detected,
                        autoDetect = state.autoDetect,
                        theme = AppTheme.read(this),
                        onPickHome = { picking = Slot.Home },
                        onAutoDetect = converter::setAutoDetect,
                        onTheme = {
                            AppTheme.write(this, it)
                            recreate()
                        },
                        onDismiss = { settings = false },
                    )
                }

                picking?.let { slot ->
                    val selected = when (slot) {
                        Slot.Top -> state.top
                        Slot.Bottom -> state.bottom
                        Slot.Home -> state.home
                    }
                    CurrencyPicker(
                        title = when (slot) {
                            Slot.Home -> "Home currency"
                            else -> "Choose currency"
                        },
                        selected = selected,
                        all = state.rates.perUsd.keys,
                        suggested = listOfNotNull(state.detected, state.home) + state.recents,
                        onPick = {
                            converter.pick(slot, it)
                            picking = null
                        },
                        onDismiss = { picking = null },
                    )
                }

                UpdateDialog(update, onUpdate = updates::update, onDismiss = updates::dismiss)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updates.onResume()
    }
}
