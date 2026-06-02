package com.example.esportsnews

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiService {
    @Headers("Cache-Control: no-cache")
    @GET("api/v1/matches")
    suspend fun getMatches(
        @Query("game") game: String?,
        @Query("status") status: String?,
        @Query("limit") limit: Int = 50,
        @Query("data_calendario") dataCalendario: String? = null
    ): List<Match>
}

object RetrofitClient {
    private val BASE_URL = if (BuildConfig.DEBUG) {
        "http://192.168.1.11:8000/"
    } else {
        "https://esports-pro-api.onrender.com/"
    }

    private val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("X-API-Key", BuildConfig.API_KEY)
            .build()
        chain.proceed(request)
    }).build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}