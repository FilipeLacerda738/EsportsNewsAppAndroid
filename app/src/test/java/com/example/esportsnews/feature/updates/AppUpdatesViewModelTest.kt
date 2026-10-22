package com.example.esportsnews.feature.updates

import com.example.esportsnews.data.remote.dto.AppVersion
import com.example.esportsnews.data.repository.AppUpdatesRepository
import com.example.esportsnews.testsupport.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AppUpdatesViewModelTest : ViewModelTest() {
    @Test fun concurrentChecksShareOneRequestAndDismissKeepsResult() = runTest(dispatcher) {
        val pending = CompletableDeferred<Response<AppVersion>>()
        val api = FakeApi().apply { update = { pending.await() } }
        val vm = own(AppUpdatesViewModel(AppUpdatesRepository(api), "1.0.0"))
        runCurrent(); vm.checkForUpdates(); runCurrent()
        assertEquals(1, api.updates)
        pending.complete(Response.success(AppVersion("2.0.0", "https://example.com/app.apk", "Release")))
        runCurrent()
        assertEquals(UpdateCheckState.Available, vm.updateCheckState.value)
        assertNotNull(vm.updateAvailable.value)
        vm.dismissUpdateDialog()
        assertNull(vm.updateAvailable.value)
        assertEquals(UpdateCheckState.Available, vm.updateCheckState.value)
    }

    @Test fun failedCheckCanBeRetried() = runTest(dispatcher) {
        val api = FakeApi().apply { update = { throw IOException() } }
        val vm = own(AppUpdatesViewModel(AppUpdatesRepository(api), "2.0.0")); runCurrent()
        assertEquals(UpdateCheckState.Failed, vm.updateCheckState.value)
        api.update = { Response.success(AppVersion("2.0.0", "https://example.com/app.apk", "")) }
        vm.checkForUpdates(); runCurrent()
        assertEquals(UpdateCheckState.UpToDate, vm.updateCheckState.value)
        assertNull(vm.updateAvailable.value)
    }
}
