package com.example.esportsnews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esportsnews.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(viewModel: MatchesViewModel) {
    val matches by viewModel.matches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()

    val selectedGame by viewModel.selectedGame.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val localeBr = Locale("pt", "BR")

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
                ) {
                    Text(
                        text = "ESPORTS HUB",
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
                                text = "BETA",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar time...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextSecondary) },
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
                    val filters = listOf("Todos", "CS2", "VALORANT")
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedGame == filter,
                            onClick = { viewModel.onGameFilterSelected(filter) },
                            label = { Text(filter, fontWeight = FontWeight.Bold) },
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

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(viewModel.dateList) { date ->
                        val isDateSelected = selectedDate == date
                        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, localeBr)
                            .replaceFirstChar { it.uppercase() }.replace(".", "")
                        val dayOfMonth = String.format(localeBr, "%02d", date.dayOfMonth)

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
                    items(statusFilters) { filter ->
                        val isSelected = selectedStatus == filter

                        Surface(
                            onClick = { viewModel.onStatusFilterSelected(filter) },
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
                                    text = filter,
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
            if (isLoading && !isRefreshing) {
                CircularProgressIndicator(color = AccentBlue, modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(text = error ?: "Erro", color = LiveRed, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items = matches, key = { match -> match.id }) { match ->
                        ModernMatchCard(match = match)
                    }
                }
            }
        }
    }
}