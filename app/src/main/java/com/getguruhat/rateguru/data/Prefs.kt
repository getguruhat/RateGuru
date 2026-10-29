package com.getguruhat.rateguru.data

import android.content.Context

/** Small settings store; the theme lives in the same file (see AppTheme). */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var home: String?
        get() = sp.getString("home", null)
        set(value) = sp.edit().putString("home", value).apply()

    /** The currencies on the main screen, top to bottom. */
    var codes: List<String>
        get() = sp.getString("codes", "")!!.split(",").filter { it.length == 3 }
        set(value) = sp.edit().putString("codes", value.joinToString(",")).apply()

    /** The row being typed into. */
    var active: String?
        get() = sp.getString("active", null)
        set(value) = sp.edit().putString("active", value).apply()

    /** What was on the keypad, so the app reopens where you left it. */
    var expr: String
        get() = sp.getString("expr", "100")!!
        set(value) = sp.edit().putString("expr", value).apply()

    /** Follow the mobile network's country when it changes (e.g. after landing). */
    var autoDetect: Boolean
        get() = sp.getBoolean("autoDetect", true)
        set(value) = sp.edit().putBoolean("autoDetect", value).apply()

    /** Last country currency we detected, so a manual choice sticks until you travel again. */
    var lastDetected: String?
        get() = sp.getString("lastDetected", null)
        set(value) = sp.edit().putString("lastDetected", value).apply()

    var recents: List<String>
        get() = sp.getString("recents", "")!!.split(",").filter { it.length == 3 }
        set(value) = sp.edit().putString("recents", value.joinToString(",")).apply()
}
