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
import java.time.format.DateTimeFormatter

class MatchesViewModel : ViewModel() {

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

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

    private var allFetchedMatches: List<Match> = emptyList()
    private var pollingJob: Job? = null

    init {
        fetchMatches()
        managePolling()
    }

    fun fetchMatches(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) _isRefreshing.value = true else _isLoading.value = true
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

                val result = RetrofitClient.api.getMatches(game = apiGame, status = apiStatus)
                allFetchedMatches = result
                applyFilters()
            } catch (e: Exception) {
                _error.value = "Erro de conexão: ${e.message}"
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
    }

    fun onGameFilterSelected(game: String) {
        _selectedGame.value = game
        fetchMatches()
    }

    fun onStatusFilterSelected(status: String) {
        _selectedStatus.value = status
        if (status == "Ao Vivo") {
            _selectedDate.value = null
        }
        fetchMatches()
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

        var filteredList = allFetchedMatches

        if (query.isNotEmpty()) {
            filteredList = filteredList.filter { match ->
                match.team_a.name.lowercase().contains(query) ||
                        match.team_b.name.lowercase().contains(query)
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
        if (_selectedStatus.value == "Ao Vivo" || _selectedStatus.value == "Todos") {
            pollingJob = viewModelScope.launch {
                while (isActive) {
                    delay(30000)
                    fetchMatches(isRefresh = true)
                }
            }
        }
    }
}