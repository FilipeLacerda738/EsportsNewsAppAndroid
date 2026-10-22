package com.example.esportsnews.feature.matchdetail

import com.example.esportsnews.data.remote.dto.*
import java.time.*

data class MatchDetailUiState(
    val deviceZone: ZoneId = ZoneId.systemDefault(),
    val detailDataStale: Boolean = false,
    val lastSuccessfulDetailQuery: Instant? = null,
    val matchDetail: MatchDetail? = null,
    val isDetailLoading: Boolean = false,
    val isDetailRefreshing: Boolean = false,
    val detailError: Int? = null
)
