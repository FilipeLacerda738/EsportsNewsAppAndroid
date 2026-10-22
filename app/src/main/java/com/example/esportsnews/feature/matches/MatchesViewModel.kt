package com.example.esportsnews.feature.matches

import com.example.esportsnews.data.repository.mergeMatchPage

import com.example.esportsnews.data.repository.validateMatchResponse

import com.example.esportsnews.core.network.SearchUnavailableException

import com.example.esportsnews.R
import com.example.esportsnews.core.network.requestErrorMessage
import com.example.esportsnews.core.time.calendarDates
import com.example.esportsnews.core.time.localDayBounds
import com.example.esportsnews.data.cache.MatchMemoryCache
import com.example.esportsnews.data.cache.MatchPageKey
import com.example.esportsnews.data.cache.RecentMatchData
import com.example.esportsnews.data.remote.dto.PaginatedMatchResponse

import com.example.esportsnews.data.repository.MatchesRepository
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esportsnews.data.remote.dto.*
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MatchesViewModel(
    private val repository: MatchesRepository = MatchesRepository(),
    private val nowNanos: () -> Long = SystemClock::elapsedRealtimeNanos,
    private val now: () -> Instant = Instant::now
) : ViewModel() {
    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    private var calendarJob: Job? = null
    private var freshnessJob: Job? = null

    private val pageCache = MatchMemoryCache<MatchPageKey, PaginatedMatchResponse>(40, nowNanos)
    private val activePages = mutableMapOf<Int, RecentMatchData<PaginatedMatchResponse>>()
    private var allFetchedMatches = mutableListOf<Match>()
    private var currentPage = 1

    private var pollingJob: Job? = null
    private var listJob: Job? = null
    private var searchJob: Job? = null
    private var searchPending = false
    private var listGeneration = 0L
    private var listRequestActive = false
    private var listRequestIsPolling = false
    private var listActive = false
    private var retryNextPage = false
    init {
        fetchMatches()
    }

    private fun manageFreshness() {
        if (!listActive) {
            freshnessJob?.cancel()
            freshnessJob = null
            return
        }
        if (freshnessJob?.isActive == true) return
        freshnessJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                updateFreshness()
                pageCache.prune()
            }
        }
    }

    private fun updateFreshness() {
        _uiState.update {
            it.copy(
                listDataStale = activePages.isNotEmpty() && (_uiState.value.error != null || activePages.values.any { !it.isFresh(nowNanos()) })
            )
        }
    }

    fun fetchMatches(isRefresh: Boolean = false, loadNextPage: Boolean = false) {
        requestMatches(isRefresh, loadNextPage, forceNetwork = isRefresh)
    }

    private fun requestMatches(
        isRefresh: Boolean, loadNextPage: Boolean, polling: Boolean = false, forceNetwork: Boolean = false
    ) {
        if (searchPending) return
        if (loadNextPage && (!_uiState.value.hasMore || listRequestActive)) return
        if (listRequestActive && (!isRefresh || (!listRequestIsPolling && !_uiState.value.isLoadingNextPage))) return
        val generation = ++listGeneration
        listJob?.cancel()
        val requestedPage = if (loadNextPage) currentPage + 1 else 1
        val apiStatus = _uiState.value.selectedStatus.apiValue
        val apiGame = _uiState.value.selectedGame.apiValue
        val apiTier = _uiState.value.selectedTier.apiValue
        val apiTeamQuery = _uiState.value.searchQuery.trim().takeIf { it.isNotEmpty() }
        val date = _uiState.value.selectedDate
        val bounds = date?.let { localDayBounds(it, _uiState.value.deviceZone) }
        if (date != null && bounds == null) {
            listRequestActive = false
            listRequestIsPolling = false
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingNextPage = false,
                    hasMore = false
                )
            }
            retryNextPage = false
            _uiState.update { it.copy(error = R.string.error_invalid_calendar_date) }
            return
        }
        val key = MatchPageKey(apiGame, apiStatus, apiTier, apiTeamQuery,
            bounds?.first?.toString(), bounds?.second?.toString(), requestedPage)
        if (!forceNetwork && !polling) {
            val cached = pageCache.get(key)
            if (cached != null && (cached.isFresh(nowNanos()) || !loadNextPage)) {
                acceptMatchPage(cached, requestedPage, loadNextPage)
                _uiState.update { it.copy(error = null, isLoading = false) }
                updateFreshness()
                if (cached.isFresh(nowNanos())) return
            }
        }
        listRequestActive = true
        listRequestIsPolling = polling
        _uiState.update {
            it.copy(
                isLoading = activePages.isEmpty() && !isRefresh && !loadNextPage && !polling,
                isRefreshing = (isRefresh || (activePages.isNotEmpty() && !loadNextPage)) && !polling,
                isLoadingNextPage = loadNextPage,
                error = null
            )
        }
        listJob = viewModelScope.launch {
            try {
                val response = repository.getMatches(
                    page = requestedPage,
                    limit = key.limit,
                    game = apiGame,
                    status = apiStatus,
                    tier = apiTier,
                    teamQuery = apiTeamQuery,
                    beginAtGte = bounds?.first?.toString(),
                    beginAtLt = bounds?.second?.toString()
                )

                if (generation != listGeneration) return@launch
                val result = validateMatchResponse(response, apiTeamQuery != null || bounds != null)
                // Validate pagination before caching; an obsolete or incompatible response never enters history.
                mergeMatchPage(emptyList(), result, requestedPage, append = false)
                pageCache.removeWhere {
                    it.copy(page = 1) == key.copy(page = 1) && it.page >= requestedPage
                }
                val accepted = pageCache.put(key, result, now())
                acceptMatchPage(accepted, requestedPage, loadNextPage)
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                reportListError(generation, loadNextPage, key, e)
            } catch (e: HttpException) {
                reportListError(generation, loadNextPage, key, e)
            } catch (e: JsonParseException) {
                reportListError(generation, loadNextPage, key, e)
            } catch (e: SearchUnavailableException) {
                reportListError(generation, loadNextPage, key, e)
            } finally {
                if (generation == listGeneration) {
                    listRequestActive = false
                    listRequestIsPolling = false
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, isLoadingNextPage = false) }
                }
            }
        }
    }

    private fun acceptMatchPage(
        entry: RecentMatchData<PaginatedMatchResponse>, requestedPage: Int, append: Boolean
    ) {
        allFetchedMatches = mergeMatchPage(allFetchedMatches, entry.value, requestedPage, append).toMutableList()
        if (!append) activePages.clear()
        activePages[requestedPage] = entry
        currentPage = requestedPage
        _uiState.update {
            it.copy(
                hasMore = entry.value.hasMore,
                matches = allFetchedMatches.toList(),
                loadedMatchesCount = allFetchedMatches.size,
                totalMatches = entry.value.total,
                lastSuccessfulQuery = entry.queriedAt
            )
        }
        retryNextPage = false
        updateFreshness()
    }

    private fun reportListError(generation: Long, loadNextPage: Boolean, key: MatchPageKey, error: Exception) {
        if (generation == listGeneration) {
            retryNextPage = loadNextPage
            _uiState.update { it.copy(error = requestErrorMessage(error)) }
            activePages.replaceAll { _, entry -> entry.copy(revalidationFailed = true) }
            pageCache.markStale { it.copy(page = 1) == key.copy(page = 1) }
            updateFreshness()
        }
    }

    fun retryMatches() {
        if (_uiState.value.error != null && !listRequestActive) {
            requestMatches(isRefresh = !retryNextPage, loadNextPage = retryNextPage, forceNetwork = true)
        }
    }

    fun loadNextPage() {
        if (!listActive) return
        fetchMatches(loadNextPage = true)
    }

    fun onGameFilterSelected(game: GameFilter) {
        if (_uiState.value.selectedGame == game) return
        _uiState.update { it.copy(selectedGame = game) }
        reloadFilters()
    }

    fun onStatusFilterSelected(status: StatusFilter) {
        if (_uiState.value.selectedStatus == status) return
        _uiState.update { it.copy(selectedStatus = status) }
        if (status == StatusFilter.LIVE) {
            _uiState.update { it.copy(selectedDate = null) }
        }
        reloadFilters()
        managePolling()
    }

    fun onTierFilterSelected(tier: TierFilter) {
        if (_uiState.value.selectedTier == tier) return
        _uiState.update { it.copy(selectedTier = tier) }
        reloadFilters()
    }

    private fun reloadFilters(debounce: Boolean = false) {
        ++listGeneration
        searchJob?.cancel()
        listJob?.cancel()
        listRequestActive = false
        listRequestIsPolling = false
        searchPending = debounce
        allFetchedMatches.clear()
        activePages.clear()
        _uiState.update { it.copy(listDataStale = false) }
        currentPage = 1
        _uiState.update { it.copy(hasMore = true) }
        retryNextPage = false
        _uiState.update {
            it.copy(
                matches = emptyList(),
                totalMatches = null,
                loadedMatchesCount = 0,
                lastSuccessfulQuery = null,
                error = null,
                isLoading = true,
                isRefreshing = false,
                isLoadingNextPage = false
            )
        }
        if (debounce) {
            val generation = listGeneration
            searchJob = viewModelScope.launch {
                delay(350)
                if (generation == listGeneration) {
                    searchPending = false
                    fetchMatches()
                }
            }
        } else {
            fetchMatches()
        }
    }

    fun onDateSelected(date: LocalDate?) {
        val changeStatus = _uiState.value.selectedStatus == StatusFilter.LIVE && date != null
        if (changeStatus) {
            _uiState.update { it.copy(selectedStatus = StatusFilter.ALL) }
            managePolling()
        }
        _uiState.update { it.copy(selectedDate = if (_uiState.value.selectedDate == date) null else date) }
        reloadFilters()
    }

    fun onSearchQueryChanged(query: String) {
        if (_uiState.value.searchQuery == query) return
        _uiState.update { it.copy(searchQuery = query) }
        reloadFilters(debounce = query.isNotBlank())
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                selectedGame = GameFilter.ALL,
                selectedStatus = StatusFilter.ALL,
                selectedTier = TierFilter.ALL,
                searchQuery = "",
                selectedDate = null
            )
        }
        reloadFilters()
        managePolling()
    }

    fun selectToday() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        _uiState.update { it.copy(deviceZone = zone, dateList = calendarDates(today), selectedDate = today) }
        if (_uiState.value.selectedStatus == StatusFilter.LIVE) { _uiState.update { it.copy(selectedStatus = StatusFilter.ALL) } }
        reloadFilters()
        managePolling()
    }

    fun setListActive(active: Boolean) {
        if (listActive == active) return
        listActive = active
        manageFreshness()
        calendarJob?.cancel()
        if (active) {
            refreshCalendar()
            updateFreshness()
            if (_uiState.value.listDataStale && !listRequestActive && _uiState.value.error == null && !searchPending) {
                requestMatches(isRefresh = true, loadNextPage = false, forceNetwork = true)
            }
            calendarJob = viewModelScope.launch {
                while (isActive) {
                    delay(60000)
                    refreshCalendar()
                }
            }
        }
        if (!active && listRequestActive && listRequestIsPolling) {
            ++listGeneration
            listJob?.cancel()
            listRequestActive = false
            listRequestIsPolling = false
        }
        managePolling()
    }

    private fun refreshCalendar() {
        val zone = ZoneId.systemDefault()
        val days = calendarDates(LocalDate.now(zone))
        if (_uiState.value.dateList != days || _uiState.value.deviceZone != zone) {
            val previousDate = _uiState.value.selectedDate
            val zoneChanged = _uiState.value.deviceZone != zone
            _uiState.update { it.copy(dateList = days, deviceZone = zone) }
            if (_uiState.value.selectedDate !in days) { _uiState.update { it.copy(selectedDate = null) } }
            if (previousDate != _uiState.value.selectedDate || (zoneChanged && previousDate != null)) {
                reloadFilters()
            }
        }
    }

    private fun managePolling() {
        pollingJob?.cancel()
        if (listActive && (_uiState.value.selectedStatus == StatusFilter.LIVE || _uiState.value.selectedStatus == StatusFilter.ALL)) {
            pollingJob = viewModelScope.launch {
                while (isActive) {
                    delay(30000)
                    if (shouldPollMatches(listActive, _uiState.value.selectedStatus, currentPage, listRequestActive, _uiState.value.error != null, searchPending)) {
                        requestMatches(isRefresh = false, loadNextPage = false, polling = true)
                    }
                }
            }
        }
    }
}
