package com.example.esportsnews.data.repository

import com.example.esportsnews.data.remote.ApiService
import com.example.esportsnews.data.remote.RetrofitClient

class AppUpdatesRepository(private val api: ApiService = RetrofitClient.api) {
    suspend fun getAppVersion() = api.getAppVersion()
}
