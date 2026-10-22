package com.example.esportsnews.feature.matches

import com.example.esportsnews.R
import com.example.esportsnews.core.ui.components.HubBrand
import com.example.esportsnews.feature.matches.components.MatchCalendar
import com.example.esportsnews.feature.matches.components.MatchFilterChip
import com.example.esportsnews.feature.matches.components.ModernMatchCard
import com.example.esportsnews.feature.matches.components.SkeletonMatchCard
import com.example.esportsnews.feature.matches.components.TierFilterMenu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.esportsnews.core.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(state: MatchesUiState, onMatchClick: (Int) -> Unit, onOpenMenu: () -> Unit,
    onRefresh: () -> Unit,
    onSearch: (String) -> Unit,
    onGame: (GameFilter) -> Unit,
    onStatus: (StatusFilter) -> Unit,
    onTier: (TierFilter) -> Unit,
    onDate: (LocalDate?) -> Unit,
    onClear: () -> Unit,
    onToday: () -> Unit,
    onRetry: () -> Unit,
    onNextPage: () -> Unit
) {
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    val matches = state.matches
    val isLoading = state.isLoading
    val isRefreshing = state.isRefreshing
    val error = state.error
    val dataStale = state.listDataStale
    val hasMore = state.hasMore
    val isLoadingNextPage = state.isLoadingNextPage
    val selectedGame = state.selectedGame
    val selectedStatus = state.selectedStatus
    val searchQuery = state.searchQuery
    val selectedDate = state.selectedDate
    val selectedTier = state.selectedTier
    val dateList = state.dateList
    val deviceZone = state.deviceZone
    val totalMatches = state.totalMatches
    val loadedMatchesCount = state.loadedMatchesCount
    val lastSuccessfulQuery = state.lastSuccessfulQuery
    val hasSelectedFilters = selectedGame != GameFilter.ALL || selectedStatus != StatusFilter.ALL ||
        selectedTier != TierFilter.ALL || searchQuery.isNotBlank() || selectedDate != null
    val currentLocale = LocalConfiguration.current.locales[0]

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(HubSpacing.Large),
                verticalArrangement = Arrangement.spacedBy(HubSpacing.Medium)
            ) {
                item(key = "filters", contentType = "filters") {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
                        MatchesHeader(onOpenMenu)
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { onSearch(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.search_team)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = if (searchQuery.isNotEmpty()) {
                                { IconButton(onClick = { onSearch("") }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_search))
                                } }
                            } else null,
                            shape = MaterialTheme.shapes.extraLarge,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            singleLine = true
                        )
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
                            LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
                                items(GameFilter.entries, key = { it }) { game ->
                                    MatchFilterChip(if (game == GameFilter.ALL) stringResource(R.string.filter_all) else game.label,
                                        selectedGame == game, onClick = { onGame(game) })
                                }
                            }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
                            items(StatusFilter.entries, key = { it }) { status ->
                                val label = stringResource(status.labelRes)
                                MatchFilterChip(label, selectedStatus == status, usePurple = true, onClick = { onStatus(status) })
                            }
                        }
                        val secondaryCount = (if (selectedTier != TierFilter.ALL) 1 else 0) +
                            (if (selectedDate != null) 1 else 0)
                        TextButton(onClick = { filtersExpanded = !filtersExpanded }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text(stringResource(if (filtersExpanded) R.string.hide_filters else R.string.more_filters, secondaryCount))
                        }
                        if (filtersExpanded) {
                            TierFilterMenu(selectedTier, onSelect = onTier)
                            MatchCalendar(dateList, selectedDate, currentLocale, onSelect = onDate)

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)
                            ) {
                                OutlinedButton(
                                    onClick = onClear,
                                    enabled = hasSelectedFilters,
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                ) { Text(stringResource(R.string.clear_filters), textAlign = TextAlign.Center) }
                                OutlinedButton(
                                    onClick = onToday,
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                ) { Text(stringResource(R.string.select_today), textAlign = TextAlign.Center) }
                            }
                        }
                        Text(stringResource(R.string.matches_section), style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = HubSpacing.Small).semantics { heading() })
                        totalMatches?.let { total ->
                            Text(
                                stringResource(R.string.matches_count, total, loadedMatchesCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (dataStale) {
                            Text(stringResource(R.string.cached_data_stale),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        lastSuccessfulQuery?.let { queriedAt ->
                            val formatter = remember(currentLocale, queriedAt, deviceZone) {
                                if (queriedAt.atZone(deviceZone).toLocalDate() == LocalDate.now(deviceZone))
                                    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(currentLocale)
                                else DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(currentLocale)
                            }
                            Text(
                                stringResource(R.string.last_successful_query, formatter.format(queriedAt.atZone(deviceZone))),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                when {
                    (isLoading || isRefreshing) && matches.isEmpty() -> {
                        items(8, key = { "skeleton_$it" }, contentType = { "skeleton" }) { SkeletonMatchCard() }
                    }
                    error != null && matches.isEmpty() -> {
                        item(key = "initial_error") {
                            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                                Column(Modifier.fillMaxWidth().padding(HubSpacing.ExtraLarge),
                                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HubSpacing.Medium)) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(stringResource(error ?: R.string.error_unknown_fallback), textAlign = TextAlign.Center)
                                    Button(onClick = { onRetry() }) { Text(stringResource(R.string.try_again)) }
                                }
                            }
                        }
                    }
                    else -> {
                        if (matches.isEmpty() && error == null && !isLoading && !isRefreshing) {
                            item(key = "matches_empty") {
                                Text(stringResource(R.string.matches_empty), style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = HubSpacing.Large), textAlign = TextAlign.Center)
                            }
                        }
                        items(matches, key = { it.id }, contentType = { "match" }) { match ->
                            ModernMatchCard(match, onClick = { onMatchClick(match.id) }, zone = deviceZone)
                        }
                        item(key = "pagination") {
                            if (hasMore && error == null && !isLoading &&
                                !isRefreshing && !isLoadingNextPage && matches.isNotEmpty()) {
                                LaunchedEffect(loadedMatchesCount, hasMore, error, isLoadingNextPage) {
                                    onNextPage()
                                }
                            }
                            when {
                                error != null -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(error ?: R.string.error_unknown_fallback), color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                                    TextButton(onClick = { onRetry() }) { Text(stringResource(R.string.try_again)) }
                                }
                                isLoadingNextPage -> Box(Modifier.fillMaxWidth().padding(HubSpacing.Large), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                                hasMore && !isLoading && !isRefreshing -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    OutlinedButton(onClick = { onNextPage() }) { Text(stringResource(R.string.load_more_matches)) }
                                }
                                else -> Spacer(Modifier.height(HubSpacing.ExtraLarge))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchesHeader(onOpenMenu: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = HubSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HubSpacing.Medium)
    ) {
        FilledIconButton(
            onClick = onOpenMenu,
            shape = MaterialTheme.shapes.large,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.open_menu))
        }
        HubBrand(Modifier.weight(1f))
    }
}
