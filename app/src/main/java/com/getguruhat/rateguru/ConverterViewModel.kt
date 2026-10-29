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
    /** Usually where you are (top) and your home currency (bottom). */
    val top: String,
    val bottom: String,
    val home: String,
    /** True while typing into the top row; tap the other row to type there. */
    val topActive: Boolean = true,
    val expr: String = "",
    val rates: Rates,
    val refreshing: Boolean = false,
    val offline: Boolean = false,
    val detected: String? = null,
    val autoDetect: Boolean = true,
    val recents: List<String> = emptyList(),
) {
    val input get() = if (topActive) top else bottom
    val output get() = if (topActive) bottom else top
    val amount: Double get() = Calc.evaluate(expr) ?: 0.0
    val converted: Double? get() = rates.convert(amount, input, output)
}

enum class Slot { Top, Bottom, Home }

class ConverterViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)
    private val repo = RatesRepository(app)
    private var justEvaluated = false

    private val _state: MutableStateFlow<ConverterState>
    val state: StateFlow<ConverterState>

    init {
        val home = prefs.home ?: Currencies.detectHome(app).also { prefs.home = it }
        val detected = Currencies.detectLocal(app)
        // A newly detected country (you just landed) wins; otherwise keep the user's own pick.
        val local = if (prefs.autoDetect && detected != null && detected != prefs.lastDetected) detected else prefs.local ?: detected
        detected?.let { prefs.lastDetected = it }
        val top = local?.takeIf { it != home } ?: if (home == "USD") "EUR" else "USD"
        prefs.local = top
        _state = MutableStateFlow(
            ConverterState(
                top = top,
                bottom = home,
                home = home,
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

    fun press(key: String) {
        val s = _state.value
        var e = s.expr
        val last = e.lastOrNull()
        val currentNumber = e.takeLastWhile { it.isDigit() || it == '.' }
        when (key) {
            "AC" -> e = ""
            "⌫" -> e = e.dropLast(1)
            "=" -> {
                val v = Calc.evaluate(e)
                e = if (v == null || v < 0) "" else Calc.plain(v, 8).let { p ->
                    if ('.' in p) p.trimEnd('0').trimEnd('.') else p
                }
                justEvaluated = true
                _state.update { it.copy(expr = e) }
                return
            }
            "%" -> if (last != null && last.isDigit()) e += "%"
            "." -> {
                if (justEvaluated) e = ""
                if ('.' !in currentNumber) e += if (currentNumber.isEmpty()) "0." else "."
            }
            "000", "0" -> {
                if (justEvaluated) e = ""
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
        _state.update { it.copy(expr = e) }
    }

    fun setAmount(v: Double) {
        justEvaluated = true
        _state.update { it.copy(expr = Calc.plain(v, 0)) }
    }

    /** Make the other row the one you type into, starting from the number it showed. */
    fun activate(top: Boolean) {
        val s = _state.value
        if (s.topActive == top) return
        val carried = s.converted?.takeIf { s.expr.isNotEmpty() && it > 0 }
            ?.let { Calc.plain(it, Currencies.info(s.output).decimals) }
            .orEmpty()
        justEvaluated = true
        _state.update { it.copy(topActive = top, expr = carried) }
    }

    fun swap() {
        _state.update { it.copy(top = it.bottom, bottom = it.top) }
        prefs.local = _state.value.top
    }

    fun pick(slot: Slot, code: String) {
        val s = _state.value
        when (slot) {
            Slot.Home -> {
                prefs.home = code
                _state.update {
                    val top = if (it.top == code) it.bottom else it.top
                    it.copy(home = code, bottom = code, top = top)
                }
            }
            Slot.Top -> _state.update { if (code == it.bottom) it.copy(top = code, bottom = it.top) else it.copy(top = code) }
            Slot.Bottom -> _state.update { if (code == it.top) it.copy(bottom = code, top = it.bottom) else it.copy(bottom = code) }
        }
        prefs.local = _state.value.top
        val recents = (listOf(code) + s.recents.filter { it != code }).take(6)
        prefs.recents = recents
        _state.update { it.copy(recents = recents) }
    }

    fun setAutoDetect(on: Boolean) {
        prefs.autoDetect = on
        _state.update { it.copy(autoDetect = on) }
    }
}
