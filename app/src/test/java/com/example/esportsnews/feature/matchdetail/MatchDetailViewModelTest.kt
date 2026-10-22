package com.example.esportsnews.feature.matchdetail

import com.example.esportsnews.data.repository.MatchesRepository
import com.example.esportsnews.data.remote.dto.MatchDetail
import com.example.esportsnews.testsupport.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class MatchDetailViewModelTest : ViewModelTest() {
    private val api = FakeApi()
    private fun model() = own(MatchDetailViewModel(MatchesRepository(api),
        nowNanos = { dispatcher.scheduler.currentTime * 1_000_000 },
        now = { Instant.EPOCH.plusMillis(dispatcher.scheduler.currentTime) }))

    @Test fun switchingDetailsRejectsOldResponseAndOldDisposal() = runTest(dispatcher) {
        val old = CompletableDeferred<MatchDetail>()
        api.detail = { if (it == 1) withContext(NonCancellable) { old.await() } else detail(it) }
        val vm = model(); val first = vm.fetchMatchDetails(1); runCurrent()
        val second = vm.fetchMatchDetails(2); runCurrent()
        vm.clearMatchDetails(first)
        old.complete(detail(1)); runCurrent()
        assertEquals(2, vm.uiState.value.matchDetail?.id)
        vm.clearMatchDetails(second)
        assertNull(vm.uiState.value.matchDetail)
    }

    @Test fun detailCacheSurvivesDisposalAndExpiredEntryIsRevalidated() = runTest(dispatcher) {
        val vm = model(); val first = vm.fetchMatchDetails(1); runCurrent()
        vm.clearMatchDetails(first)
        val second = vm.fetchMatchDetails(1); runCurrent()
        assertEquals(1, api.details.size)
        vm.clearMatchDetails(second); advanceTimeBy(30_001)
        vm.fetchMatchDetails(1); runCurrent()
        assertEquals(2, api.details.size)
    }

    @Test fun closingDetailCancelsRequestAndDropsLateResponse() = runTest(dispatcher) {
        val pending = CompletableDeferred<MatchDetail>()
        api.detail = { withContext(NonCancellable) { pending.await() } }
        val vm = model(); val session = vm.fetchMatchDetails(1); runCurrent()
        vm.clearMatchDetails(session); pending.complete(detail(1)); runCurrent()
        assertNull(vm.uiState.value.matchDetail)
        assertFalse(vm.uiState.value.isDetailLoading)
    }

    @Test fun leavingDetailStopsPollingAndReturningRevalidatesExpiredData() = runTest(dispatcher) {
        val vm = model(); val session = vm.fetchMatchDetails(1)
        vm.setDetailActive(session, true); runCurrent()
        advanceTimeBy(30_000); runCurrent()
        assertEquals(2, api.details.size)
        vm.setDetailActive(session, false)
        advanceTimeBy(60_000); runCurrent()
        assertEquals(2, api.details.size)
        vm.setDetailActive(session, true); runCurrent()
        assertEquals(3, api.details.size)
        vm.setDetailActive(session, false)
    }

    @Test fun failedRefreshPreservesDetailsAndMarksThemStale() = runTest(dispatcher) {
        val vm = model(); val session = vm.fetchMatchDetails(1); runCurrent()
        api.detail = { throw IOException() }
        vm.refreshMatchDetails(session); runCurrent()
        assertEquals(1, vm.uiState.value.matchDetail?.id)
        assertTrue(vm.uiState.value.detailDataStale)
        assertNotNull(vm.uiState.value.detailError)
        assertFalse(vm.uiState.value.isDetailRefreshing)
    }
}
