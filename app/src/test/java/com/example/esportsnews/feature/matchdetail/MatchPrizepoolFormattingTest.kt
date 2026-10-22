package com.example.esportsnews.feature.matchdetail

import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class MatchPrizepoolFormattingTest {
    @Test fun formatsKnownCurrencyInPortuguese() {
        val result = formatPrizepool("50000 United States Dollar", Locale.forLanguageTag("pt-BR"))
        assertTrue(result.contains("US$"))
        assertTrue(result.contains("50.000"))
        assertFalse(result.contains("Dollar"))
    }
    @Test fun preservesCurrencyAndFractionsAcrossLocales() {
        assertEquals("$50,000.25", formatPrizepool("50000.25 USD", Locale.US))
        val result = formatPrizepool("50000 EUR", Locale.forLanguageTag("es-ES"))
        assertTrue(result.contains("50.000"))
        assertTrue(result.contains("€"))
    }
    @Test fun preservesUnknownAndAmbiguousAmounts() {
        for (value in listOf("TBD", "50000 unknown currency", "00123", "50,000 USD")) {
            assertEquals(value, formatPrizepool(value, Locale.US))
        }
        assertEquals("50,000", formatPrizepool("50000", Locale.US))
    }
}
