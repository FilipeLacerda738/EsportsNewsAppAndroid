package com.example.esportsnews

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.contentDescription
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.example.esportsnews.ui.theme.*
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(viewModel: MatchesViewModel, onMatchClick: (Int) -> Unit) {
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
    val hasLocalFilter = searchQuery.isNotBlank() || selectedDate != null

    val updateData by viewModel.updateAvailable.collectAsState()
    val updateCheckState by viewModel.updateCheckState.collectAsState()
    val context = LocalContext.current
    val currentLocale = LocalConfiguration.current.locales[0]

    updateData?.let { updateInfo ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissUpdateDialog() },
            title = { Text(text = stringResource(R.string.update_available_title), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = stringResource(R.string.update_available_desc, updateInfo.version.orEmpty(), updateInfo.releaseNotes.orEmpty()),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (openExternalWebLink(context, updateInfo.downloadUrl)) {
                            viewModel.dismissUpdateDialog()
                        }
                    }
                ) {
                    Text(stringResource(R.string.update_open_release), fontWeight = FontWeight.Bold, color = AccentBlue)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissUpdateDialog() }
                ) {
                    Text(stringResource(R.string.update_download_no), color = TextSecondary)
                }
            }
        )
    }

    Scaffold(containerColor = DarkBackground) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.fetchMatches(isRefresh = true) },
            modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "filters") {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, top = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.app_name_caps),
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentBlue,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.beta_tag),
                                        color = DarkBackground,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            LanguageSelector()
                        }

                        TextButton(
                            onClick = { viewModel.checkForUpdates() },
                            enabled = updateCheckState != UpdateCheckState.Checking
                        ) {
                            Text(
                                stringResource(
                                    if (updateCheckState == UpdateCheckState.Checking) R.string.update_checking
                                    else R.string.update_check
                                ),
                                color = TextSecondary
                            )
                        }
                        val updateStatus = when (updateCheckState) {
                            UpdateCheckState.UpToDate -> R.string.update_up_to_date
                            UpdateCheckState.Available -> R.string.update_available_title
                            UpdateCheckState.Failed -> R.string.update_check_failed
                            else -> null
                        }
                        updateStatus?.let {
                            Text(
                                text = stringResource(it),
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 8.dp).semantics {
                                    liveRegion = LiveRegionMode.Polite
                                }
                            )
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.search_team), color = TextSecondary) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CardSurface,
                                unfocusedContainerColor = CardSurface,
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val games = listOf("Todos", "CS2", "VALORANT")
                            items(games) { game ->
                                val displayText = if (game == "Todos") stringResource(R.string.filter_all) else game

                                FilterChip(
                                    selected = selectedGame == game,
                                    onClick = { viewModel.onGameFilterSelected(game) },
                                    label = { Text(displayText, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = CardSurface,
                                        labelColor = TextSecondary,
                                        selectedContainerColor = AccentBlue.copy(alpha = 0.2f),
                                        selectedLabelColor = AccentBlue
                                    ),
                                    border = null,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val tiers = listOf("Todos", "S", "A", "B", "C")
                            items(tiers) { tier ->
                                val displayText = if (tier == "Todos") stringResource(R.string.filter_all) else stringResource(R.string.tier_format, tier)

                                val activeColor = when (tier) {
                                    "S" -> Color(0xFFFFD700)
                                    "A" -> Color(0xFFC0C0C0)
                                    "B" -> Color(0xFFCD7F32)
                                    else -> AccentBlue
                                }

                                FilterChip(
                                    selected = selectedTier == tier,
                                    onClick = { viewModel.onTierFilterSelected(tier) },
                                    label = { Text(displayText, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = CardSurface,
                                        labelColor = TextSecondary,
                                        selectedContainerColor = activeColor.copy(alpha = 0.2f),
                                        selectedLabelColor = activeColor
                                    ),
                                    border = null,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(end = 16.dp)
                        ) {
                            items(dateList) { date ->
                                val isDateSelected = selectedDate == date
                                val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, currentLocale)
                                    .replaceFirstChar { it.uppercase() }.replace(".", "")
                                val dayOfMonth = String.format(currentLocale, "%02d", date.dayOfMonth)

                                val dateDescription = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(currentLocale))
                                Surface(
                                    onClick = { viewModel.onDateSelected(date) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isDateSelected) AccentBlue else CardSurface,
                                    modifier = Modifier
                                        .width(54.dp)
                                        .semantics {
                                            selected = isDateSelected
                                            contentDescription = dateDescription
                                        }
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = dayOfWeek,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDateSelected) DarkBackground.copy(alpha = 0.8f) else TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = dayOfMonth,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isDateSelected) DarkBackground else TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            val statusFilters = listOf("Todos", "Ao Vivo", "Em breve", "Finalizados")
                            items(statusFilters) { status ->
                                val displayText = when(status) {
                                    "Todos" -> stringResource(R.string.filter_all)
                                    "Ao Vivo" -> stringResource(R.string.status_live)
                                    "Em breve" -> stringResource(R.string.status_upcoming)
                                    "Finalizados" -> stringResource(R.string.status_finished)
                                    else -> status
                                }

                                val isSelected = selectedStatus == status

                                Surface(
                                    onClick = { viewModel.onStatusFilterSelected(status) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) AccentBlue else CardSurface,
                                    shadowElevation = if (isSelected) 6.dp else 0.dp,
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = displayText,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isSelected) DarkBackground else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                when {
                    (isLoading || isRefreshing) && matches.isEmpty() -> {
                        items(8) { SkeletonMatchCard() }
                    }
                    error != null && matches.isEmpty() -> {
                        item(key = "initial_error") {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = TextSecondary)
                                Text(stringResource(error ?: R.string.error_unknown_fallback), color = TextPrimary, textAlign = TextAlign.Center)
                                Button(onClick = { viewModel.retryMatches() }) {
                                    Text(stringResource(R.string.try_again))
                                }
                            }
                        }
                    }
                    else -> {
                        if (hasLocalFilter || matches.isEmpty()) {
                            item(key = "filter_hint") {
                                Text(
                                    text = stringResource(
                                        if (hasLocalFilter && hasMore) R.string.local_filter_partial
                                        else if (hasLocalFilter) R.string.local_filter_empty_hint
                                        else R.string.matches_empty
                                    ),
                                    color = TextSecondary,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        items(items = matches, key = { it.id }) { match ->
                            ModernMatchCard(match = match, onClick = { onMatchClick(match.id) }, zone = deviceZone)
                        }
                        item(key = "pagination") {
                            if (!hasLocalFilter && hasMore && error == null && !isLoading &&
                                !isRefreshing && !isLoadingNextPage && matches.isNotEmpty()) {
                                LaunchedEffect(matches.size, hasMore) { viewModel.loadNextPage() }
                            }
                            when {
                                error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text(stringResource(error ?: R.string.error_unknown_fallback), color = TextSecondary)
                                    TextButton(onClick = { viewModel.retryMatches() }) { Text(stringResource(R.string.try_again)) }
                                }
                                isLoadingNextPage -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = AccentBlue)
                                }
                                hasMore && !isLoading && !isRefreshing -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    TextButton(onClick = { viewModel.loadNextPage() }) { Text(stringResource(R.string.load_more_matches)) }
                                }
                                else -> Spacer(Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
