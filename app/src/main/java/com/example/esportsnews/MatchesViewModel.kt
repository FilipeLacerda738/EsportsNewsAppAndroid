package com.example.esportsnews

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esportsnews.model.*
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MatchesViewModel(application: Application) : AndroidViewModel(application) {

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches

    private val _totalMatches = MutableStateFlow<Int?>(null)
    val totalMatches: StateFlow<Int?> = _totalMatches
    private val _loadedMatchesCount = MutableStateFlow(0)
    val loadedMatchesCount: StateFlow<Int> = _loadedMatchesCount
    private val _lastSuccessfulQuery = MutableStateFlow<Instant?>(null)
    val lastSuccessfulQuery: StateFlow<Instant?> = _lastSuccessfulQuery

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _isLoadingNextPage = MutableStateFlow(false)
    val isLoadingNextPage: StateFlow<Boolean> = _isLoadingNextPage

    private val _error = MutableStateFlow<Int?>(null)
    val error: StateFlow<Int?> = _error
    private val _selectedGame = MutableStateFlow<String>("Todos")
    val selectedGame: StateFlow<String> = _selectedGame

    private val _selectedStatus = MutableStateFlow<String>("Todos")
    val selectedStatus: StateFlow<String> = _selectedStatus

    private val _selectedTier = MutableStateFlow<String>("Todos")
    val selectedTier: StateFlow<String> = _selectedTier

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    private val _dateList = MutableStateFlow(calendarDates(LocalDate.now()))
    val dateList: StateFlow<List<LocalDate>> = _dateList
    private val _deviceZone = MutableStateFlow(ZoneId.systemDefault())
    val deviceZone: StateFlow<ZoneId> = _deviceZone
    private var calendarJob: Job? = null
    private var freshnessJob: Job? = null

    private val pageCache = MatchMemoryCache<MatchPageKey, PaginatedMatchResponse>(40, SystemClock::elapsedRealtimeNanos)
    private val detailCache = MatchMemoryCache<Int, MatchDetail>(20, SystemClock::elapsedRealtimeNanos)
    private val activePages = mutableMapOf<Int, RecentMatchData<PaginatedMatchResponse>>()
    private var activeDetailData: RecentMatchData<MatchDetail>? = null
    private val _listDataStale = MutableStateFlow(false)
    val listDataStale: StateFlow<Boolean> = _listDataStale
    private val _detailDataStale = MutableStateFlow(false)
    val detailDataStale: StateFlow<Boolean> = _detailDataStale
    private val _lastSuccessfulDetailQuery = MutableStateFlow<Instant?>(null)
    val lastSuccessfulDetailQuery: StateFlow<Instant?> = _lastSuccessfulDetailQuery

    private var allFetchedMatches = mutableListOf<Match>()
    private var currentPage = 1
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore

    private var pollingJob: Job? = null
    private var listJob: Job? = null
    private var searchJob: Job? = null
    private var searchPending = false
    private var listGeneration = 0L
    private var listRequestActive = false
    private var listRequestIsPolling = false
    private var listActive = false
    private var retryNextPage = false
    private var detailJob: Job? = null
    private val detailRequests = DetailRequestSession()
    private var detailPollingJob: Job? = null
    private var detailActive = false
    private var detailRequestActive = false
    private var detailRequestIsPolling = false

    private val _matchDetail = MutableStateFlow<MatchDetail?>(null)
    val matchDetail: StateFlow<MatchDetail?> = _matchDetail

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading

    private val _isDetailRefreshing = MutableStateFlow(false)
    val isDetailRefreshing: StateFlow<Boolean> = _isDetailRefreshing

    private val _detailError = MutableStateFlow<Int?>(null)
    val detailError: StateFlow<Int?> = _detailError

    private val _updateAvailable = MutableStateFlow<AppVersion?>(null)
    val updateAvailable: StateFlow<AppVersion?> = _updateAvailable

    private val _updateCheckState = MutableStateFlow(UpdateCheckState.Idle)
    internal val updateCheckState: StateFlow<UpdateCheckState> = _updateCheckState

    init {
        fetchMatches()
        checkForUpdates()
    }

    private fun manageFreshness() {
        if (!listActive && !detailActive) {
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
                detailCache.prune()
            }
        }
    }

    private fun updateFreshness() {
        _listDataStale.value = activePages.isNotEmpty() &&
            (_error.value != null || activePages.values.any { !it.isFresh(SystemClock.elapsedRealtimeNanos()) })
        _detailDataStale.value = activeDetailData?.let { !it.isFresh(SystemClock.elapsedRealtimeNanos()) || _detailError.value != null } ?: false
    }

    fun checkForUpdates() {
        if (_updateCheckState.value == UpdateCheckState.Checking) return
        _updateCheckState.value = UpdateCheckState.Checking
        _updateAvailable.value = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getAppVersion()
                val candidate = response.body()
                val result = if (response.isSuccessful) {
                    evaluateAppUpdate(BuildConfig.VERSION_NAME, candidate)
                } else {
                    UpdateCheckState.Failed
                }
                _updateCheckState.value = result
                if (result == UpdateCheckState.Available) {
                    _updateAvailable.value = candidate
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: IOException) {
                // The optional update check must not block matches.
                _updateCheckState.value = UpdateCheckState.Failed
            } catch (_: HttpException) {
                _updateCheckState.value = UpdateCheckState.Failed
            } catch (_: JsonParseException) {
                _updateCheckState.value = UpdateCheckState.Failed
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateAvailable.value = null
    }

    fun fetchMatchDetails(matchId: Int): Long {
        val session = detailRequests.open(matchId)
        detailPollingJob?.cancel()
        detailJob?.cancel()
        detailActive = false
        manageFreshness()
        detailRequestActive = false
        detailRequestIsPolling = false
        activeDetailData = detailCache.get(matchId)
        _matchDetail.value = activeDetailData?.value
        _lastSuccessfulDetailQuery.value = activeDetailData?.queriedAt
        _detailError.value = null
        _isDetailLoading.value = false
        _isDetailRefreshing.value = false
        updateFreshness()
        if (activeDetailData?.isFresh(SystemClock.elapsedRealtimeNanos()) != true) requestMatchDetails(session)
        return session
    }

    fun refreshMatchDetails(expectedSession: Long) {
        if (detailRequests.owns(expectedSession) && (!detailRequestActive || detailRequestIsPolling)) {
            requestMatchDetails(expectedSession)
        }
    }

    private fun requestMatchDetails(session: Long, polling: Boolean = false) {
        val matchId = detailRequests.matchId ?: return
        val request = detailRequests.nextRequest()
        detailJob?.cancel()
        detailRequestActive = true
        detailRequestIsPolling = polling
        _isDetailLoading.value = _matchDetail.value == null
        _isDetailRefreshing.value = _matchDetail.value != null && !polling
        _detailError.value = null
        detailJob = viewModelScope.launch {
            try {
                val detail = RetrofitClient.api.getMatchDetails(matchId)
                if (!detailRequests.accepts(session, request)) return@launch
                val accepted = validateMatchDetail(detail, matchId)
                activeDetailData = detailCache.put(matchId, accepted, Instant.now())
                _matchDetail.value = accepted
                _lastSuccessfulDetailQuery.value = activeDetailData?.queriedAt
                updateFreshness()
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                reportDetailError(session, request, e)
            } catch (e: HttpException) {
                reportDetailError(session, request, e)
            } catch (e: JsonParseException) {
                reportDetailError(session, request, e)
            } finally {
                if (detailRequests.accepts(session, request)) {
                    detailRequestActive = false
                    detailRequestIsPolling = false
                    _isDetailLoading.value = false
                    _isDetailRefreshing.value = false
                }
            }
        }
    }

    private fun reportDetailError(session: Long, request: Long, error: Exception) {
        if (detailRequests.accepts(session, request)) {
            _detailError.value = requestErrorMessage(error)
            activeDetailData = activeDetailData?.copy(revalidationFailed = true)
            detailCache.markStale { it == detailRequests.matchId }
            updateFreshness()
        }
    }

    fun clearMatchDetails(expectedSession: Long) {
        if (!detailRequests.close(expectedSession)) return
        detailPollingJob?.cancel()
        detailJob?.cancel()
        detailActive = false
        manageFreshness()
        detailRequestActive = false
        detailRequestIsPolling = false
        _matchDetail.value = null
        activeDetailData = null
        _lastSuccessfulDetailQuery.value = null
        _detailDataStale.value = false
        _detailError.value = null
        _isDetailLoading.value = false
        _isDetailRefreshing.value = false
    }

    fun setDetailActive(session: Long, active: Boolean) {
        if (!detailRequests.owns(session) || detailActive == active) return
        detailActive = active
        manageFreshness()
        detailPollingJob?.cancel()
        if (!active && detailRequestActive && detailRequestIsPolling) {
            detailRequests.nextRequest()
            detailJob?.cancel()
            detailRequestActive = false
            detailRequestIsPolling = false
        }
        if (active) {
            refreshCalendar()
            updateFreshness()
            if (_detailDataStale.value && !detailRequestActive && _detailError.value == null) {
                requestMatchDetails(session)
            }
            detailPollingJob = viewModelScope.launch {
                while (isActive && detailRequests.owns(session)) {
                    delay(30000)
                    refreshCalendar()
                    if (shouldPollDetail(detailActive, _matchDetail.value?.status, detailRequestActive, _detailError.value != null)) {
                        requestMatchDetails(session, polling = true)
                    }
                }
            }
        }
    }

    fun fetchMatches(isRefresh: Boolean = false, loadNextPage: Boolean = false) {
        requestMatches(isRefresh, loadNextPage, forceNetwork = isRefresh)
    }

    private fun requestMatches(
        isRefresh: Boolean, loadNextPage: Boolean, polling: Boolean = false, forceNetwork: Boolean = false
    ) {
        if (searchPending) return
        if (loadNextPage && (!_hasMore.value || listRequestActive)) return
        if (listRequestActive && (!isRefresh || (!listRequestIsPolling && !_isLoadingNextPage.value))) return
        val generation = ++listGeneration
        listJob?.cancel()
        val requestedPage = if (loadNextPage) currentPage + 1 else 1
        val apiStatus = when (_selectedStatus.value) {
            "Ao Vivo" -> "running"
            "Finalizados" -> "finished"
            "Em breve" -> "not_started"
            else -> null
        }
        val apiGame = when (_selectedGame.value) {
            "Todos" -> null
            "CS2" -> "CSGO"
            else -> _selectedGame.value
        }
        val apiTier = _selectedTier.value.takeUnless { it == "Todos" }
        val apiTeamQuery = _searchQuery.value.trim().takeIf { it.isNotEmpty() }
        val date = _selectedDate.value
        val bounds = date?.let { localDayBounds(it, _deviceZone.value) }
        if (date != null && bounds == null) {
            listRequestActive = false
            listRequestIsPolling = false
            _isLoading.value = false
            _isRefreshing.value = false
            _isLoadingNextPage.value = false
            _hasMore.value = false
            retryNextPage = false
            _error.value = R.string.error_invalid_calendar_date
            return
        }
        val key = MatchPageKey(apiGame, apiStatus, apiTier, apiTeamQuery,
            bounds?.first?.toString(), bounds?.second?.toString(), requestedPage)
        if (!forceNetwork && !polling) {
            val cached = pageCache.get(key)
            if (cached != null && (cached.isFresh(SystemClock.elapsedRealtimeNanos()) || !loadNextPage)) {
                acceptMatchPage(cached, requestedPage, loadNextPage)
                _error.value = null
                _isLoading.value = false
                updateFreshness()
                if (cached.isFresh(SystemClock.elapsedRealtimeNanos())) return
            }
        }
        listRequestActive = true
        listRequestIsPolling = polling
        _isLoading.value = activePages.isEmpty() && !isRefresh && !loadNextPage && !polling
        _isRefreshing.value = (isRefresh || (activePages.isNotEmpty() && !loadNextPage)) && !polling
        _isLoadingNextPage.value = loadNextPage
        _error.value = null
        listJob = viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getMatches(
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
                val accepted = pageCache.put(key, result, Instant.now())
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
                    _isLoading.value = false
                    _isRefreshing.value = false
                    _isLoadingNextPage.value = false
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
        _hasMore.value = entry.value.hasMore
        _matches.value = allFetchedMatches.toList()
        _loadedMatchesCount.value = allFetchedMatches.size
        _totalMatches.value = entry.value.total
        _lastSuccessfulQuery.value = entry.queriedAt
        retryNextPage = false
        updateFreshness()
    }

    private fun reportListError(generation: Long, loadNextPage: Boolean, key: MatchPageKey, error: Exception) {
        if (generation == listGeneration) {
            retryNextPage = loadNextPage
            _error.value = requestErrorMessage(error)
            activePages.replaceAll { _, entry -> entry.copy(revalidationFailed = true) }
            pageCache.markStale { it.copy(page = 1) == key.copy(page = 1) }
            updateFreshness()
        }
    }

    fun retryMatches() {
        if (_error.value != null && !listRequestActive) {
            requestMatches(isRefresh = !retryNextPage, loadNextPage = retryNextPage, forceNetwork = true)
        }
    }

    fun loadNextPage() {
        if (!listActive) return
        fetchMatches(loadNextPage = true)
    }

    fun onGameFilterSelected(game: String) {
        if (_selectedGame.value == game) return
        _selectedGame.value = game
        reloadFilters()
    }

    fun onStatusFilterSelected(status: String) {
        if (_selectedStatus.value == status) return
        _selectedStatus.value = status
        if (status == "Ao Vivo") {
            _selectedDate.value = null
        }
        reloadFilters()
        managePolling()
    }

    fun onTierFilterSelected(tier: String) {
        if (_selectedTier.value == tier) return
        _selectedTier.value = tier
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
        _listDataStale.value = false
        currentPage = 1
        _hasMore.value = true
        retryNextPage = false
        _matches.value = emptyList()
        _totalMatches.value = null
        _loadedMatchesCount.value = 0
        _lastSuccessfulQuery.value = null
        _error.value = null
        _isLoading.value = true
        _isRefreshing.value = false
        _isLoadingNextPage.value = false
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
        val changeStatus = _selectedStatus.value == "Ao Vivo" && date != null
        if (changeStatus) {
            _selectedStatus.value = "Todos"
            managePolling()
        }
        _selectedDate.value = if (_selectedDate.value == date) null else date
        reloadFilters()
    }

    fun onSearchQueryChanged(query: String) {
        if (_searchQuery.value == query) return
        _searchQuery.value = query
        reloadFilters(debounce = query.isNotBlank())
    }

    fun clearFilters() {
        _selectedGame.value = "Todos"
        _selectedStatus.value = "Todos"
        _selectedTier.value = "Todos"
        _searchQuery.value = ""
        _selectedDate.value = null
        reloadFilters()
        managePolling()
    }

    fun selectToday() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        _deviceZone.value = zone
        _dateList.value = calendarDates(today)
        _selectedDate.value = today
        if (_selectedStatus.value == "Ao Vivo") _selectedStatus.value = "Todos"
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
            if (_listDataStale.value && !listRequestActive && _error.value == null && !searchPending) {
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
        if (_dateList.value != days || _deviceZone.value != zone) {
            val previousDate = _selectedDate.value
            val zoneChanged = _deviceZone.value != zone
            _dateList.value = days
            _deviceZone.value = zone
            if (_selectedDate.value !in days) _selectedDate.value = null
            if (previousDate != _selectedDate.value || (zoneChanged && previousDate != null)) {
                reloadFilters()
            }
        }
    }

    private fun managePolling() {
        pollingJob?.cancel()
        if (listActive && (_selectedStatus.value == "Ao Vivo" || _selectedStatus.value == "Todos")) {
            pollingJob = viewModelScope.launch {
                while (isActive) {
                    delay(30000)
                    if (shouldPollMatches(listActive, _selectedStatus.value, currentPage, listRequestActive, _error.value != null, searchPending)) {
                        requestMatches(isRefresh = false, loadNextPage = false, polling = true)
                    }
                }
            }
        }
    }
}
