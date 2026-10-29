package com.getguruhat.rateguru

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
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

private const val PICK_ADD = -1
private const val PICK_HOME = -2

class MainActivity : ComponentActivity() {

    private val converter: ConverterViewModel by viewModels()
    private val updates: UpdateViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppTheme.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Light system-bar icons always: they sit on the orange (or navy) backdrop in both themes.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            RateGuruTheme {
                val state by converter.state.collectAsStateWithLifecycle()
                val update by updates.state.collectAsStateWithLifecycle()
                // Saveable, so the sheets come back after a theme change recreates the screen.
                var picking by rememberSaveable { mutableStateOf<Int?>(null) }
                var settings by rememberSaveable { mutableStateOf(false) }

                ConverterScreen(
                    state = state,
                    onKey = converter::press,
                    onActivate = converter::activate,
                    onPick = { picking = it },
                    onAdd = { picking = PICK_ADD },
                    onRefresh = converter::refresh,
                    onSettings = { settings = true },
                )

                if (settings) {
                    SettingsSheet(
                        home = state.home,
                        detected = state.detected,
                        autoDetect = state.autoDetect,
                        theme = AppTheme.read(this),
                        onPickHome = { picking = PICK_HOME },
                        onAutoDetect = converter::setAutoDetect,
                        onTheme = {
                            AppTheme.write(this, it)
                            recreate()
                        },
                        onDismiss = { settings = false },
                    )
                }

                // A row index to change that row, or PICK_ADD / PICK_HOME.
                picking?.let { target ->
                    val listed = state.rates.perUsd.keys
                    CurrencyPicker(
                        title = when (target) {
                            PICK_HOME -> "Home currency"
                            PICK_ADD -> "Add currency"
                            else -> "Change currency"
                        },
                        selected = when (target) {
                            PICK_HOME -> state.home
                            PICK_ADD -> ""
                            else -> state.codes.getOrElse(target) { "" }
                        },
                        all = if (target == PICK_ADD) listed - state.codes.toSet() else listed,
                        suggested = (listOfNotNull(state.detected, state.home) + state.recents)
                            .filter { target != PICK_ADD || it !in state.codes },
                        onPick = {
                            when (target) {
                                PICK_HOME -> converter.setHome(it)
                                PICK_ADD -> converter.add(it)
                                else -> converter.replace(target, it)
                            }
                            picking = null
                        },
                        onDismiss = { picking = null },
                        onRemove = if (target >= 0 && state.codes.size > 2) {
                            {
                                converter.remove(target)
                                picking = null
                            }
                        } else null,
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
