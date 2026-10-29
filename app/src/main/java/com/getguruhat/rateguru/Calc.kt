package com.getguruhat.rateguru

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

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
}
