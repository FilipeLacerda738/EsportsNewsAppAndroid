package com.example.esportsnews

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.esportsnews.model.*
import com.example.esportsnews.ui.theme.*

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
                title = { Text(stringResource(R.string.match_detail_title), fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearMatchDetails()
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
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
                error != null -> Text(text = error ?: stringResource(R.string.error_unknown_fallback), color = MaterialTheme.colorScheme.error)
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
                    Spacer(modifier = Modifier.height(8.dp))

                    StatusBadge(status = match.status, streamUrl = match.streamUrl)
                }

                TeamLogoAndName(team = match.teamB, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (!match.prizepool.isNullOrBlank()) {
            item {
                Surface(
                    color = AccentBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = "[ $ ] ",
                            fontWeight = FontWeight.Black,
                            color = AccentBlue,
                            fontSize = 16.sp
                        )
                        Text(
                            text = stringResource(id = R.string.match_prizepool),
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = match.prizepool,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        if (!match.streams.isNullOrEmpty()) {
            item {
                StreamChipsSection(streams = match.streams)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        item {
            Text(stringResource(R.string.rosters_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                Text(stringResource(R.string.maps_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val mapDisplayName = if (!game.mapName.isNullOrBlank()) {
                            stringResource(R.string.map_name_with_number, game.position.toString(), game.mapName)
                        } else {
                            stringResource(R.string.map_number_only, game.position.toString())
                        }

                        Text(
                            text = mapDisplayName,
                            fontWeight = FontWeight.Medium
                        )

                        val statusText = when (game.status.lowercase()) {
                            "running" -> stringResource(R.string.status_live).uppercase()
                            "finished" -> stringResource(R.string.status_finished).uppercase()
                            "not_started" -> stringResource(R.string.status_upcoming).uppercase()
                            else -> game.status.uppercase()
                        }

                        Text(
                            text = statusText,
                            fontWeight = FontWeight.Bold,
                            color = if (game.status.lowercase() == "running") LiveRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StreamChipsSection(streams: List<Stream>) {
    val uriHandler = LocalUriHandler.current
    val officialChannelText = stringResource(R.string.official_channel)
    val streamFallbackText = stringResource(R.string.stream_fallback)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.watch_live_title),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(streams) { stream ->
                val url = stream.raw_url ?: ""

                val platformName = when {
                    url.contains("twitch", ignoreCase = true) -> "Twitch"
                    url.contains("youtube", ignoreCase = true) -> "YouTube"
                    url.contains("kick", ignoreCase = true) -> "Kick"
                    else -> "Web"
                }

                val platformColor = when (platformName) {
                    "Twitch" -> Color(0xFF9146FF)
                    "YouTube" -> Color(0xFFFF0000)
                    "Kick" -> Color(0xFF53FC18)
                    else -> Color.DarkGray
                }

                val textColor = if (platformColor == Color(0xFF53FC18)) Color.Black else Color.White

                val channelName = if (platformName == "YouTube" && url.contains("watch")) {
                    officialChannelText
                } else {
                    url.trimEnd('/').substringAfterLast("/").replaceFirstChar { it.uppercase() }
                }.ifBlank { streamFallbackText }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(platformColor.copy(alpha = 0.15f))
                        .clickable {
                            try { uriHandler.openUri(url) } catch (e: Exception) { e.printStackTrace() }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(platformColor, RoundedCornerShape(50))
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "$channelName • $platformName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "[${stream.language.uppercase()}]",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor.copy(alpha = 0.7f)
                    )
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
        val teamName = team?.name ?: ""
        AsyncImage(
            model = team?.darkModeImageUrl ?: team?.imageUrl ?: "https://via.placeholder.com/150",
            contentDescription = stringResource(R.string.cd_team_logo, teamName),
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (team?.location != null) {
                AsyncImage(
                    model = "https://flagcdn.com/w40/${team.location.lowercase()}.png",
                    contentDescription = null,
                    modifier = Modifier
                        .height(14.dp)
                        .width(18.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = team?.acronym ?: team?.name ?: stringResource(R.string.tbd),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
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
            contentDescription = stringResource(R.string.cd_player_photo, player.name),
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