package com.example.esportsnews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    matchId: Int,
    viewModel: MatchesViewModel,
    onNavigateBack: () -> Unit
) {
    val detail by viewModel.matchDetail.collectAsState()
    val isLoading by viewModel.isDetailLoading.collectAsState()
    val error by viewModel.detailError.collectAsState()
    LaunchedEffect(matchId) {
        viewModel.fetchMatchDetails(matchId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Partida", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearMatchDetails()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(text = error ?: "Erro desconhecido", color = MaterialTheme.colorScheme.error)
                detail != null -> MatchDetailContent(match = detail!!)
            }
        }
    }
}

@Composable
fun MatchDetailContent(match: MatchDetail) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamLogoAndName(team = match.teamA, modifier = Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${match.teamAScore} - ${match.teamBScore}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = match.status.uppercase(),
                        fontSize = 12.sp,
                        color = if (match.status == "running") Color.Red else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
                TeamLogoAndName(team = match.teamB, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        item {
            Text("ESCALAÇÕES", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    match.teamA?.players?.forEach { player ->
                        PlayerItem(player)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    match.teamB?.players?.forEach { player ->
                        PlayerItem(player)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        if (match.games.isNotEmpty()) {
            item {
                Text("MAPAS", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(match.games) { game ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Mapa ${game.position}")
                        Text(
                            text = game.status.uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = if (game.status == "running") Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeamLogoAndName(team: TeamDetail?, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        AsyncImage(
            model = team?.imageUrl ?: "https://via.placeholder.com/150",
            contentDescription = "Logo ${team?.name}",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = team?.acronym ?: team?.name ?: "TBD",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PlayerItem(player: Player) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        AsyncImage(
            model = player.imageUrl ?: "https://via.placeholder.com/150",
            contentDescription = "Foto ${player.name}",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = player.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (player.firstName != null && player.lastName != null) {
                Text(
                    text = "${player.firstName} ${player.lastName}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}