package com.example.esportsnews.testsupport

import com.example.esportsnews.data.remote.ApiService
import com.example.esportsnews.data.remote.dto.AppVersion
import com.example.esportsnews.data.remote.dto.PaginatedMatchResponse
import com.example.esportsnews.data.remote.dto.Match
import com.example.esportsnews.data.remote.dto.MatchDetail
import okhttp3.Headers
import retrofit2.Response

internal data class ListRequest(val page: Int, val game: String?, val status: String?, val team: String?)

internal class FakeApi : ApiService {
    val lists = mutableListOf<ListRequest>()
    val details = mutableListOf<Int>()
    var updates = 0
    var list: suspend (ListRequest) -> Response<PaginatedMatchResponse> = { page(it.page, it.page) }
    var detail: suspend (Int) -> MatchDetail = { com.example.esportsnews.testsupport.detail(it) }
    var update: suspend () -> Response<AppVersion> = { Response.success(AppVersion("2.0.0", "https://example.com/app.apk", "Release")) }

    override suspend fun getMatches(page: Int, game: String?, status: String?, limit: Int,
        dataCalendario: String?, tier: String?, teamQuery: String?, beginAtGte: String?,
        beginAtLt: String?): Response<PaginatedMatchResponse> {
        val request = ListRequest(page, game, status, teamQuery)
        lists += request
        return list(request)
    }
    override suspend fun getMatchDetails(matchId: Int): MatchDetail {
        details += matchId
        return detail(matchId)
    }
    override suspend fun getAppVersion(): Response<AppVersion> { updates++; return update() }
}

internal fun page(page: Int, id: Int, more: Boolean = false): Response<PaginatedMatchResponse> =
    Response.success(PaginatedMatchResponse(10, page, 1, more, listOf(
        Match(id, id, "CSGO", "running", 0, 0, null, null,
            team_a = null, team_b = null, league = null)
    )), Headers.headersOf("X-Match-Search-Version", "1"))

internal fun detail(id: Int) = MatchDetail(id, "CSGO", "running", null, null, 0, 0, null,
    league = null, teamA = null, teamB = null, games = null)
