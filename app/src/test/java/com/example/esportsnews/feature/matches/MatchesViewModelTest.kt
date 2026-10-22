package com.example.esportsnews.feature.matches

import com.example.esportsnews.R
import com.example.esportsnews.data.remote.dto.PaginatedMatchResponse
import com.example.esportsnews.data.repository.MatchesRepository
import com.example.esportsnews.testsupport.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class MatchesViewModelTest : ViewModelTest() {
    private val api = FakeApi()
    private fun model() = own(MatchesViewModel(MatchesRepository(api),
        nowNanos = { dispatcher.scheduler.currentTime * 1_000_000 },
        now = { Instant.EPOCH.plusMillis(dispatcher.scheduler.currentTime) }))

    @Test fun rapidFiltersRejectOldResponseEvenWhenTransportIgnoresCancellation() = runTest(dispatcher) {
        val old = CompletableDeferred<Response<PaginatedMatchResponse>>()
        api.list = { if (it.game == null) withContext(NonCancellable) { old.await() } else page(1, 2) }
        val vm = model(); runCurrent()
        vm.onGameFilterSelected(GameFilter.CS2); runCurrent()
        assertEquals("CSGO", api.lists.last().game)
        old.complete(page(1, 99)); runCurrent()
        assertEquals(listOf(2), vm.uiState.value.matches.map { it.id })
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun searchDebouncesAndCancelsPreviousQuery() = runTest(dispatcher) {
        val vm = model(); runCurrent()
        vm.onSearchQueryChanged("old"); advanceTimeBy(200)
        vm.onSearchQueryChanged("new"); advanceTimeBy(349); runCurrent()
        assertEquals(1, api.lists.size)
        advanceTimeBy(1); runCurrent()
        assertEquals("new", api.lists.last().team)
        assertEquals(2, api.lists.size)
    }

    @Test fun nextPageFailureKeepsItemsAndRetryRequestsTheSamePage() = runTest(dispatcher) {
        var fail = true
        api.list = { if (it.page == 2 && fail) throw IOException() else page(it.page, it.page, more = true) }
        val vm = model(); vm.setListActive(true); runCurrent()
        vm.loadNextPage(); runCurrent()
        assertEquals(listOf(1), vm.uiState.value.matches.map { it.id })
        assertEquals(R.string.error_connection, vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoadingNextPage)
        fail = false; vm.retryMatches(); runCurrent()
        assertEquals(listOf(1, 2), vm.uiState.value.matches.map { it.id })
        assertEquals(listOf(1, 2, 2), api.lists.map { it.page })
        vm.setListActive(false)
    }

    @Test fun freshCacheIsReusedAndExpiredCacheIsRevalidated() = runTest(dispatcher) {
        val vm = model(); runCurrent()
        vm.onGameFilterSelected(GameFilter.CS2); runCurrent()
        vm.onGameFilterSelected(GameFilter.ALL); runCurrent()
        assertEquals(2, api.lists.size)
        advanceTimeBy(30_001)
        vm.onGameFilterSelected(GameFilter.CS2); runCurrent()
        vm.onGameFilterSelected(GameFilter.ALL); runCurrent()
        assertEquals(4, api.lists.size)
        assertFalse(vm.uiState.value.listDataStale)
    }

    @Test fun leavingListCancelsInFlightPollingAndRejectsLateResult() = runTest(dispatcher) {
        val late = CompletableDeferred<Response<PaginatedMatchResponse>>()
        val vm = model(); vm.setListActive(true); runCurrent()
        api.list = { withContext(NonCancellable) { late.await() } }
        advanceTimeBy(30_000); runCurrent()
        assertEquals(2, api.lists.size)
        vm.setListActive(false)
        late.complete(page(1, 99)); runCurrent()
        advanceTimeBy(90_000); runCurrent()
        assertEquals(2, api.lists.size)
        assertEquals(listOf(1), vm.uiState.value.matches.map { it.id })
    }

    @Test fun liveFilterClearsDateAndDateSelectionStopsLiveFilter() = runTest(dispatcher) {
        val vm = model(); runCurrent()
        vm.selectToday(); runCurrent()
        vm.onStatusFilterSelected(StatusFilter.LIVE); runCurrent()
        assertNull(vm.uiState.value.selectedDate)
        assertEquals("running", api.lists.last().status)
        vm.onDateSelected(java.time.LocalDate.now()); runCurrent()
        assertEquals(StatusFilter.ALL, vm.uiState.value.selectedStatus)
        assertNotNull(vm.uiState.value.selectedDate)
    }
}
