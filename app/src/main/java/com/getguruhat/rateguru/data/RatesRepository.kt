package com.getguruhat.rateguru.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Units of each currency per 1 US dollar. */
data class Rates(
    val perUsd: Map<String, Double>,
    /** When the source published these rates (epoch seconds). */
    val updated: Long,
    /** When this phone last downloaded them (epoch millis); 0 for the built-in snapshot. */
    val fetchedAt: Long,
) {
    fun convert(amount: Double, from: String, to: String): Double? {
        val f = perUsd[from] ?: return null
        val t = perUsd[to] ?: return null
        return amount / f * t
    }
}

/**
 * Daily rates from open.er-api.com (no key, free), with a jsDelivr mirror of
 * fawazahmed0/exchange-api as a backup. The last download is kept on the phone,
 * and a snapshot ships inside the app, so it always works offline.
 */
class RatesRepository(private val context: Context) {

    private val cacheFile = File(context.filesDir, "rates.json")

    fun loadCached(): Rates =
        runCatching { parse(cacheFile.readText()) }.getOrNull()
            ?: parse(context.assets.open("rates_seed.json").bufferedReader().use { it.readText() })

    suspend fun refresh(): Rates = withContext(Dispatchers.IO) {
        val rates = runCatching { fromErApi() }.getOrElse { fromMirror() }
        cacheFile.writeText(
            JSONObject()
                .put("updated", rates.updated)
                .put("fetchedAt", rates.fetchedAt)
                .put("rates", JSONObject(rates.perUsd))
                .toString(),
        )
        rates
    }

    private fun fromErApi(): Rates {
        val json = JSONObject(get("https://open.er-api.com/v6/latest/USD"))
        check(json.optString("result") == "success") { "Rates service error" }
        return Rates(json.getJSONObject("rates").toDoubleMap(), json.getLong("time_last_update_unix"), System.currentTimeMillis())
    }

    private fun fromMirror(): Rates {
        val json = JSONObject(get("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json"))
        // The mirror also lists crypto and metals; keep only the currencies we know.
        val known = loadCached().perUsd.keys
        val rates = json.getJSONObject("usd").toDoubleMap()
            .mapKeys { it.key.uppercase() }
            .filterKeys { it in known }
        check(rates.size > 100) { "Mirror returned too few rates" }
        return Rates(rates, System.currentTimeMillis() / 1000, System.currentTimeMillis())
    }

    private fun get(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
        }
        try {
            check(connection.responseCode == HttpURLConnection.HTTP_OK) { "HTTP ${connection.responseCode}" }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun parse(text: String): Rates {
        val json = JSONObject(text)
        return Rates(json.getJSONObject("rates").toDoubleMap(), json.getLong("updated"), json.optLong("fetchedAt", 0))
    }

    private fun JSONObject.toDoubleMap(): Map<String, Double> =
        keys().asSequence().associateWith { getDouble(it) }.filterValues { it > 0 }
}
