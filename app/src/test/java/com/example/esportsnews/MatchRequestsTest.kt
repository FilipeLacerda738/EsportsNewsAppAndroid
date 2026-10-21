package com.example.esportsnews

import com.example.esportsnews.model.Match
import com.google.gson.Gson
import com.google.gson.JsonParseException
import okhttp3.Headers.Companion.headersOf
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class MatchRequestsTest {
    private val gson = Gson()

    @Test
    fun filteredResponseRequiresSupportedSearchContractAndAcceptsHeaderNameCase() {
        val body = PaginatedMatchResponse(1, 1, 1, false, listOf(match(2)))
        for (name in listOf("X-Match-Search-Version", "x-match-search-version", "X-MATCH-SEARCH-VERSION")) {
            val response = Response.success(body, headersOf(name, "1"))
            assertSame(body, validateMatchResponse(response, requiresSearchContract = true))
        }
        val missing = Response.success(body)
        assertThrows(SearchUnavailableException::class.java) {
            validateMatchResponse(missing, requiresSearchContract = true)
        }
        for (version in listOf("", "0", "2", "01", "1, 2")) {
            val response = Response.success(body, headersOf("X-Match-Search-Version", version))
            assertThrows(SearchUnavailableException::class.java) {
                validateMatchResponse(response, requiresSearchContract = true)
            }
        }
        val conflicting = Response.success(body, headersOf("X-Match-Search-Version", "2", "X-Match-Search-Version", "1"))
        assertThrows(SearchUnavailableException::class.java) {
            validateMatchResponse(conflicting, requiresSearchContract = true)
        }
    }

    @Test
    fun unfilteredResponseRemainsCompatibleWithOlderApi() {
        val body = PaginatedMatchResponse(0, 1, 0, false, emptyList())
        assertSame(body, validateMatchResponse(Response.success(body), requiresSearchContract = false))
        val unknownVersion = Response.success(body, headersOf("X-Match-Search-Version", "2"))
        assertSame(body, validateMatchResponse(unknownVersion, requiresSearchContract = false))
    }

    @Test
    fun httpErrorsAndMissingBodiesAreClassifiedBeforeSearchCompatibility() {
        for (code in listOf(403, 422, 503)) {
            val response = Response.error<PaginatedMatchResponse>(code, "".toResponseBody())
            val error = assertThrows(HttpException::class.java) {
                validateMatchResponse(response, requiresSearchContract = true)
            }
            assertEquals(code, error.code())
        }
        for (required in listOf(false, true)) {
            assertThrows(JsonParseException::class.java) {
                validateMatchResponse(Response.success<PaginatedMatchResponse>(null), required)
            }
            val supportedWithoutBody = Response.success<PaginatedMatchResponse>(null, headersOf("X-Match-Search-Version", "1"))
            assertThrows(JsonParseException::class.java) { validateMatchResponse(supportedWithoutBody, required) }
        }
    }

    @Test
    fun unsupportedResponseCannotReplaceOrAppendPreviouslyAcceptedMatches() {
        val previous = listOf(match(1))
        val body = PaginatedMatchResponse(2, 2, 1, false, listOf(match(2)))
        for (append in listOf(false, true)) {
            val candidate = if (append) body else body.copy(page = 1)
            assertThrows(SearchUnavailableException::class.java) {
                val accepted = validateMatchResponse(Response.success(candidate), requiresSearchContract = true)
                mergeMatchPage(previous, accepted, candidate.page, append)
            }
            assertEquals(listOf(1), previous.map { it.id })
        }
        val supported = Response.success(body, headersOf("X-Match-Search-Version", "1"))
        val accepted = validateMatchResponse(supported, requiresSearchContract = true)
        assertEquals(listOf(1, 2), mergeMatchPage(previous, accepted, 2, true).map { it.id })
    }

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
        val response = PaginatedMatchResponse(3, 2, 2, false, listOf(match(2), match(3)))
        assertEquals(listOf(1, 2, 3), mergeMatchPage(firstPage, response, 2, true).map { it.id })
        val refresh = PaginatedMatchResponse(1, 1, 1, false, listOf(match(4)))
        assertEquals(listOf(4), mergeMatchPage(firstPage, refresh, 1, false).map { it.id })
    }

    @Test
    fun invalidPageDoesNotMutatePreviousDataAndRetryCanSucceed() {
        val previous = listOf(match(1))
        val response = PaginatedMatchResponse(2, 3, 1, false, listOf(match(2)))
        assertThrows(JsonParseException::class.java) { mergeMatchPage(previous, response, 2, true) }
        assertEquals(listOf(1), previous.map { it.id })
        assertEquals(listOf(1, 2), mergeMatchPage(previous, response.copy(page = 2), 2, true).map { it.id })
    }

    @Test
    fun emptyPageIsAcceptedButMissingOrNullItemsFail() {
        val empty = PaginatedMatchResponse(0, 1, 0, false, emptyList())
        assertTrue(mergeMatchPage(emptyList(), empty, 1, false).isEmpty())
        for (json in listOf("""{"page":1}""", """{"page":1,"items":null}""", """{"page":1,"items":[null]}""")) {
            val malformed = gson.fromJson(json, PaginatedMatchResponse::class.java)
            assertThrows(JsonParseException::class.java) { mergeMatchPage(emptyList(), malformed, 1, false) }
        }
    }

    @Test
    fun invalidMetadataCannotBePublishedAsSuccessfulResults() {
        val previous = listOf(match(1))
        val valid = PaginatedMatchResponse(2, 2, 1, false, listOf(match(2)))
        for (response in listOf(
            valid.copy(total = null), valid.copy(total = -1), valid.copy(total = 0),
            valid.copy(size = -1), valid.copy(size = 0), valid.copy(size = 2)
        )) {
            assertThrows(JsonParseException::class.java) { mergeMatchPage(previous, response, 2, true) }
            assertEquals(listOf(1), previous.map { it.id })
        }
        val missingTotal = gson.fromJson(
            """{"page":1,"size":0,"has_more":false,"items":[]}""", PaginatedMatchResponse::class.java
        )
        assertNull(missingTotal.total)
        assertThrows(JsonParseException::class.java) { mergeMatchPage(previous, missingTotal, 1, false) }
        assertEquals(listOf(1, 2), mergeMatchPage(previous, valid, 2, true).map { it.id })
    }

    @Test
    fun emptyPageWithMoreResultsIsRejectedWithoutChangingPreviousData() {
        val previous = listOf(match(1))
        val empty = PaginatedMatchResponse(2, 2, 0, true, emptyList())
        assertThrows(JsonParseException::class.java) { mergeMatchPage(previous, empty, 2, true) }
        assertEquals(listOf(1), previous.map { it.id })
        assertEquals(previous, mergeMatchPage(previous, empty.copy(hasMore = false), 2, true))
    }

    @Test
    fun changedRemoteTotalDoesNotInvalidateAlreadyLoadedPages() {
        val previous = listOf(match(1), match(2))
        val response = PaginatedMatchResponse(1, 2, 1, false, listOf(match(3)))
        assertEquals(listOf(1, 2, 3), mergeMatchPage(previous, response, 2, true).map { it.id })
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
        assertFalse(shouldPollMatches(true, "Todos", 1, false, false, queryPending = true))
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
