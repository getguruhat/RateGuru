package com.getguruhat.rateguru.data

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Locale

data class CurrencyInfo(
    val code: String,
    val name: String,
    val flag: String,
    val symbol: String,
    /** Usual number of decimals: 2 for USD, 0 for VND and JPY. */
    val decimals: Int,
)

object Currencies {

    // Codes the rates feed has that Android's currency data doesn't name.
    private val extraNames = mapOf(
        "FOK" to "Faroese Króna",
        "GGP" to "Guernsey Pound",
        "IMP" to "Manx Pound",
        "JEP" to "Jersey Pound",
        "KID" to "Kiribati Dollar",
        "TVD" to "Tuvaluan Dollar",
        "SLE" to "Sierra Leonean Leone",
        "VES" to "Venezuelan Bolívar",
        "XCG" to "Caribbean Guilder",
        "ZWG" to "Zimbabwe Gold",
    )

    // Currencies whose first two letters aren't the country to show a flag for.
    private val flagCountry = mapOf(
        "EUR" to "EU",
        "XAF" to "CM",
        "XOF" to "SN",
        "XCD" to "AG",
        "XPF" to "PF",
        "ANG" to "CW",
        "XCG" to "CW",
    )

    private val cache = HashMap<String, CurrencyInfo>()

    fun info(code: String): CurrencyInfo = cache.getOrPut(code) {
        val locale = Locale.getDefault()
        val icu = runCatching { android.icu.util.Currency.getInstance(code) }.getOrNull()
        val icuName = icu?.getDisplayName(locale)
        val name = extraNames[code] ?: icuName?.takeIf { it != code } ?: code
        val symbol = icu?.let {
            runCatching {
                it.getName(android.icu.util.ULocale.forLocale(locale), android.icu.util.Currency.NARROW_SYMBOL_NAME, null)
            }.getOrNull()
        } ?: code
        val decimals = icu?.defaultFractionDigits?.takeIf { it in 0..3 } ?: 2
        CurrencyInfo(code, name, flagFor(code), symbol, decimals)
    }

    private fun flagFor(code: String): String {
        val country = flagCountry[code] ?: code.take(2).takeUnless { code.startsWith("X") } ?: return "🌐"
        if (country.length != 2 || !country.all { it in 'A'..'Z' }) return "🌐"
        return country.map { String(Character.toChars(0x1F1E6 + (it - 'A'))) }.joinToString("")
    }

    /** The currency a country uses, from its two-letter ISO code. */
    fun forCountry(iso: String?): String? {
        if (iso.isNullOrBlank()) return null
        return runCatching {
            @Suppress("DEPRECATION")
            java.util.Currency.getInstance(Locale("", iso.uppercase())).currencyCode
        }.getOrNull()
    }

    /** Country names per currency ("VND" -> "Vietnam"), so search finds a currency by country. */
    val countryNames: Map<String, List<String>> by lazy {
        val out = HashMap<String, MutableSet<String>>()
        for (iso in Locale.getISOCountries()) {
            val code = forCountry(iso) ?: continue
            @Suppress("DEPRECATION")
            val name = Locale("", iso).getDisplayCountry(Locale.getDefault())
            out.getOrPut(code) { mutableSetOf() }.add(name)
        }
        out.mapValues { it.value.sorted() }
    }

    /** Where the phone is right now, from the mobile network (no location permission needed). */
    fun detectLocal(context: Context): String? {
        val tm = context.getSystemService(TelephonyManager::class.java) ?: return null
        return forCountry(tm.networkCountryIso.takeUnless { it.isNullOrBlank() })
    }

    /** Where the user is from: the SIM's country, then the phone's language region. */
    fun detectHome(context: Context): String {
        val tm = context.getSystemService(TelephonyManager::class.java)
        return forCountry(tm?.simCountryIso) ?: forCountry(Locale.getDefault().country) ?: "USD"
    }
}
