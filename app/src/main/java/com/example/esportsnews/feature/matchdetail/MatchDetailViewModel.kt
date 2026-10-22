package com.example.esportsnews.feature.matchdetail

import com.example.esportsnews.data.repository.validateMatchDetail

import com.example.esportsnews.core.network.requestErrorMessage
import com.example.esportsnews.data.cache.MatchMemoryCache
import com.example.esportsnews.data.cache.RecentMatchData

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
import java.time.ZoneId

class MatchDetailViewModel(
    private val repository: MatchesRepository = MatchesRepository(),
    private val nowNanos: () -> Long = SystemClock::elapsedRealtimeNanos,
    private val now: () -> Instant = Instant::now
) : ViewModel() {
    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    private val detailCache = MatchMemoryCache<Int, MatchDetail>(20, nowNanos)
    private var activeDetailData: RecentMatchData<MatchDetail>? = null
    private var freshnessJob: Job? = null

    private var detailJob: Job? = null
    private val detailRequests = DetailRequestSession()
    private var detailPollingJob: Job? = null
    private var detailActive = false
    private var detailRequestActive = false
    private var detailRequestIsPolling = false

    private fun updateFreshness() {
        _uiState.update {
            it.copy(
                detailDataStale = activeDetailData?.let { !it.isFresh(nowNanos()) || _uiState.value.detailError != null } ?: false
            )
        }
    }
    private fun manageFreshness() {
        freshnessJob?.cancel()
        if (detailActive) freshnessJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                updateFreshness()
                detailCache.prune()
            }
        }
    }
    private fun refreshCalendar() {
        _uiState.update { it.copy(deviceZone = ZoneId.systemDefault()) }
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
        _uiState.update {
            it.copy(
                matchDetail = activeDetailData?.value,
                lastSuccessfulDetailQuery = activeDetailData?.queriedAt,
                detailError = null,
                isDetailLoading = false,
                isDetailRefreshing = false
            )
        }
        updateFreshness()
        if (activeDetailData?.isFresh(nowNanos()) != true) requestMatchDetails(session)
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
        _uiState.update {
            it.copy(
                isDetailLoading = _uiState.value.matchDetail == null,
                isDetailRefreshing = _uiState.value.matchDetail != null && !polling,
                detailError = null
            )
        }
        detailJob = viewModelScope.launch {
            try {
                val detail = repository.getMatchDetails(matchId)
                if (!detailRequests.accepts(session, request)) return@launch
                val accepted = validateMatchDetail(detail, matchId)
                activeDetailData = detailCache.put(matchId, accepted, now())
                _uiState.update {
                    it.copy(
                        matchDetail = accepted,
                        lastSuccessfulDetailQuery = activeDetailData?.queriedAt
                    )
                }
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
                    _uiState.update { it.copy(isDetailLoading = false, isDetailRefreshing = false) }
                }
            }
        }
    }

    private fun reportDetailError(session: Long, request: Long, error: Exception) {
        if (detailRequests.accepts(session, request)) {
            _uiState.update { it.copy(detailError = requestErrorMessage(error)) }
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
        _uiState.update { it.copy(matchDetail = null) }
        activeDetailData = null
        _uiState.update {
            it.copy(
                lastSuccessfulDetailQuery = null,
                detailDataStale = false,
                detailError = null,
                isDetailLoading = false,
                isDetailRefreshing = false
            )
        }
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
            if (_uiState.value.detailDataStale && !detailRequestActive && _uiState.value.detailError == null) {
                requestMatchDetails(session)
            }
            detailPollingJob = viewModelScope.launch {
                while (isActive && detailRequests.owns(session)) {
                    delay(30000)
                    refreshCalendar()
                    if (shouldPollDetail(detailActive, _uiState.value.matchDetail?.status, detailRequestActive, _uiState.value.detailError != null)) {
                        requestMatchDetails(session, polling = true)
                    }
                }
            }
        }
    }

}
