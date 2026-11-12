package com.jizhang.xiaomeng.ui.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Currency(
    val code: String,
    val name: String,
    val symbol: String
)

object CurrencyUtils {
    val CNY = Currency("CNY", "人民币", "¥")

    val supported = listOf(
        CNY,
        Currency("USD", "美元", "$"),
        Currency("EUR", "欧元", "€"),
        Currency("JPY", "日元", "¥"),
        Currency("HKD", "港币", "HK$"),
        Currency("GBP", "英镑", "£"),
        Currency("KRW", "韩元", "₩"),
        Currency("SGD", "新加坡元", "S$"),
        Currency("THB", "泰铢", "฿"),
        Currency("AUD", "澳元", "A$")
    )

    fun findByCode(code: String): Currency =
        supported.firstOrNull { it.code == code } ?: CNY
}

/**
 * 使用 Frankfurter（欧洲央行 ECB 参考汇率，免 key）获取以 CNY 为基准的汇率。
 * 返回值为「1 CNY 可兑换的外币数量」，例如 rates["USD"] = 0.139 表示 1 CNY ≈ 0.139 USD。
 */
object ExchangeRateService {
    private const val BASE_URL = "https://api.frankfurter.app/latest?from=CNY&to="

    suspend fun fetchRates(): Map<String, Double>? = withContext(Dispatchers.IO) {
        val targets = CurrencyUtils.supported
            .filter { it.code != "CNY" }
            .joinToString(",") { it.code }
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(BASE_URL + targets).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val rates = JSONObject(body).getJSONObject("rates")
            val result = mutableMapOf("CNY" to 1.0)
            CurrencyUtils.supported.forEach { currency ->
                if (currency.code != "CNY" && rates.has(currency.code)) {
                    result[currency.code] = rates.getDouble(currency.code)
                }
            }
            result
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
