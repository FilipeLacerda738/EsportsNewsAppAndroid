package com.example.esportsnews

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.esportsnews.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(viewModel: MatchesViewModel, onMatchClick: (Int) -> Unit, onOpenMenu: () -> Unit) {
    val matches by viewModel.matches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val isLoadingNextPage by viewModel.isLoadingNextPage.collectAsState()
    val selectedGame by viewModel.selectedGame.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedTier by viewModel.selectedTier.collectAsState()
    val dateList by viewModel.dateList.collectAsState()
    val deviceZone by viewModel.deviceZone.collectAsState()
    val totalMatches by viewModel.totalMatches.collectAsState()
    val loadedMatchesCount by viewModel.loadedMatchesCount.collectAsState()
    val lastSuccessfulQuery by viewModel.lastSuccessfulQuery.collectAsState()
    val hasSelectedFilters = selectedGame != "Todos" || selectedStatus != "Todos" ||
        selectedTier != "Todos" || searchQuery.isNotBlank() || selectedDate != null
    val currentLocale = LocalConfiguration.current.locales[0]

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.fetchMatches(isRefresh = true) },
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
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.search_team)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = if (searchQuery.isNotEmpty()) {
                                { IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
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
                                items(listOf("Todos", "CS2", "VALORANT"), key = { it }) { game ->
                                    MatchFilterChip(if (game == "Todos") stringResource(R.string.filter_all) else game,
                                        selectedGame == game, onClick = { viewModel.onGameFilterSelected(game) })
                                }
                            }
                            TierFilter(selectedTier, onSelect = { viewModel.onTierFilterSelected(it) })
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
                            items(listOf("Todos", "Ao Vivo", "Em breve", "Finalizados"), key = { it }) { status ->
                                val label = when (status) {
                                    "Todos" -> stringResource(R.string.filter_all)
                                    "Ao Vivo" -> stringResource(R.string.status_live)
                                    "Em breve" -> stringResource(R.string.status_upcoming)
                                    else -> stringResource(R.string.status_finished)
                                }
                                MatchFilterChip(label, selectedStatus == status, usePurple = true, onClick = { viewModel.onStatusFilterSelected(status) })
                            }
                        }
                        MatchCalendar(dateList, selectedDate, currentLocale, onSelect = { viewModel.onDateSelected(it) })
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)
                        ) {
                            OutlinedButton(
                                onClick = viewModel::clearFilters,
                                enabled = hasSelectedFilters,
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                            ) { Text(stringResource(R.string.clear_filters), textAlign = TextAlign.Center) }
                            OutlinedButton(
                                onClick = viewModel::selectToday,
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                            ) { Text(stringResource(R.string.select_today), textAlign = TextAlign.Center) }
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
                        lastSuccessfulQuery?.let { queriedAt ->
                            val formatter = remember(currentLocale) {
                                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(currentLocale)
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
                                    Button(onClick = { viewModel.retryMatches() }) { Text(stringResource(R.string.try_again)) }
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
                                    viewModel.loadNextPage()
                                }
                            }
                            when {
                                error != null -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(error ?: R.string.error_unknown_fallback), color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                                    TextButton(onClick = { viewModel.retryMatches() }) { Text(stringResource(R.string.try_again)) }
                                }
                                isLoadingNextPage -> Box(Modifier.fillMaxWidth().padding(HubSpacing.Large), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                                hasMore && !isLoading && !isRefreshing -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    OutlinedButton(onClick = { viewModel.loadNextPage() }) { Text(stringResource(R.string.load_more_matches)) }
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

@Composable
private fun MatchFilterChip(label: String, isSelected: Boolean, usePurple: Boolean = false, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected, onClick = onClick,
        modifier = Modifier.heightIn(min = 48.dp),
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, when {
            !isSelected -> MaterialTheme.colorScheme.outlineVariant
            usePurple -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.primary
        }),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = if (usePurple) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = if (usePurple) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun TierFilter(selectedTier: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = HubSpacing.Small), modifier = Modifier.heightIn(min = 48.dp)) {
            Text(if (selectedTier == "Todos") stringResource(R.string.filter_tiers) else stringResource(R.string.tier_format, selectedTier),
                style = MaterialTheme.typography.labelMedium)
            Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("Todos", "S", "A", "B", "C").forEach { tier ->
                DropdownMenuItem(
                    text = { Text(if (tier == "Todos") stringResource(R.string.all_tiers) else stringResource(R.string.tier_format, tier)) },
                    onClick = { onSelect(tier); expanded = false },
                    modifier = Modifier.semantics { selected = selectedTier == tier }
                )
            }
        }
    }
}

@Composable
private fun MatchCalendar(dates: List<LocalDate>, selectedDate: LocalDate?, locale: Locale, onSelect: (LocalDate) -> Unit) {
    val descriptionFormatter = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale) }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
        items(dates, key = { it.toEpochDay() }) { date ->
            val isSelected = selectedDate == date
            val description = date.format(descriptionFormatter)
            Surface(
                onClick = { onSelect(date) }, shape = MaterialTheme.shapes.medium,
                color = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.widthIn(min = 52.dp).heightIn(min = 64.dp).semantics {
                    selected = isSelected
                    contentDescription = description
                }
            ) {
                Column(Modifier.padding(horizontal = HubSpacing.Small, vertical = HubSpacing.Small),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HubSpacing.ExtraSmall)) {
                    Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).replace(".", ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format(locale, "%02d", date.dayOfMonth), style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
