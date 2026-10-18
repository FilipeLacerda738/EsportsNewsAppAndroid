package com.example.esportsnews

import com.example.esportsnews.model.Match
import com.example.esportsnews.model.MatchDetail
import com.google.gson.JsonParseException

// A screen owns a session; refreshing creates a request without changing that ownership.
internal class DetailRequestSession {
    var session: Long = 0L
        private set
    var matchId: Int? = null
        private set
    private var request: Long = 0L

    fun open(id: Int): Long {
        ++session
        ++request
        matchId = id
        return session
    }

    fun nextRequest(): Long = ++request

    fun owns(expectedSession: Long): Boolean = matchId != null && session == expectedSession

    fun accepts(expectedSession: Long, expectedRequest: Long): Boolean =
        owns(expectedSession) && request == expectedRequest

    fun close(expectedSession: Long): Boolean {
        if (!owns(expectedSession)) return false
        ++session
        ++request
        matchId = null
        return true
    }
}

internal fun mergeMatchPage(
    previous: List<Match>,
    response: PaginatedMatchResponse,
    requestedPage: Int,
    append: Boolean
): List<Match> {
    if (response.page != requestedPage) throw JsonParseException("Unexpected match page")
    val items: List<Match?>? = response.items
    if (items == null || items.any { it == null || it.status.isNullOrBlank() || it.game.isNullOrBlank() }) {
        throw JsonParseException("Invalid match items")
    }
    return (if (append) previous + response.items else response.items).distinctBy { it.id }
}

internal fun shouldPollMatches(
    active: Boolean, status: String, page: Int, requestActive: Boolean, hasError: Boolean
): Boolean = active && status in listOf("Todos", "Ao Vivo") && page == 1 && !requestActive && !hasError

internal fun shouldPollDetail(
    active: Boolean, status: String?, requestActive: Boolean, hasError: Boolean
): Boolean = active && status == "running" && !requestActive && !hasError

internal fun validateMatchDetail(detail: MatchDetail, expectedId: Int): MatchDetail {
    val invalidTeam = listOfNotNull(detail.teamA, detail.teamB).any { team ->
        team.name.isNullOrBlank() || team.players.orEmpty().filterNotNull().any { it.name.isNullOrBlank() }
    }
    val invalidMap = detail.games.orEmpty().filterNotNull().any { it.status.isNullOrBlank() || it.position <= 0 }
    if (detail.id != expectedId || detail.status.isNullOrBlank() || detail.game.isNullOrBlank() || invalidTeam || invalidMap) {
        throw JsonParseException("Invalid match details")
    }
    return detail
}
