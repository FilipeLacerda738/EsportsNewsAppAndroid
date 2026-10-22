package com.example.esportsnews.feature.matchdetail

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

internal fun formatPrizepool(value: String, locale: Locale): String {
    val match = Regex("(-?\\d+(?:\\.\\d+)?)\\s+(.+)").matchEntire(value.trim())
    if (match != null) {
        val currencyName = match.groupValues[2].trim()
        val code = when (currencyName.lowercase(Locale.ROOT)) {
            "united states dollar", "us dollar", "usd" -> "USD"
            "euro", "eur" -> "EUR"
            "brazilian real", "brl" -> "BRL"
            "pound sterling", "gbp" -> "GBP"
            else -> currencyName.takeIf { it.matches(Regex("[A-Z]{3}")) }
        }
        val currency = code?.let { runCatching { Currency.getInstance(it) }.getOrNull() }
        val amount = match.groupValues[1].toBigDecimalOrNull()
        if (currency != null && amount != null) {
            return NumberFormat.getCurrencyInstance(locale).apply {
                this.currency = currency
                minimumFractionDigits = 0
                maximumFractionDigits = maxOf(currency.defaultFractionDigits, amount.scale()).coerceAtLeast(0)
            }.format(amount)
        }
    }
    if (!value.matches(Regex("-?\\d+"))) return value
    val digits = value.removePrefix("-")
    if (digits.length > 1 && digits.startsWith('0')) return value
    val amount = value.toBigIntegerOrNull() ?: return value
    return NumberFormat.getIntegerInstance(locale).format(amount)
}
