package com.example.esportsnews

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

internal fun matchDateTime(value: String?, zone: ZoneId = ZoneId.systemDefault()): ZonedDateTime? {
    if (value.isNullOrBlank()) return null
    return try {
        OffsetDateTime.parse(value).atZoneSameInstant(zone)
    } catch (_: DateTimeParseException) {
        null
    }
}

internal fun calendarDates(today: LocalDate): List<LocalDate> =
    List(8) { today.minusDays(1).plusDays(it.toLong()) }

fun formatMatchDate(
    dateString: String?,
    tbdString: String,
    undefinedString: String,
    locale: Locale = Locale.getDefault(),
    zone: ZoneId = ZoneId.systemDefault()
): String {
    if (dateString.isNullOrBlank()) return tbdString
    return matchDateTime(dateString, zone)?.format(DateTimeFormatter.ofPattern("dd/MM • HH:mm", locale))
        ?: undefinedString
}
