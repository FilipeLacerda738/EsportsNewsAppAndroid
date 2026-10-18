package com.example.esportsnews

import android.app.Application
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
import java.time.LocalDate
import java.time.ZoneId

class MatchesViewModel(application: Application) : AndroidViewModel(application) {

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches

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

    private var allFetchedMatches = mutableListOf<Match>()
    private var currentPage = 1
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore

    private var pollingJob: Job? = null
    private var listJob: Job? = null
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
        detailRequestActive = false
        _matchDetail.value = null
        requestMatchDetails(session)
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
                _matchDetail.value = validateMatchDetail(detail, matchId)
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
        }
    }

    fun clearMatchDetails(expectedSession: Long) {
        if (!detailRequests.close(expectedSession)) return
        detailPollingJob?.cancel()
        detailJob?.cancel()
        detailActive = false
        detailRequestActive = false
        detailRequestIsPolling = false
        _matchDetail.value = null
        _detailError.value = null
        _isDetailLoading.value = false
        _isDetailRefreshing.value = false
    }

    fun setDetailActive(session: Long, active: Boolean) {
        if (!detailRequests.owns(session) || detailActive == active) return
        detailActive = active
        detailPollingJob?.cancel()
        if (!active && detailRequestActive && detailRequestIsPolling) {
            detailRequests.nextRequest()
            detailJob?.cancel()
            detailRequestActive = false
            detailRequestIsPolling = false
        }
        if (active) {
            refreshCalendar()
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
        requestMatches(isRefresh, loadNextPage)
    }

    private fun requestMatches(isRefresh: Boolean, loadNextPage: Boolean, polling: Boolean = false) {
        if (loadNextPage && (!_hasMore.value || listRequestActive)) return
        if (listRequestActive && !isRefresh) return
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
        listRequestActive = true
        listRequestIsPolling = polling
        _isLoading.value = !isRefresh && !loadNextPage && !polling
        _isRefreshing.value = isRefresh && !polling
        _isLoadingNextPage.value = loadNextPage
        _error.value = null
        listJob = viewModelScope.launch {
            try {
                val result = RetrofitClient.api.getMatches(
                    page = requestedPage,
                    game = apiGame,
                    status = apiStatus,
                    tier = apiTier
                )

                if (generation != listGeneration) return@launch
                allFetchedMatches = mergeMatchPage(allFetchedMatches, result, requestedPage, loadNextPage).toMutableList()
                currentPage = requestedPage
                _hasMore.value = result.hasMore
                applyFilters()
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                reportListError(generation, loadNextPage, e)
            } catch (e: HttpException) {
                reportListError(generation, loadNextPage, e)
            } catch (e: JsonParseException) {
                reportListError(generation, loadNextPage, e)
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

    private fun reportListError(generation: Long, loadNextPage: Boolean, error: Exception) {
        if (generation == listGeneration) {
            retryNextPage = loadNextPage
            _error.value = requestErrorMessage(error)
        }
    }

    fun retryMatches() {
        if (_error.value != null && !listRequestActive) {
            fetchMatches(isRefresh = !retryNextPage, loadNextPage = retryNextPage)
        }
    }

    fun loadNextPage() {
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

    private fun reloadFilters() {
        ++listGeneration
        listJob?.cancel()
        listRequestActive = false
        allFetchedMatches.clear()
        currentPage = 1
        _hasMore.value = true
        applyFilters()
        fetchMatches()
    }

    fun onDateSelected(date: LocalDate?) {
        val changeStatus = _selectedStatus.value == "Ao Vivo" && date != null
        if (changeStatus) {
            _selectedStatus.value = "Todos"
            managePolling()
        }
        _selectedDate.value = if (_selectedDate.value == date) null else date
        if (changeStatus) reloadFilters() else applyFilters()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    private fun applyFilters() {
        val query = _searchQuery.value.trim().lowercase()
        val targetDate = _selectedDate.value

        var filteredList = allFetchedMatches.toList()

        if (query.isNotEmpty()) {
            filteredList = filteredList.filter { match ->
                match.team_a?.name?.lowercase()?.contains(query) == true ||
                        match.team_b?.name?.lowercase()?.contains(query) == true
            }
        }

        if (targetDate != null) {
            filteredList = filteredList.filter { match ->
                matchDateTime(match.begin_at, _deviceZone.value)?.toLocalDate() == targetDate
            }
        }
        _matches.value = filteredList
    }

    fun setListActive(active: Boolean) {
        if (listActive == active) return
        listActive = active
        calendarJob?.cancel()
        if (active) {
            refreshCalendar()
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
        val days = calendarDates(LocalDate.now())
        val zone = ZoneId.systemDefault()
        if (_dateList.value != days || _deviceZone.value != zone) {
            _dateList.value = days
            _deviceZone.value = zone
            if (_selectedDate.value !in days) _selectedDate.value = null
            applyFilters()
        }
    }

    private fun managePolling() {
        pollingJob?.cancel()
        if (listActive && (_selectedStatus.value == "Ao Vivo" || _selectedStatus.value == "Todos")) {
            pollingJob = viewModelScope.launch {
                while (isActive) {
                    delay(30000)
                    if (shouldPollMatches(listActive, _selectedStatus.value, currentPage, listRequestActive, _error.value != null)) {
                        requestMatches(isRefresh = false, loadNextPage = false, polling = true)
                    }
                }
            }
        }
    }
}
