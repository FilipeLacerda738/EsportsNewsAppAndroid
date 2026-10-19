package com.example.esportsnews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
                title = { Text(stringResource(R.string.match_detail_title), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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

@OptIn(ExperimentalLayoutApi::class)
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

    val configuration = LocalConfiguration.current
    val compactRoster = configuration.screenWidthDp < 400 || configuration.fontScale >= 1.25f
    val compactHero = configuration.screenWidthDp < 340 || configuration.fontScale >= 1.5f
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(HubSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(HubSpacing.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (error != null) {
            item(key = "refresh_error") {
                Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.errorContainer) {
                    Column(Modifier.fillMaxWidth().padding(HubSpacing.Medium), verticalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
                        Text(stringResource(error), color = MaterialTheme.colorScheme.onErrorContainer)
                        TextButton(onClick = onRetry) { Text(stringResource(R.string.try_again)) }
                    }
                }
            }
        }
        item(key = "score") {
            Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                Column(Modifier.fillMaxWidth().padding(HubSpacing.Large), verticalArrangement = Arrangement.spacedBy(HubSpacing.Medium)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
                        TeamLogoAndName(match.teamA, Modifier.weight(1f))
                        if (!compactHero) ScoreSummary(match, Modifier.widthIn(min = 72.dp).heightIn(min = HubDimensions.DetailTeamLogo))
                        TeamLogoAndName(match.teamB, Modifier.weight(1f))
                    }
                    if (compactHero) ScoreSummary(match, Modifier.fillMaxWidth())
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        StatusBadge(match.status, match.streamUrl)
                    }
                }
            }
        }
        item(key = "context") {
            Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
              Column(Modifier.fillMaxWidth().padding(HubSpacing.Large), verticalArrangement = Arrangement.spacedBy(HubSpacing.Medium)) {
                match.league?.name?.takeIf { it.isNotBlank() }?.let {
                    Text(it, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() }, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(formatMatchDate(match.beginAt, stringResource(R.string.to_be_defined), stringResource(R.string.undefined), locale, zone), color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small), verticalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
                    match.numberOfGames?.takeIf { it > 0 }?.let { MetadataPill(stringResource(R.string.best_of_format, it)) }
                    match.tier?.takeIf { it.isNotBlank() }?.let { MetadataPill(stringResource(R.string.tier_format, it.uppercase(Locale.ROOT))) }
                    match.prizepool?.takeIf { it.isNotBlank() }?.let { MetadataPill("${stringResource(R.string.match_prizepool)} $it") }
                }
              }
            }
        }
        item(key = "streams_heading") {
            Text(stringResource(R.string.watch_live_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().semantics { heading() }, color = MaterialTheme.colorScheme.onSurface)
        }
        if (streams.isEmpty()) {
            item(key = "no_streams") { Text(stringResource(R.string.no_streams), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(items = streams, key = { "stream_${it.raw_url.orEmpty()}" }) { stream -> StreamButton(stream) }
        }
        item(key = "players_heading") {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.rosters_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() }, color = MaterialTheme.colorScheme.onSurface)
                Text(stringResource(R.string.rosters_hint), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (compactRoster) {
            item(key = "team_a_heading") { RosterTeamHeading(match.teamA?.name) }
            if (playersA.isEmpty()) item(key = "team_a_empty") { Text(stringResource(R.string.no_players), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(count = playersA.size, key = { "player_a_$it" }) { index -> PlayerItem(playersA[index]) }
            item(key = "team_b_heading") { RosterTeamHeading(match.teamB?.name) }
            if (playersB.isEmpty()) item(key = "team_b_empty") { Text(stringResource(R.string.no_players), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(count = playersB.size, key = { "player_b_$it" }) { index -> PlayerItem(playersB[index]) }
        } else {
            item(key = "roster_team_headings") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(match.teamA?.name ?: stringResource(R.string.to_be_defined), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall)
                    Text(match.teamB?.name ?: stringResource(R.string.to_be_defined), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall)
                }
            }
            if (playersA.isEmpty() || playersB.isEmpty()) item(key = "roster_empty") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (playersA.isEmpty()) stringResource(R.string.no_players) else "", Modifier.weight(1f), textAlign = TextAlign.Center, color = TextSecondary)
                    Text(if (playersB.isEmpty()) stringResource(R.string.no_players) else "", Modifier.weight(1f), textAlign = TextAlign.Center, color = TextSecondary)
                }
            }
            items(count = maxOf(playersA.size, playersB.size), key = { "players_$it" }) { index ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f).fillMaxHeight()) { playersA.getOrNull(index)?.let { PlayerItem(it, Modifier.fillMaxHeight()) } }
                    Box(Modifier.weight(1f).fillMaxHeight()) { playersB.getOrNull(index)?.let { PlayerItem(it, Modifier.fillMaxHeight()) } }
                }
            }
        }
        item(key = "maps_heading") {
            Text(stringResource(R.string.maps_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().semantics { heading() }, color = MaterialTheme.colorScheme.onSurface)
            if (maps.isEmpty()) Text(stringResource(R.string.no_maps), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(items = maps, key = { "map_${it.id}" }) { game ->
            Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(HubSpacing.Medium), verticalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
                    Text(
                        if (!game.mapName.isNullOrBlank()) stringResource(R.string.map_name_with_number, game.position.toString(), game.mapName)
                        else stringResource(R.string.map_number_only, game.position.toString()),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.semantics { heading() }
                    )
                    StatusBadge(game.status, null)
                    mapWinnerName(game.winnerId, match.teamA, match.teamB)?.let {
                        Text(stringResource(R.string.map_winner, it), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreSummary(match: MatchDetail, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        if (match.status != "not_started") {
            Text(
                "${match.teamAScore} : ${match.teamBScore}",
                style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(stringResource(R.string.match_versus), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun StreamButton(stream: Stream) {
    val context = LocalContext.current
    val labels = listOfNotNull(
        streamChannel(stream.raw_url),
        streamPlatform(stream.raw_url),
        if (stream.official == true) stringResource(R.string.official_channel) else null,
        if (stream.main == true) stringResource(R.string.main_stream) else null,
        stream.language?.takeIf { it.matches(Regex("[a-zA-Z]{2,3}([_-][a-zA-Z]{2})?")) }?.uppercase(Locale.ROOT)
    )
    OutlinedButton(
        onClick = { openExternalWebLink(context, stream.raw_url) },
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(HubSpacing.ExtraSmall)) {
            Text(labels.firstOrNull() ?: stringResource(R.string.watch_live_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            labels.drop(1).takeIf { it.isNotEmpty() }?.let {
                Text(it.joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TeamLogoAndName(team: TeamDetail?, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
        val fallback = painterResource(R.drawable.ic_team_placeholder)
        AsyncImage(
            model = team?.darkModeImageUrl?.takeIf { it.isNotBlank() } ?: team?.imageUrl?.takeIf { it.isNotBlank() },
            contentDescription = null,
            placeholder = fallback, error = fallback, fallback = fallback,
            modifier = Modifier.size(HubDimensions.DetailTeamLogo).clip(CircleShape).background(MaterialTheme.colorScheme.background).padding(HubSpacing.Small),
            contentScale = ContentScale.Fit
        )
        Text(team?.name?.takeIf { it.isNotBlank() } ?: stringResource(R.string.to_be_defined), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall)
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
fun PlayerItem(player: Player, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
      Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        val fallback = painterResource(R.drawable.ic_player_placeholder)
        AsyncImage(
            model = player.imageUrl?.takeIf { it.isNotBlank() }, contentDescription = null,
            placeholder = fallback, error = fallback, fallback = fallback,
            modifier = Modifier.size(HubDimensions.PlayerPhoto).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(player.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            listOfNotNull(player.firstName?.takeIf { it.isNotBlank() }, player.lastName?.takeIf { it.isNotBlank() })
                .joinToString(" ").takeIf { it.isNotBlank() }?.let {
                    Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun MetadataPill(text: String) {
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(text, Modifier.padding(horizontal = HubSpacing.Small, vertical = HubSpacing.ExtraSmall), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RosterTeamHeading(teamName: String?) {
    Text(teamName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.to_be_defined), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() }, color = MaterialTheme.colorScheme.onSurface)
}
