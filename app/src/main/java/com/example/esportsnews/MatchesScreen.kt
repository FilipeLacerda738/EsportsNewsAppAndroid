package com.example.esportsnews

import android.content.Intent
import androidx.compose.foundation.clickable
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
import androidx.core.net.toUri
import com.example.esportsnews.ui.theme.*
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(viewModel: MatchesViewModel, onMatchClick: (Int) -> Unit) {
    val matches by viewModel.matches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()

    val selectedGame by viewModel.selectedGame.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedTier by viewModel.selectedTier.collectAsState()

    val updateData by viewModel.updateAvailable.collectAsState()
    val context = LocalContext.current
    val currentLocale = LocalConfiguration.current.locales[0]

    updateData?.let { updateInfo ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissUpdateDialog() },
            title = { Text(text = stringResource(R.string.update_available_title), fontWeight = FontWeight.Bold) },
            text = {
                Text(text = stringResource(R.string.update_available_desc, updateInfo.version, updateInfo.releaseNotes))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, updateInfo.downloadUrl.toUri())
                        context.startActivity(intent)
                        viewModel.dismissUpdateDialog()
                    }
                ) {
                    Text(stringResource(R.string.update_download_yes), fontWeight = FontWeight.Bold, color = AccentBlue)
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

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, top = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name_caps),
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AccentBlue,
                        modifier = Modifier.height(20.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.beta_tag),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    LanguageSelector()
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
                    items(viewModel.dateList) { date ->
                        val isDateSelected = selectedDate == date
                        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, currentLocale)
                            .replaceFirstChar { it.uppercase() }.replace(".", "")
                        val dayOfMonth = String.format(currentLocale, "%02d", date.dayOfMonth)

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDateSelected) AccentBlue else CardSurface,
                            modifier = Modifier
                                .width(54.dp)
                                .clickable { viewModel.onDateSelected(date) }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = dayOfWeek,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDateSelected) Color.White.copy(alpha = 0.8f) else TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dayOfMonth,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDateSelected) Color.White else TextPrimary
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
                            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = displayText,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.fetchMatches(isRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && !isRefreshing && matches.isEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(8) {
                        SkeletonMatchCard()
                    }
                }
            }
            else if (error != null && matches.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Erro de conexão",
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.error_oops),
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error ?: stringResource(R.string.error_unknown_fallback),
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.fetchMatches(isRefresh = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text(stringResource(R.string.try_again), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items = matches, key = { match -> match.id }) { match ->
                        ModernMatchCard(match = match, onClick = { onMatchClick(match.id) })
                    }
                    item {
                        val isLoadingNextPage by viewModel.isLoadingNextPage.collectAsState()
                        if (matches.isNotEmpty()) {
                            LaunchedEffect(matches.size) {
                                viewModel.loadNextPage()
                            }
                        }

                        if (isLoadingNextPage) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = AccentBlue,
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}