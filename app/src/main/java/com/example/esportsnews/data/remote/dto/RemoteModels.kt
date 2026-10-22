package com.example.esportsnews.data.remote.dto

import com.example.esportsnews.data.remote.dto.*
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

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
