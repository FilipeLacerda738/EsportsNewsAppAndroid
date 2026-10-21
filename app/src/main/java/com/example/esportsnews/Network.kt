package com.example.esportsnews

import com.example.esportsnews.model.*
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@Keep
data class AppVersion(
    @SerializedName("version") val version: String?,
    @SerializedName("download_url") val downloadUrl: String?,
    @SerializedName("release_notes") val releaseNotes: String?
)

@Keep
data class PaginatedMatchResponse(
    @SerializedName("total") val total: Int?,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("has_more") val hasMore: Boolean,
    @SerializedName("items") val items: List<Match>
)

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

object RetrofitClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-API-Key", BuildConfig.API_KEY)
                .build()
            chain.proceed(request)
        }).build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
