package com.example.esportsnews.feature.matches

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MatchesRoute(viewModel: MatchesViewModel, lifecycle: Lifecycle,
    onMatchClick: (Int) -> Unit, onOpenMenu: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle(lifecycle)
    DisposableEffect(viewModel, lifecycle) {
        val observer = LifecycleEventObserver { _, _ ->
            viewModel.setListActive(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
        }
        lifecycle.addObserver(observer)
        viewModel.setListActive(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
        onDispose { lifecycle.removeObserver(observer); viewModel.setListActive(false) }
    }
    MatchesScreen(state, onMatchClick, onOpenMenu,
        onRefresh = { viewModel.fetchMatches(isRefresh = true) },
        onSearch = viewModel::onSearchQueryChanged, onGame = viewModel::onGameFilterSelected,
        onStatus = viewModel::onStatusFilterSelected, onTier = viewModel::onTierFilterSelected,
        onDate = viewModel::onDateSelected, onClear = viewModel::clearFilters,
        onToday = viewModel::selectToday, onRetry = viewModel::retryMatches,
        onNextPage = viewModel::loadNextPage)
}
