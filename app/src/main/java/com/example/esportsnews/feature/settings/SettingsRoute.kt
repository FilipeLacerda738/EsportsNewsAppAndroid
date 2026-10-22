package com.example.esportsnews.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.esportsnews.feature.updates.AppUpdatesViewModel

@Composable
fun SettingsRoute(viewModel: AppUpdatesViewModel, onOpenMenu: () -> Unit) {
    val state by viewModel.updateCheckState.collectAsStateWithLifecycle()
    SettingsScreen(state, viewModel::checkForUpdates, onOpenMenu)
}
