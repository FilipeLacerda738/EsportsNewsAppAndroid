package com.example.esportsnews.data.repository

import com.example.esportsnews.data.remote.ApiService
import com.example.esportsnews.data.remote.RetrofitClient

class MatchesRepository(private val api: ApiService = RetrofitClient.api) {
    suspend fun getMatches(page: Int, game: String?, status: String?, limit: Int = 15,
        tier: String? = null, teamQuery: String? = null, beginAtGte: String? = null,
        beginAtLt: String? = null) = api.getMatches(page = page, game = game, status = status,
            limit = limit, tier = tier, teamQuery = teamQuery, beginAtGte = beginAtGte, beginAtLt = beginAtLt)
    suspend fun getMatchDetails(matchId: Int) = api.getMatchDetails(matchId)
}
