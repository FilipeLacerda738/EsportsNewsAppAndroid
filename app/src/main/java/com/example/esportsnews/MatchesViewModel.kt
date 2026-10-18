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
import java.time.format.DateTimeParseException
import java.time.ZonedDateTime

class MatchesViewModel(application: Application) : AndroidViewModel(application) {

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _isLoadingNextPage = MutableStateFlow(false)
    val isLoadingNextPage: StateFlow<Boolean> = _isLoadingNextPage

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
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

    val dateList: List<LocalDate> = List(8) { LocalDate.now().minusDays(1).plusDays(it.toLong()) }

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
    private var detailGeneration = 0L

    private val _matchDetail = MutableStateFlow<MatchDetail?>(null)
    val matchDetail: StateFlow<MatchDetail?> = _matchDetail

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading

    private val _detailError = MutableStateFlow<String?>(null)
    val detailError: StateFlow<String?> = _detailError

    private val _updateAvailable = MutableStateFlow<AppVersion?>(null)
    val updateAvailable: StateFlow<AppVersion?> = _updateAvailable

    init {
        fetchMatches()
        checkForUpdates()
    }

    private fun checkForUpdates() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getAppVersion()
                if (response.isSuccessful) {
                    val serverVersion = response.body()?.version
                    val currentVersion = BuildConfig.VERSION_NAME
                    if (serverVersion != null && serverVersion != currentVersion) {
                        _updateAvailable.value = response.body()
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: IOException) {
                // The optional update check must not block matches.
            } catch (_: HttpException) {
            } catch (_: JsonParseException) {
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateAvailable.value = null
    }

    fun fetchMatchDetails(matchId: Int): Long {
        val generation = ++detailGeneration
        detailJob?.cancel()
        _matchDetail.value = null
        _isDetailLoading.value = true
        _detailError.value = null
        detailJob = viewModelScope.launch {
            try {
                val detail = RetrofitClient.api.getMatchDetails(matchId)
                if (generation != detailGeneration) return@launch
                if (detail.id != matchId) throw JsonParseException("Unexpected match ID")
                _matchDetail.value = detail
            } catch (e: CancellationException) {
                throw e
            } catch (_: IOException) {
                reportDetailError(generation)
            } catch (_: HttpException) {
                reportDetailError(generation)
            } catch (_: JsonParseException) {
                reportDetailError(generation)
            } finally {
                if (generation == detailGeneration) _isDetailLoading.value = false
            }
        }
        return generation
    }

    private fun reportDetailError(generation: Long) {
        if (generation == detailGeneration) {
            _detailError.value = getApplication<Application>().getString(R.string.error_match_details)
        }
    }

    fun clearMatchDetails(expectedGeneration: Long? = null) {
        if (expectedGeneration != null && expectedGeneration != detailGeneration) return
        ++detailGeneration
        detailJob?.cancel()
        _matchDetail.value = null
        _detailError.value = null
        _isDetailLoading.value = false
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
                if (result.page != requestedPage) throw JsonParseException("Unexpected match page")
                val items: List<Match?>? = result.items
                if (items == null || items.any { it == null }) {
                    throw JsonParseException("Missing match items")
                }
                val combined = if (loadNextPage) allFetchedMatches + result.items else result.items
                allFetchedMatches = combined.distinctBy { it.id }.toMutableList()
                currentPage = requestedPage
                _hasMore.value = result.hasMore
                applyFilters()
            } catch (e: CancellationException) {
                throw e
            } catch (_: IOException) {
                reportListError(generation, loadNextPage)
            } catch (_: HttpException) {
                reportListError(generation, loadNextPage)
            } catch (_: JsonParseException) {
                reportListError(generation, loadNextPage)
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

    private fun reportListError(generation: Long, loadNextPage: Boolean) {
        if (generation == listGeneration) {
            retryNextPage = loadNextPage
            _error.value = getApplication<Application>().getString(R.string.error_connection)
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
                if (match.begin_at == null) false else {
                    try {
                        val matchDateTime = ZonedDateTime.parse(match.begin_at)
                        val matchLocalDate = matchDateTime.toLocalTime().let { matchDateTime.toLocalDate() }
                        matchLocalDate.isEqual(targetDate)
                    } catch (_: DateTimeParseException) {
                        false
                    }
                }
            }
        }
        _matches.value = filteredList
    }

    fun setListActive(active: Boolean) {
        if (listActive == active) return
        listActive = active
        if (!active && listRequestActive && listRequestIsPolling) {
            ++listGeneration
            listJob?.cancel()
            listRequestActive = false
            listRequestIsPolling = false
        }
        managePolling()
    }

    private fun managePolling() {
        pollingJob?.cancel()
        if (listActive && (_selectedStatus.value == "Ao Vivo" || _selectedStatus.value == "Todos")) {
            pollingJob = viewModelScope.launch {
                while (isActive) {
                    delay(30000)
                    if (currentPage == 1 && !listRequestActive && _error.value == null) {
                        requestMatches(isRefresh = false, loadNextPage = false, polling = true)
                    }
                }
            }
        }
    }
}
