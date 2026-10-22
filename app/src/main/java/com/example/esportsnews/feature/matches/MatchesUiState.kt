package com.example.esportsnews.feature.matches

import com.example.esportsnews.core.time.calendarDates

import com.example.esportsnews.data.remote.dto.*
import java.time.*

data class MatchesUiState(
    val matches: List<Match> = emptyList(),
    val totalMatches: Int? = null,
    val loadedMatchesCount: Int = 0,
    val lastSuccessfulQuery: Instant? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val error: Int? = null,
    val selectedGame: GameFilter = GameFilter.ALL,
    val selectedStatus: StatusFilter = StatusFilter.ALL,
    val selectedTier: TierFilter = TierFilter.ALL,
    val searchQuery: String = "",
    val selectedDate: LocalDate? = null,
    val dateList: List<LocalDate> = calendarDates(LocalDate.now()),
    val deviceZone: ZoneId = ZoneId.systemDefault(),
    val listDataStale: Boolean = false,
    val hasMore: Boolean = true
)
