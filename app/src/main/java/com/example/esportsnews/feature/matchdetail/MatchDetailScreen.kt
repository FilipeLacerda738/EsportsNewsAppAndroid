package com.example.esportsnews.feature.matchdetail

import androidx.activity.compose.BackHandler
import com.example.esportsnews.R
import com.example.esportsnews.feature.matchdetail.components.MatchDetailContent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    matchId: Int,
    state: MatchDetailUiState,
    sessionReady: Boolean,
    refresh: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler(onBack = onNavigateBack)
    val detail = state.matchDetail
    val isLoading = state.isDetailLoading
    val isRefreshing = state.isDetailRefreshing
    val error = state.detailError
    val dataStale = state.detailDataStale
    val queriedAt = state.lastSuccessfulDetailQuery
    val deviceZone = state.deviceZone
    val currentDetail = detail?.takeIf { it.id == matchId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.match_detail_title), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = refresh, enabled = sessionReady && !isLoading && !isRefreshing) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh_match))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = refresh,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (currentDetail != null) {
                MatchDetailContent(currentDetail, error, refresh, deviceZone, dataStale, queriedAt)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        if (isLoading || !sessionReady) {
                            CircularProgressIndicator()
                        } else {
                            Text(stringResource(error ?: R.string.error_match_details), textAlign = TextAlign.Center)
                            Button(onClick = refresh) { Text(stringResource(R.string.try_again)) }
                        }
                    }
                }
            }
        }
    }
}
