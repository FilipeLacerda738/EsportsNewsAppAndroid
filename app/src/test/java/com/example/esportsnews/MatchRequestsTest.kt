package com.example.esportsnews

import com.example.esportsnews.model.Match
import com.google.gson.Gson
import com.google.gson.JsonParseException
import org.junit.Assert.*
import org.junit.Test

class MatchRequestsTest {
    private val gson = Gson()

    @Test
    fun refreshingDoesNotChangeScreenOwnership() {
        val gate = DetailRequestSession()
        val owner = gate.open(10)
        val initial = gate.nextRequest()
        val refresh = gate.nextRequest()
        assertFalse(gate.accepts(owner, initial))
        assertTrue(gate.accepts(owner, refresh))
        assertTrue(gate.close(owner))
        assertFalse(gate.accepts(owner, refresh))
    }

    @Test
    fun oldResponseAndDisposalCannotAffectReenteredDetail() {
        val gate = DetailRequestSession()
        val oldOwner = gate.open(10)
        val oldRequest = gate.nextRequest()
        val newOwner = gate.open(10)
        val newRequest = gate.nextRequest()
        assertFalse(gate.close(oldOwner))
        assertFalse(gate.accepts(oldOwner, oldRequest))
        assertTrue(gate.accepts(newOwner, newRequest))
        assertEquals(10, gate.matchId)
    }

    @Test
    fun cancellingPollingInvalidatesItsFinallyWithoutLosingSession() {
        val gate = DetailRequestSession()
        val owner = gate.open(10)
        val pollingRequest = gate.nextRequest()
        gate.nextRequest()
        assertFalse(gate.accepts(owner, pollingRequest))
        assertTrue(gate.owns(owner))
        val resumed = gate.nextRequest()
        assertTrue(gate.accepts(owner, resumed))
    }

    @Test
    fun switchingMatchesRejectsPreviousRequests() {
        val gate = DetailRequestSession()
        val first = gate.open(10)
        val request = gate.nextRequest()
        gate.open(20)
        assertFalse(gate.accepts(first, request))
        assertEquals(20, gate.matchId)
    }

    @Test
    fun paginationDeduplicatesAndRefreshReplacesPages() {
        val firstPage = listOf(match(1), match(2))
        val response = PaginatedMatchResponse(3, 2, 15, false, listOf(match(2), match(3)))
        assertEquals(listOf(1, 2, 3), mergeMatchPage(firstPage, response, 2, true).map { it.id })
        val refresh = PaginatedMatchResponse(1, 1, 15, false, listOf(match(4)))
        assertEquals(listOf(4), mergeMatchPage(firstPage, refresh, 1, false).map { it.id })
    }

    @Test
    fun invalidPageDoesNotMutatePreviousDataAndRetryCanSucceed() {
        val previous = listOf(match(1))
        val response = PaginatedMatchResponse(2, 3, 15, false, listOf(match(2)))
        assertThrows(JsonParseException::class.java) { mergeMatchPage(previous, response, 2, true) }
        assertEquals(listOf(1), previous.map { it.id })
        assertEquals(listOf(1, 2), mergeMatchPage(previous, response.copy(page = 2), 2, true).map { it.id })
    }

    @Test
    fun emptyPageIsAcceptedButMissingOrNullItemsFail() {
        val empty = PaginatedMatchResponse(0, 1, 15, false, emptyList())
        assertTrue(mergeMatchPage(emptyList(), empty, 1, false).isEmpty())
        for (json in listOf("""{"page":1}""", """{"page":1,"items":null}""", """{"page":1,"items":[null]}""")) {
            val malformed = gson.fromJson(json, PaginatedMatchResponse::class.java)
            assertThrows(JsonParseException::class.java) { mergeMatchPage(emptyList(), malformed, 1, false) }
        }
    }

    @Test
    fun listPollingRequiresVisibleEligibleFirstPageWithoutErrorOrRequest() {
        assertTrue(shouldPollMatches(true, "Todos", 1, false, false))
        assertTrue(shouldPollMatches(true, "Ao Vivo", 1, false, false))
        assertFalse(shouldPollMatches(false, "Todos", 1, false, false))
        assertFalse(shouldPollMatches(true, "Finalizados", 1, false, false))
        assertFalse(shouldPollMatches(true, "Em breve", 1, false, false))
        assertFalse(shouldPollMatches(true, "Todos", 2, false, false))
        assertFalse(shouldPollMatches(true, "Todos", 1, true, false))
        assertFalse(shouldPollMatches(true, "Todos", 1, false, true))
    }

    @Test
    fun detailPollingOnlyRunsForVisibleLiveMatchWithoutErrorOrRequest() {
        assertTrue(shouldPollDetail(true, "running", false, false))
        assertFalse(shouldPollDetail(false, "running", false, false))
        assertFalse(shouldPollDetail(true, "not_started", false, false))
        assertFalse(shouldPollDetail(true, "finished", false, false))
        assertFalse(shouldPollDetail(true, null, false, false))
        assertFalse(shouldPollDetail(true, "running", true, false))
        assertFalse(shouldPollDetail(true, "running", false, true))
    }

    private fun match(id: Int): Match = gson.fromJson(
        """{"id":$id,"game":"CSGO","status":"running"}""", Match::class.java
    )
}
