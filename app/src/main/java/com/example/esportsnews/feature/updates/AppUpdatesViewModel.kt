package com.example.esportsnews.feature.updates

import com.example.esportsnews.BuildConfig
import com.example.esportsnews.data.remote.dto.AppVersion

import com.example.esportsnews.data.repository.AppUpdatesRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AppUpdatesViewModel(
    private val repository: AppUpdatesRepository = AppUpdatesRepository(),
    private val currentVersion: String = BuildConfig.VERSION_NAME
) : ViewModel() {
    private val _updateAvailable = MutableStateFlow<AppVersion?>(null)
    val updateAvailable: StateFlow<AppVersion?> = _updateAvailable.asStateFlow()

    private val _updateCheckState = MutableStateFlow(UpdateCheckState.Idle)
    internal val updateCheckState: StateFlow<UpdateCheckState> = _updateCheckState.asStateFlow()

    init { checkForUpdates() }
    fun checkForUpdates() {
        if (_updateCheckState.value == UpdateCheckState.Checking) return
        _updateCheckState.value = UpdateCheckState.Checking
        _updateAvailable.value = null
        viewModelScope.launch {
            try {
                val response = repository.getAppVersion()
                val candidate = response.body()
                val result = if (response.isSuccessful) {
                    evaluateAppUpdate(currentVersion, candidate)
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

}
