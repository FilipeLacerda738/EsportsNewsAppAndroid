package com.example.esportsnews

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class MatchMemoryCacheTest {
    private val queriedAt = Instant.parse("2026-10-21T12:00:00Z")
    private val query = MatchPageKey("CSGO", "finished", "A", "Aurora",
        "2026-10-21T03:00:00Z", "2026-10-22T03:00:00Z", 1)

    @Test
    fun everyEffectiveParameterIsolatesPagesAndContracts() {
        val cache = MatchMemoryCache<MatchPageKey, String>(40) { 0L }
        cache.put(query, "accepted", queriedAt)
        val otherQueries = listOf(
            query.copy(game = "VALORANT"), query.copy(status = "running"),
            query.copy(tier = "S"), query.copy(team = "Horizonte"),
            query.copy(beginAtGte = "2026-10-21T04:00:00Z"),
            query.copy(beginAtLt = "2026-10-22T04:00:00Z"),
            query.copy(page = 2), query.copy(limit = 30), query.copy(searchVersion = null)
        )
        otherQueries.forEach { assertNull(cache.get(it)) }
        assertEquals("accepted", cache.get(query)?.value)
    }

    @Test
    fun reuseDoesNotRenewFreshnessOrSuccessfulNetworkTime() {
        var now = 0L
        val cache = MatchMemoryCache<Int, String>(20) { now }
        cache.put(7, "detail", queriedAt)
        now = MATCH_FRESH_NANOS - 1
        assertTrue(cache.get(7)!!.isFresh(now))
        now = MATCH_FRESH_NANOS
        val stale = cache.get(7)!!
        assertFalse(stale.isFresh(now))
        assertEquals(queriedAt, stale.queriedAt)
        assertEquals(0L, stale.receivedNanos)
        assertNull(cache.get(8))
        now = 600_000_000_000L
        assertNull(cache.get(7))
    }

    @Test
    fun failedRevalidationStaysStaleOnReentryWithoutChangingNetworkTime() {
        val cache = MatchMemoryCache<Int, String>(20) { 0L }
        cache.put(1, "saved", queriedAt)
        cache.put(2, "other", queriedAt)
        cache.markStale { it == 1 }
        assertFalse(cache.get(1)!!.isFresh(1L))
        assertEquals("saved", cache.get(1)?.value)
        assertEquals(queriedAt, cache.get(1)?.queriedAt)
        assertTrue(cache.get(2)!!.isFresh(1L))
        cache.put(1, "updated", queriedAt.plusSeconds(1))
        assertTrue(cache.get(1)!!.isFresh(1L))
    }

    @Test
    fun leastRecentlyUsedHistoryIsBoundedAndClockRegressionIsRejected() {
        var now = 1L
        val cache = MatchMemoryCache<Int, String>(2) { now }
        cache.put(1, "one", queriedAt)
        cache.put(2, "two", queriedAt)
        cache.get(1)
        cache.put(3, "three", queriedAt)
        assertNull(cache.get(2))
        assertNotNull(cache.get(1))
        assertNotNull(cache.get(3))
        now = 0L
        assertNull(cache.get(1))
    }

    @Test
    fun revalidatingAPageDiscardsItsOldContinuationWithoutAffectingOtherQueries() {
        val cache = MatchMemoryCache<MatchPageKey, String>(40) { 0L }
        val other = query.copy(team = "Horizonte", page = 3)
        for (page in 1..3) cache.put(query.copy(page = page), "page$page", queriedAt)
        cache.put(other, "other", queriedAt)
        val refreshed = query.copy(page = 2)
        cache.removeWhere { it.copy(page = 1) == refreshed.copy(page = 1) && it.page >= refreshed.page }
        cache.put(refreshed, "new page2", queriedAt.plusSeconds(1))
        assertEquals("page1", cache.get(query)?.value)
        assertEquals("new page2", cache.get(refreshed)?.value)
        assertNull(cache.get(query.copy(page = 3)))
        assertEquals("other", cache.get(other)?.value)
    }
}
