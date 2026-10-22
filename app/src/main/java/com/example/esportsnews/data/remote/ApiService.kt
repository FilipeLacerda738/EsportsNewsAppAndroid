package com.example.esportsnews.data.remote

import com.example.esportsnews.data.remote.dto.AppVersion
import com.example.esportsnews.data.remote.dto.PaginatedMatchResponse

import com.example.esportsnews.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @Headers("Cache-Control: no-cache")
    @GET("api/v1/matches/")
    suspend fun getMatches(
        @Query("page") page: Int,
        @Query("game") game: String?,
        @Query("status") status: String?,
        @Query("limit") limit: Int = 15,
        @Query("data_calendario") dataCalendario: String? = null,
        @Query("tier") tier: String? = null,
        @Query("team_query") teamQuery: String? = null,
        @Query("begin_at_gte") beginAtGte: String? = null,
        @Query("begin_at_lt") beginAtLt: String? = null
    ): Response<PaginatedMatchResponse>

    @Headers("Cache-Control: no-cache")
    @GET("api/v1/matches/{match_id}/details")
    suspend fun getMatchDetails(
        @Path("match_id") matchId: Int
    ): MatchDetail

    @Headers("Cache-Control: no-cache")
    @GET("api/v1/system/app-version")
    suspend fun getAppVersion(): Response<AppVersion>
}
