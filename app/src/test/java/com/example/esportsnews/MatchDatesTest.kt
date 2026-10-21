package com.example.esportsnews

import org.junit.Assert.*
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class MatchDatesTest {
    private val zone = ZoneId.of("America/Sao_Paulo")

    @Test
    fun equivalentOffsetsAndFractionsUseTheSameLocalDay() {
        val utc = matchDateTime("2026-10-18T01:30:00.123Z", zone)!!
        val offset = matchDateTime("2026-10-18T04:30:00.123+03:00", zone)!!
        assertEquals(utc, offset)
        assertEquals(LocalDate.of(2026, 10, 17), utc.toLocalDate())
        assertEquals("17/10 • 22:30", formatMatchDate("2026-10-18T01:30:00.123Z", "TBD", "Invalid", Locale.ENGLISH, zone))
    }

    @Test
    fun daylightSavingUsesDeviceZoneRules() {
        val newYork = ZoneId.of("America/New_York")
        assertEquals(1, matchDateTime("2026-03-08T06:59:00Z", newYork)!!.hour)
        assertEquals(3, matchDateTime("2026-03-08T07:01:00Z", newYork)!!.hour)
    }

    @Test
    fun remoteDayBoundsUseLocalMidnightAndExclusiveNextDay() {
        val bounds = localDayBounds(LocalDate.of(2026, 10, 17), zone)!!
        assertEquals(Instant.parse("2026-10-17T03:00:00Z"), bounds.first)
        assertEquals(Instant.parse("2026-10-18T03:00:00Z"), bounds.second)
        assertEquals(LocalDate.of(2026, 10, 17), bounds.second.minusNanos(1).atZone(zone).toLocalDate())
        assertEquals(LocalDate.of(2026, 10, 18), bounds.second.atZone(zone).toLocalDate())
        val tokyo = localDayBounds(LocalDate.of(2026, 10, 17), ZoneId.of("Asia/Tokyo"))!!
        assertEquals(Instant.parse("2026-10-16T15:00:00Z"), tokyo.first)
        assertNotEquals(bounds, tokyo)
    }

    @Test
    fun remoteDayBoundsRespectShortAndLongDays() {
        val newYork = ZoneId.of("America/New_York")
        val spring = localDayBounds(LocalDate.of(2026, 3, 8), newYork)!!
        val autumn = localDayBounds(LocalDate.of(2026, 11, 1), newYork)!!
        assertEquals(23L, Duration.between(spring.first, spring.second).toHours())
        assertEquals(25L, Duration.between(autumn.first, autumn.second).toHours())
    }

    @Test
    fun remoteDayBoundsHandleMidnightGapAndRejectSkippedDay() {
        val gap = localDayBounds(LocalDate.of(2018, 11, 4), zone)!!
        assertEquals(1, gap.first.atZone(zone).hour)
        assertEquals(LocalDate.of(2018, 11, 4), gap.first.atZone(zone).toLocalDate())
        assertEquals(23L, Duration.between(gap.first, gap.second).toHours())
        assertNull(localDayBounds(LocalDate.of(2011, 12, 30), ZoneId.of("Pacific/Apia")))
    }

    @Test
    fun absentAndInvalidDatesRemainDistinct() {
        assertEquals("TBD", formatMatchDate(null, "TBD", "Invalid"))
        assertEquals("Invalid", formatMatchDate("2026-02-30T12:00:00Z", "TBD", "Invalid"))
        assertNull(matchDateTime("2026-10-18T12:00:00", zone))
        assertNull(matchDateTime("not-a-date", zone))
    }

    @Test
    fun calendarRenewsAcrossYearBoundary() {
        val before = calendarDates(LocalDate.of(2026, 12, 31))
        val after = calendarDates(LocalDate.of(2027, 1, 1))
        assertEquals(8, after.size)
        assertEquals(LocalDate.of(2026, 12, 31), after.first())
        assertEquals(before.drop(1), after.dropLast(1))
    }
}
