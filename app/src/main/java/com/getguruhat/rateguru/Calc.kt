package com.getguruhat.rateguru

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong

/** Keypad math and number formatting. Operators are the display glyphs + − × ÷ %. */
object Calc {

    const val OPERATORS = "+−×÷"

    private sealed interface Tok {
        data class Num(val v: Double) : Tok
        data class Op(val c: Char) : Tok
        data object Pct : Tok
    }

    /** Evaluates "25000×3+10%"; null while the expression can't be computed yet. */
    fun evaluate(expr: String): Double? {
        val toks = mutableListOf<Tok>()
        val num = StringBuilder()
        fun flush(): Boolean {
            if (num.isEmpty()) return true
            val v = num.toString().toDoubleOrNull() ?: return false
            toks += Tok.Num(v)
            num.clear()
            return true
        }
        for (c in expr) {
            when {
                c.isDigit() || c == '.' -> num.append(c)
                c == '%' -> { if (!flush()) return null; toks += Tok.Pct }
                c in OPERATORS -> { if (!flush()) return null; toks += Tok.Op(c) }
            }
        }
        if (!flush()) return null
        while (toks.lastOrNull() is Tok.Op) toks.removeAt(toks.lastIndex)
        return eval(toks)
    }

    private fun eval(input: List<Tok>): Double? {
        val t = input.toMutableList()
        // "a + b%" means a plus b percent of a (like a tip); a lone "b%" is b/100.
        var i = 0
        while (i < t.size) {
            if (t[i] == Tok.Pct) {
                val n = t.getOrNull(i - 1) as? Tok.Num ?: return null
                val op = t.getOrNull(i - 2) as? Tok.Op
                val v = if (op != null && (op.c == '+' || op.c == '−')) {
                    (eval(t.subList(0, i - 2).toList()) ?: return null) * n.v / 100
                } else {
                    n.v / 100
                }
                t[i - 1] = Tok.Num(v)
                t.removeAt(i)
            } else {
                i++
            }
        }
        var term = (t.firstOrNull() as? Tok.Num)?.v ?: return null
        var sum = 0.0
        var sign = 1.0
        i = 1
        while (i + 1 < t.size) {
            val op = (t[i] as? Tok.Op)?.c ?: return null
            val n = (t[i + 1] as? Tok.Num)?.v ?: return null
            when (op) {
                '×' -> term *= n
                '÷' -> { if (n == 0.0) return null; term /= n }
                else -> { sum += sign * term; sign = if (op == '+') 1.0 else -1.0; term = n }
            }
            i += 2
        }
        return sum + sign * term
    }

    fun hasOperator(expr: String) = expr.any { it in OPERATORS || it == '%' }

    private val symbols = DecimalFormatSymbols(Locale.US)

    /** "1234567.5" -> "1,234,567.5", keeping what the user typed after the point. */
    private fun groupTyped(number: String): String {
        val dot = number.indexOf('.')
        val whole = if (dot < 0) number else number.substring(0, dot)
        val grouped = whole.toBigIntegerOrNull()?.let { DecimalFormat("#,##0", symbols).format(it) } ?: whole
        return if (dot < 0) grouped else grouped + number.substring(dot)
    }

    /** The expression as shown: numbers grouped, spaces around operators. */
    fun display(expr: String): String {
        if (expr.isEmpty()) return "0"
        val out = StringBuilder()
        val num = StringBuilder()
        for (c in expr) {
            if (c.isDigit() || c == '.') {
                num.append(c)
            } else {
                out.append(groupTyped(num.toString())); num.clear()
                out.append(if (c == '%') "%" else " $c ")
            }
        }
        out.append(groupTyped(num.toString()))
        return out.toString()
    }

    /** A result as money: the currency's usual decimals, none for big numbers, more for tiny ones. */
    fun format(v: Double, decimals: Int): String {
        if (v.isNaN() || v.isInfinite()) return "—"
        val a = abs(v)
        val bd = when {
            a == 0.0 -> BigDecimal.ZERO
            a >= 100_000 -> BigDecimal(v).setScale(0, RoundingMode.HALF_UP)
            a >= 1 -> BigDecimal(v).setScale(decimals, RoundingMode.HALF_UP)
            else -> BigDecimal(v).round(MathContext(3)).stripTrailingZeros().let { if (it.scale() < decimals) it.setScale(decimals) else it }
        }
        val pattern = if (bd.scale() > 0) "#,##0." + "0".repeat(bd.scale()) else "#,##0"
        return DecimalFormat(pattern, symbols).format(bd)
    }

    /** A result to put back on the keypad, without grouping: "3.87", "25834". */
    fun plain(v: Double, decimals: Int): String = format(v, decimals).replace(",", "")

    /** "10K", "2.5M" for chips. */
    fun compact(v: Double): String {
        fun trim(x: Double) = BigDecimal(x).round(MathContext(3)).stripTrailingZeros().toPlainString()
        return when {
            v >= 1e9 -> trim(v / 1e9) + "B"
            v >= 1e6 -> trim(v / 1e6) + "M"
            v >= 1e4 -> trim(v / 1e3) + "K"
            else -> format(v, 0)
        }
    }

    /** Round amounts that match the banknotes people actually hold: 10K, 20K, 50K, 100K… */
    fun quickAmounts(unitsPerUsd: Double): List<Double> {
        val start = 10.0.pow(floor(log10(unitsPerUsd)).coerceAtLeast(0.0))
        return listOf(1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0, 200.0).map { it * start }
    }

    /**
     * A one-line rule for doing the conversion in your head, where [rate] is how many
     * "to" units one "from" unit buys. VND -> USD at 1/25,834 gives "Drop 3 zeros, then ÷ 26".
     */
    fun mentalMath(rate: Double): String? {
        if (rate <= 0 || rate.isNaN()) return null
        if (rate in 0.97..1.03) return "About the same value"
        fun nice(x: Double): String =
            if (x >= 10) x.roundToLong().toString()
            else BigDecimal(x).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        fun zeros(k: Int) = if (k == 1) "1 zero" else "$k zeros"
        return if (rate < 1) {
            val d = 1 / rate
            when {
                d >= 1000 -> {
                    val k = floor(log10(d)).toInt() - 1
                    "Drop ${zeros(k)}, then ÷ ${nice(d / 10.0.pow(k))}"
                }
                d >= 3 -> "Divide by ${nice(d)}"
                else -> "Multiply by ${BigDecimal(rate).setScale(2, RoundingMode.HALF_UP).toPlainString()}"
            }
        } else {
            when {
                rate >= 1000 -> {
                    val k = floor(log10(rate)).toInt() - 1
                    "Multiply by ${nice(rate / 10.0.pow(k))}, then add ${zeros(k)}"
                }
                else -> "Multiply by ${nice(rate)}"
            }
        }
    }
}
