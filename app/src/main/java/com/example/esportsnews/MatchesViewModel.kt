package com.example.esportsnews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime

class MatchesViewModel : ViewModel() {

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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    val dateList: List<LocalDate> = List(8) { LocalDate.now().minusDays(1).plusDays(it.toLong()) }

    private var allFetchedMatches = mutableListOf<Match>()
    private var currentPage = 1
    private var isLastPage = false

    private var pollingJob: Job? = null

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
        managePolling()
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
            } catch (e: Exception) {}
        }
    }

    fun dismissUpdateDialog() {
        _updateAvailable.value = null
    }

    fun fetchMatchDetails(matchId: Int) {
        viewModelScope.launch {
            _isDetailLoading.value = true
            _detailError.value = null
            try {
                val detail = RetrofitClient.api.getMatchDetails(matchId)
                _matchDetail.value = detail
            } catch (e: Exception) {
                _detailError.value = "Não foi possível carregar os detalhes desta partida."
            } finally {
                _isDetailLoading.value = false
            }
        }
    }

    fun clearMatchDetails() {
        _matchDetail.value = null
        _detailError.value = null
    }

    fun fetchMatches(isRefresh: Boolean = false, loadNextPage: Boolean = false) {
        viewModelScope.launch {
            if (loadNextPage && (_isLoadingNextPage.value || isLastPage)) return@launch

            if (isRefresh) {
                _isRefreshing.value = true
                currentPage = 1
                isLastPage = false
            } else if (loadNextPage) {
                _isLoadingNextPage.value = true
                currentPage++
            } else {
                _isLoading.value = true
            }

            _error.value = null

            try {
                val apiStatus = when (_selectedStatus.value) {
                    "Ao Vivo" -> "running"
                    "Finalizados" -> "finished"
                    "Em breve" -> "not_started"
                    else -> null
                }
                val apiGame = when(_selectedGame.value){
                    "Todos" -> null
                    "CS2" -> "CSGO"
                    else -> _selectedGame.value
                }

                val result = RetrofitClient.api.getMatches(
                    page = currentPage,
                    game = apiGame,
                    status = apiStatus
                )

                isLastPage = !result.hasMore

                if (isRefresh || (!isRefresh && !loadNextPage)) {
                    allFetchedMatches.clear()
                    allFetchedMatches.addAll(result.items)
                } else {
                    allFetchedMatches.addAll(result.items)
                }

                applyFilters()

            } catch (e: Exception) {
                _error.value = "Sem conexão com nossos servidores. Verifique sua rede e tente novamente."
                if (loadNextPage) currentPage--
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
                _isLoadingNextPage.value = false
            }
        }
    }

    fun loadNextPage() {
        fetchMatches(loadNextPage = true)
    }

    fun onGameFilterSelected(game: String) {
        _selectedGame.value = game
        fetchMatches(isRefresh = true)
    }

    fun onStatusFilterSelected(status: String) {
        _selectedStatus.value = status
        if (status == "Ao Vivo") {
            _selectedDate.value = null
        }
        fetchMatches(isRefresh = true)
        managePolling()
    }

    fun onDateSelected(date: LocalDate?) {
        if (_selectedStatus.value == "Ao Vivo") {
            _selectedStatus.value = "Todos"
            managePolling()
        }
        _selectedDate.value = if (_selectedDate.value == date) null else date
        applyFilters()
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
                    } catch (e: Exception) {
                        false
                    }
                }
            }
        }
        _matches.value = filteredList
    }

    private fun managePolling() {
        pollingJob?.cancel()
        if ((_selectedStatus.value == "Ao Vivo" || _selectedStatus.value == "Todos") && currentPage == 1) {
            pollingJob = viewModelScope.launch {
                while (isActive) {
                    delay(30000)
                    fetchMatches(isRefresh = true)
                }
            }
        }
    }
}