package com.getguruhat.rateguru

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.getguruhat.rateguru.data.Currencies
import com.getguruhat.rateguru.data.Prefs
import com.getguruhat.rateguru.data.Rates
import com.getguruhat.rateguru.data.RatesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConverterState(
    /** The currencies on screen, top to bottom; all of them convert at once. */
    val codes: List<String>,
    /** The row the keypad types into. */
    val active: String,
    val home: String,
    val expr: String = "",
    val rates: Rates,
    val refreshing: Boolean = false,
    val offline: Boolean = false,
    val detected: String? = null,
    val autoDetect: Boolean = true,
    val recents: List<String> = emptyList(),
) {
    val amount: Double get() = Calc.evaluate(expr) ?: 0.0
    fun valueOf(code: String): Double? = if (code == active) amount else rates.convert(amount, active, code)
}

const val MAX_CURRENCIES = 8

class ConverterViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)
    private val repo = RatesRepository(app)
    private var justEvaluated = false

    private val _state: MutableStateFlow<ConverterState>
    val state: StateFlow<ConverterState>

    init {
        val home = prefs.home ?: Currencies.detectHome(app).also { prefs.home = it }
        val detected = Currencies.detectLocal(app)
        var codes = prefs.codes.ifEmpty {
            // First launch: where you are, home, then the big travel currencies.
            (listOfNotNull(detected, home) + listOf("USD", "EUR", "GBP", "JPY")).distinct().take(4)
        }
        var active = prefs.active?.takeIf { it in codes } ?: codes.first()
        // Just landed somewhere new: put its currency on top and type in it.
        if (prefs.autoDetect && detected != null && detected != prefs.lastDetected) {
            codes = (listOf(detected) + codes.filter { it != detected }).take(MAX_CURRENCIES)
            active = detected
        }
        detected?.let { prefs.lastDetected = it }
        if (home !in codes) codes = (codes + home).takeLast(MAX_CURRENCIES)
        prefs.codes = codes
        prefs.active = active
        _state = MutableStateFlow(
            ConverterState(
                codes = codes,
                active = active,
                home = home,
                expr = prefs.expr,
                rates = repo.loadCached(),
                detected = detected,
                autoDetect = prefs.autoDetect,
                recents = prefs.recents,
            ),
        )
        state = _state.asStateFlow()
        // er-api publishes once a day; don't hit it on every launch.
        if (System.currentTimeMillis() - _state.value.rates.fetchedAt > 3 * 60 * 60 * 1000L) refresh()
    }

    fun refresh() {
        if (_state.value.refreshing) return
        _state.update { it.copy(refreshing = true) }
        viewModelScope.launch {
            runCatching { repo.refresh() }
                .onSuccess { rates -> _state.update { it.copy(rates = rates, refreshing = false, offline = false) } }
                .onFailure { _state.update { it.copy(refreshing = false, offline = true) } }
        }
    }

    private fun setExpr(e: String) {
        prefs.expr = e
        _state.update { it.copy(expr = e) }
    }

    fun press(key: String) {
        val s = _state.value
        var e = s.expr
        val last = e.lastOrNull()
        val currentNumber = e.takeLastWhile { it.isDigit() || it == '.' }
        when (key) {
            "AC" -> e = ""
            "⌫" -> e = if (justEvaluated) "" else e.dropLast(1)
            "=" -> {
                val v = Calc.evaluate(e)
                e = if (v == null || v < 0) "" else Calc.plain(v, 8).let { p ->
                    if ('.' in p) p.trimEnd('0').trimEnd('.') else p
                }
                justEvaluated = true
                setExpr(e)
                return
            }
            "%" -> if (last != null && last.isDigit()) e += "%"
            "." -> {
                if (justEvaluated) e = ""
                val cur = e.takeLastWhile { it.isDigit() || it == '.' }
                if ('.' !in cur) e += if (cur.isEmpty()) "0." else "."
            }
            "000", "0" -> {
                // After "=", 0 starts a new number but 000 grows the result (600 -> 600,000).
                if (justEvaluated && key == "0") e = ""
                // No leading zeros: "0" + "0" stays "0", and "000" needs a number before it.
                val cur = e.takeLastWhile { it.isDigit() || it == '.' }
                if (cur.isNotEmpty() && cur != "0") e += key
                else if (cur.isEmpty() && key == "0" && e.lastOrNull() != '%') e += "0"
            }
            else -> when {
                key.length == 1 && key[0].isDigit() -> {
                    if (justEvaluated) e = ""
                    val cur = e.takeLastWhile { it.isDigit() || it == '.' }
                    if (e.lastOrNull() == '%') return
                    e = if (cur == "0") e.dropLast(1) + key else e + key
                }
                key in Calc.OPERATORS -> {
                    if (e.isEmpty()) return
                    e = if (last != null && last in Calc.OPERATORS) e.dropLast(1) + key else e + key
                }
            }
        }
        justEvaluated = false
        // Keep it on screen: 15 digits is already a quadrillion.
        if (e.count { it.isDigit() } > 15 && e.length > s.expr.length) return
        if (currentNumber.length > 15 && e.length > s.expr.length) return
        setExpr(e)
    }

    /** Type into another row, starting from the number it showed. */
    fun activate(code: String) {
        val s = _state.value
        if (s.active == code) return
        val carried = s.valueOf(code)?.takeIf { s.expr.isNotEmpty() && it > 0 }
            ?.let { Calc.plain(it, Currencies.info(code).decimals) }
            ?.let { if ('.' in it) it.trimEnd('0').trimEnd('.') else it }
            .orEmpty()
        justEvaluated = true
        prefs.active = code
        prefs.expr = carried
        _state.update { it.copy(active = code, expr = carried) }
    }

    /** Replace the currency at [index]; picking one already listed swaps the two rows. */
    fun replace(index: Int, code: String) {
        val s = _state.value
        val old = s.codes.getOrNull(index) ?: return
        val codes = s.codes.toMutableList()
        val existing = codes.indexOf(code)
        if (existing >= 0) codes[existing] = old
        codes[index] = code
        val active = if (s.active == old) code else s.active
        save(codes, active, code)
    }

    fun add(code: String) {
        val s = _state.value
        if (code in s.codes || s.codes.size >= MAX_CURRENCIES) return
        save(s.codes + code, s.active, code)
    }

    fun remove(index: Int) {
        val s = _state.value
        if (s.codes.size <= 2) return
        val codes = s.codes.filterIndexed { i, _ -> i != index }
        save(codes, s.active.takeIf { it in codes } ?: codes.first(), null)
    }

    fun setHome(code: String) {
        prefs.home = code
        val s = _state.value
        val codes = if (code in s.codes) s.codes else (s.codes + code).takeLast(MAX_CURRENCIES)
        _state.update { it.copy(home = code) }
        save(codes, s.active.takeIf { it in codes } ?: codes.first(), code)
    }

    private fun save(codes: List<String>, active: String, picked: String?) {
        prefs.codes = codes
        prefs.active = active
        val recents = if (picked == null) _state.value.recents
        else (listOf(picked) + _state.value.recents.filter { it != picked }).take(6)
        prefs.recents = recents
        _state.update { it.copy(codes = codes, active = active, recents = recents) }
    }

    fun setAutoDetect(on: Boolean) {
        prefs.autoDetect = on
        _state.update { it.copy(autoDetect = on) }
    }
}
