package com.example.esportsnews

import java.text.NumberFormat
import java.util.Locale

internal fun formatPrizepool(value: String, locale: Locale): String {
    if (!value.matches(Regex("-?\\d+"))) return value
    val digits = value.removePrefix("-")
    if (digits.length > 1 && digits.startsWith('0')) return value

    val amount = value.toBigIntegerOrNull() ?: return value
    return NumberFormat.getIntegerInstance(locale).format(amount)
}
