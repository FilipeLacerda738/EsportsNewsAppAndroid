package com.example.esportsnews.feature.matchdetail

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MatchDetailRoute(matchId: Int, viewModel: MatchDetailViewModel, lifecycle: Lifecycle,
    onNavigateBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle(lifecycle)
    var session by remember(matchId) { mutableStateOf<Long?>(null) }

    DisposableEffect(matchId, viewModel, lifecycle) {
        val owner = viewModel.fetchMatchDetails(matchId)
        session = owner
        val observer = LifecycleEventObserver { _, _ ->
            viewModel.setDetailActive(owner, lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
        }
        lifecycle.addObserver(observer)
        viewModel.setDetailActive(owner, lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
        onDispose {
            lifecycle.removeObserver(observer)
            viewModel.clearMatchDetails(owner)
        }
    }
    val refresh = { session?.let { viewModel.refreshMatchDetails(it) }; Unit }
    MatchDetailScreen(matchId, state, session != null, refresh, onNavigateBack)
}
