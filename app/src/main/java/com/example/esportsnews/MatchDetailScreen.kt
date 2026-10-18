package com.example.esportsnews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.esportsnews.model.*
import com.example.esportsnews.ui.theme.*
import java.util.Locale
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    matchId: Int,
    viewModel: MatchesViewModel,
    lifecycle: Lifecycle,
    onNavigateBack: () -> Unit
) {
    val detail by viewModel.matchDetail.collectAsState()
    val isLoading by viewModel.isDetailLoading.collectAsState()
    val isRefreshing by viewModel.isDetailRefreshing.collectAsState()
    val error by viewModel.detailError.collectAsState()
    val deviceZone by viewModel.deviceZone.collectAsState()
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
    val currentDetail = detail?.takeIf { it.id == matchId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.match_detail_title), fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = refresh, enabled = session != null && !isLoading && !isRefreshing) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh_match))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = refresh,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (currentDetail != null) {
                MatchDetailContent(currentDetail, error, refresh, deviceZone)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        if (isLoading || session == null) {
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

@Composable
fun MatchDetailContent(
    match: MatchDetail, error: Int? = null, onRetry: () -> Unit = {}, zone: ZoneId = ZoneId.systemDefault()
) {
    val locale = LocalConfiguration.current.locales[0]
    val streams = orderedStreams(match.streams, locale.language).ifEmpty {
        orderedStreams(listOf(Stream(null, match.streamUrl, null, null, null)), locale.language)
    }
    val playersA = match.teamA?.players.orEmpty().filterNotNull()
    val playersB = match.teamB?.players.orEmpty().filterNotNull()
    val maps = match.games.orEmpty().filterNotNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (error != null) {
            item(key = "refresh_error") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(error), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    TextButton(onClick = onRetry) { Text(stringResource(R.string.try_again)) }
                }
            }
        }
        item(key = "score") {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TeamLogoAndName(match.teamA, Modifier.weight(1f))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (match.status != "not_started") {
                        Text("${match.teamAScore} - ${match.teamBScore}", fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                    StatusBadge(match.status, match.streamUrl)
                }
                TeamLogoAndName(match.teamB, Modifier.weight(1f))
            }
        }
        item(key = "context") {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                match.league?.name?.takeIf { it.isNotBlank() }?.let {
                    Text(it, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Text(formatMatchDate(match.beginAt, stringResource(R.string.to_be_defined), stringResource(R.string.undefined), locale, zone))
                match.numberOfGames?.takeIf { it > 0 }?.let { Text(stringResource(R.string.best_of_format, it)) }
                match.tier?.takeIf { it.isNotBlank() }?.let { Text(stringResource(R.string.tier_format, it.uppercase(Locale.ROOT))) }
                match.prizepool?.takeIf { it.isNotBlank() }?.let {
                    Text("${stringResource(R.string.match_prizepool)} $it")
                }
            }
        }
        item(key = "streams") {
            if (streams.isEmpty()) {
                Text(stringResource(R.string.no_streams), color = TextSecondary)
            } else {
                StreamChipsSection(streams)
            }
        }
        item(key = "players_heading") {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.rosters_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(stringResource(R.string.rosters_hint), color = TextSecondary)
                Row(Modifier.fillMaxWidth()) {
                    Text(match.teamA?.name ?: stringResource(R.string.to_be_defined), Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text(match.teamB?.name ?: stringResource(R.string.to_be_defined), Modifier.weight(1f), textAlign = TextAlign.Center)
                }
                if (playersA.isEmpty() || playersB.isEmpty()) {
                    Row(Modifier.fillMaxWidth()) {
                        Text(if (playersA.isEmpty()) stringResource(R.string.no_players) else "", Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(if (playersB.isEmpty()) stringResource(R.string.no_players) else "", Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
            }
        }
        items(count = maxOf(playersA.size, playersB.size), key = { "players_$it" }) { index ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) { playersA.getOrNull(index)?.let { PlayerItem(it) } }
                Box(Modifier.weight(1f)) { playersB.getOrNull(index)?.let { PlayerItem(it) } }
            }
        }
        item(key = "maps_heading") {
            Text(stringResource(R.string.maps_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (maps.isEmpty()) Text(stringResource(R.string.no_maps), color = TextSecondary)
        }
        items(items = maps, key = { "map_${it.id}" }) { game ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardHeader)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (!game.mapName.isNullOrBlank()) stringResource(R.string.map_name_with_number, game.position.toString(), game.mapName)
                        else stringResource(R.string.map_number_only, game.position.toString()),
                        fontWeight = FontWeight.Medium
                    )
                    StatusBadge(game.status, null)
                    mapWinnerName(game.winnerId, match.teamA, match.teamB)?.let {
                        Text(stringResource(R.string.map_winner, it), color = AccentBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun StreamChipsSection(streams: List<Stream>) {
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.watch_live_title), fontWeight = FontWeight.Bold, color = TextSecondary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items = streams, key = { it.raw_url.orEmpty() }) { stream ->
                val labels = listOfNotNull(
                    streamChannel(stream.raw_url),
                    streamPlatform(stream.raw_url),
                    if (stream.official == true) stringResource(R.string.official_channel) else null,
                    if (stream.main == true) stringResource(R.string.main_stream) else null,
                    stream.language?.takeIf { it.matches(Regex("[a-zA-Z]{2,3}([_-][a-zA-Z]{2})?")) }?.uppercase(Locale.ROOT)
                )
                OutlinedButton(
                    onClick = { openExternalWebLink(context, stream.raw_url) },
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) { Text(labels.joinToString(" • ")) }
            }
        }
    }
}

@Composable
fun TeamLogoAndName(team: TeamDetail?, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val fallback = painterResource(R.drawable.ic_team_placeholder)
        AsyncImage(
            model = team?.darkModeImageUrl?.takeIf { it.isNotBlank() } ?: team?.imageUrl?.takeIf { it.isNotBlank() },
            contentDescription = null,
            placeholder = fallback, error = fallback, fallback = fallback,
            modifier = Modifier.size(64.dp).clip(CircleShape).background(CardHeader).padding(8.dp),
            contentScale = ContentScale.Fit
        )
        Text(team?.name?.takeIf { it.isNotBlank() } ?: stringResource(R.string.to_be_defined), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        countryFlagUrl(team?.location)?.let {
            AsyncImage(
                model = it,
                contentDescription = stringResource(R.string.country_code, team?.location.orEmpty().uppercase(Locale.ROOT)),
                modifier = Modifier.size(width = 20.dp, height = 16.dp), contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun PlayerItem(player: Player) {
    Column(Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val fallback = painterResource(R.drawable.ic_player_placeholder)
        AsyncImage(
            model = player.imageUrl?.takeIf { it.isNotBlank() }, contentDescription = null,
            placeholder = fallback, error = fallback, fallback = fallback,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(CardHeader),
            contentScale = ContentScale.Crop
        )
        Text(player.name, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        listOfNotNull(player.firstName?.takeIf { it.isNotBlank() }, player.lastName?.takeIf { it.isNotBlank() })
            .joinToString(" ").takeIf { it.isNotBlank() }?.let {
                Text(it, color = TextSecondary, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
    }
}
