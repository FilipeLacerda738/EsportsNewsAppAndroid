package com.example.esportsnews.feature.matchdetail.components

import com.example.esportsnews.R
import com.example.esportsnews.core.links.openExternalWebLink
import com.example.esportsnews.core.time.formatMatchDate
import com.example.esportsnews.core.ui.components.StatusBadge
import com.example.esportsnews.feature.matchdetail.formatPrizepool
import com.example.esportsnews.feature.matchdetail.mapWinnerName
import com.example.esportsnews.feature.matchdetail.orderedStreams
import com.example.esportsnews.feature.matchdetail.streamChannel
import com.example.esportsnews.feature.matchdetail.streamPlatform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.esportsnews.data.remote.dto.*
import com.example.esportsnews.core.ui.theme.*
import java.util.Locale
import java.time.ZoneId
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
private fun LeagueHeader(match: MatchDetail) {
    val league = match.league
    val logo = league?.dark_mode_image_url?.takeIf { it.isNotBlank() }
        ?: league?.image_url?.takeIf { it.isNotBlank() }
    Row(
        Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(HubSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HubSpacing.Medium)
    ) {
        if (logo != null) {
            AsyncImage(
                model = logo,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HubSpacing.ExtraSmall)) {
            Text(
                league?.name?.takeIf { it.isNotBlank() } ?: stringResource(R.string.match_detail_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MatchFormatLine(match: MatchDetail) {
    val gameName = if (match.game.equals("CSGO", ignoreCase = true)) "CS2" else match.game.uppercase(Locale.ROOT)
    val format = match.numberOfGames?.takeIf { it > 0 }?.let { stringResource(R.string.best_of_format, it) }
    Row(
        Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(gameName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        if (format != null) {
            Text("  •  ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(format, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MatchMetadataCards(match: MatchDetail, locale: Locale, zone: ZoneId) {
    val date = formatMatchDate(
        match.beginAt,
        stringResource(R.string.to_be_defined),
        stringResource(R.string.undefined),
        locale,
        zone
    )
    val prizepool = match.prizepool?.takeIf { it.isNotBlank() }
    val tier = match.tier?.takeIf { it.isNotBlank() }

    val dateLabel = stringResource(R.string.match_date_label)
    val prizeLabel = stringResource(R.string.match_prizepool)
    val tierLabel = stringResource(R.string.match_tier_label)
    val entries = listOfNotNull(
        MetadataEntry(dateLabel, date, Icons.Default.CalendarMonth),
        prizepool?.let { MetadataEntry(prizeLabel, formatPrizepool(it, locale), Icons.Default.EmojiEvents, isPrize = true) },
        tier?.let { MetadataEntry(tierLabel, it.uppercase(Locale.ROOT)) }
    )
    val configuration = LocalConfiguration.current
    val compact = configuration.screenWidthDp < 380 || configuration.fontScale >= 1.25f
    if (compact) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
            entries.forEach { entry -> MetadataCard(entry, Modifier.fillMaxWidth()) }
        }
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
            entries.forEach { entry ->
                MetadataCard(entry, Modifier.weight(if (entry.isPrize) 1.3f else 1f))
            }
        }
    }
}

@Composable
private fun MetadataCard(entry: MetadataEntry, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(HubSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(HubSpacing.Small)
        ) {
            entry.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (entry.isPrize) TierGold else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(entry.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(entry.value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private data class MetadataEntry(
    val label: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val isPrize: Boolean = false
)

@Composable
fun MatchDetailContent(
    match: MatchDetail, error: Int? = null, onRetry: () -> Unit = {}, zone: ZoneId = ZoneId.systemDefault(),
    dataStale: Boolean = false, queriedAt: Instant? = null
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
        if (dataStale || queriedAt != null) {
            item(key = "data_freshness") {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
                    if (dataStale) Text(stringResource(R.string.cached_data_stale), style = MaterialTheme.typography.bodySmall)
                    queriedAt?.let {
                        val formatter = remember(locale, it, zone) {
                            if (it.atZone(zone).toLocalDate() == java.time.LocalDate.now(zone))
                                DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
                            else DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale)
                        }
                        Text(stringResource(R.string.last_successful_query, formatter.format(it.atZone(zone))),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
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
            Surface(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(HubSpacing.Large),
                    verticalArrangement = Arrangement.spacedBy(HubSpacing.Medium)
                ) {
                    LeagueHeader(match)
                    MatchFormatLine(match)
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)
                    ) {
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
            MatchMetadataCards(match, locale, zone)
        }
        item(key = "streams_heading") {
            Text(
                stringResource(R.string.streams_title),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().semantics { heading() },
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (streams.isEmpty()) {
            item(key = "no_streams") { Text(stringResource(R.string.no_streams), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            item(key = "streams_carousel") {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = HubSpacing.ExtraSmall),
                    horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)
                ) {
                    items(items = streams, key = { "stream_${it.raw_url.orEmpty()}" }) {
                        StreamCard(it, isLive = match.status.equals("running", ignoreCase = true))
                    }
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
                    Text(match.teamA?.name?.takeIf { it.isNotBlank() } ?: stringResource(R.string.to_be_defined), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall)
                    Text(match.teamB?.name?.takeIf { it.isNotBlank() } ?: stringResource(R.string.to_be_defined), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall)
                }
            }
            if (playersA.isEmpty() || playersB.isEmpty()) item(key = "roster_empty") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (playersA.isEmpty()) stringResource(R.string.no_players) else "", Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (playersB.isEmpty()) stringResource(R.string.no_players) else "", Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(count = maxOf(playersA.size, playersB.size), key = { "players_$it" }) { index ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f).fillMaxHeight()) { playersA.getOrNull(index)?.let { PlayerItem(it, Modifier.fillMaxHeight()) } }
                    Box(Modifier.weight(1f).fillMaxHeight()) { playersB.getOrNull(index)?.let { PlayerItem(it, Modifier.fillMaxHeight()) } }
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
private fun StreamCard(stream: Stream, isLive: Boolean) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val channel = streamChannel(stream.raw_url)
    val platform = streamPlatform(stream.raw_url)
    val cardWidth = (124f * configuration.fontScale.coerceAtLeast(1f))
        .coerceAtMost(configuration.screenWidthDp * 0.62f)
        .coerceAtLeast(112f)
    val labels = listOfNotNull(
        if (stream.official == true) stringResource(R.string.official_channel) else null,
        if (stream.main == true) stringResource(R.string.main_stream) else null,
        stream.language?.takeIf { it.matches(Regex("[a-zA-Z]{2,3}([_-][a-zA-Z]{2})?")) }?.uppercase(Locale.ROOT)
    )
    Card(
        onClick = { openExternalWebLink(context, stream.raw_url) },
        modifier = Modifier.width(cardWidth.dp).heightIn(min = 112.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(HubSpacing.Medium),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(HubSpacing.Small)
        ) {
            Text(channel ?: platform, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            val secondaryLabels = listOfNotNull(platform.takeIf { channel != null }) + labels
            secondaryLabels.takeIf { it.isNotEmpty() }?.let {
                Text(it.joinToString(" • "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(
                Modifier.fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(if (isLive) LiveRed else MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = HubSpacing.Small, vertical = HubSpacing.ExtraSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HubSpacing.ExtraSmall)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isLive) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    stringResource(if (isLive) R.string.watch_live_title else R.string.open_stream),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isLive) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
