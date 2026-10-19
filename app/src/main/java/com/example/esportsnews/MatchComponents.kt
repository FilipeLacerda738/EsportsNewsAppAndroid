package com.example.esportsnews

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.esportsnews.model.Match
import com.example.esportsnews.model.Team
import com.example.esportsnews.ui.theme.*
import java.time.ZoneId

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModernMatchCard(
    match: Match,
    onClick: () -> Unit,
    zone: ZoneId = ZoneId.systemDefault(),
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val stackedScore = configuration.fontScale >= 1.5f || configuration.screenWidthDp < 340
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(AccentContainer.copy(alpha = 0.55f), CardSurface, PurpleContainer.copy(alpha = 0.22f))))
                .padding(HubSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(HubSpacing.Small)
        ) {
            if (stackedScore) {
                ChampionshipIdentity(match, Modifier.fillMaxWidth())
                MatchLabels(match)
            } else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
                    ChampionshipIdentity(match, Modifier.weight(1f))
                    MatchLabels(match)
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
                Text(
                    if (match.game.equals("CSGO", ignoreCase = true)) "CS2" else match.game.uppercase(java.util.Locale.ROOT),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(match.status, match.stream_url)
            }
            // Top alignment keeps logos and score aligned even when team names wrap.
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
                TeamProfile(match.team_a, Modifier.weight(1f))
                if (!stackedScore) MatchScore(match, Modifier.widthIn(min = 64.dp).heightIn(min = HubDimensions.TeamLogo))
                TeamProfile(match.team_b, Modifier.weight(1f))
            }
            if (stackedScore) MatchScore(match, Modifier.fillMaxWidth())
            Text(
                formatMatchDate(match.begin_at, stringResource(R.string.to_be_defined), stringResource(R.string.undefined),
                    LocalConfiguration.current.locales[0], zone),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ChampionshipIdentity(match: Match, modifier: Modifier = Modifier) {
    match.league?.let { league ->
        Row(modifier, verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
            val logo = league.dark_mode_image_url?.takeIf { it.isNotBlank() } ?: league.image_url
            if (!logo.isNullOrBlank()) {
                val fallback = painterResource(R.drawable.ic_team_placeholder)
                AsyncImage(model = logo, contentDescription = null,
                    placeholder = fallback, error = fallback, fallback = fallback,
                    modifier = Modifier.size(40.dp).clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.background).padding(HubSpacing.ExtraSmall),
                    contentScale = ContentScale.Fit)
            }
            league.name?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface)
            }
        }
    } ?: Spacer(modifier)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchLabels(match: Match) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(HubSpacing.ExtraSmall)) {
        match.tier?.trim()?.takeIf { it.isNotEmpty() }?.let { tier ->
            val normalized = tier.uppercase(java.util.Locale.ROOT)
            val accent = when (normalized) {
                "S" -> TierGold
                "A" -> Purple80
                "B" -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            val surface = when (normalized) {
                "S" -> TierGoldSurface
                "A" -> TierPurpleSurface
                "B" -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            Surface(shape = MaterialTheme.shapes.extraSmall, color = surface,
                border = BorderStroke(1.dp, accent)) {
                Text(stringResource(R.string.tier_format, normalized),
                    style = MaterialTheme.typography.labelMedium, color = accent,
                    modifier = Modifier.padding(horizontal = HubSpacing.Small, vertical = HubSpacing.ExtraSmall))
            }
        }
        match.number_of_games?.takeIf { it > 0 }?.let {
            Surface(shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Text(stringResource(R.string.best_of_format, it), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = HubSpacing.Small, vertical = HubSpacing.ExtraSmall))
            }
        }
    }
}

@Composable
private fun MatchScore(match: Match, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(
            if (match.status.equals("not_started", ignoreCase = true)) stringResource(R.string.match_versus)
            else "${match.team_a_score} : ${match.team_b_score}",
            style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TeamProfile(team: Team?, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
        TeamLogo(team)
        TeamName(team)
    }
}

@Composable
private fun TeamLogo(team: Team?, modifier: Modifier = Modifier) {
    val fallback = painterResource(R.drawable.ic_team_placeholder)
    AsyncImage(
        model = team?.dark_mode_image_url?.takeIf { it.isNotBlank() } ?: team?.image_url?.takeIf { it.isNotBlank() },
        contentDescription = null,
        placeholder = fallback, error = fallback, fallback = fallback,
        modifier = modifier.size(HubDimensions.TeamLogo).clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.background).padding(HubSpacing.ExtraSmall),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun TeamName(team: Team?, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HubSpacing.ExtraSmall)) {
        Text(team?.name?.takeIf { it.isNotBlank() } ?: stringResource(R.string.to_be_defined),
            style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        countryFlagUrl(team?.location)?.let {
            AsyncImage(model = it, contentDescription = stringResource(R.string.country_code, team?.location.orEmpty()),
                modifier = Modifier.size(width = 16.dp, height = 12.dp), contentScale = ContentScale.Fit)
        }
    }
}

@Composable
fun StatusBadge(status: String, streamUrl: String?, modifier: Modifier = Modifier) {
    val isLive = status.equals("running", ignoreCase = true)
    val badgeColor = when {
        isLive -> LiveRed
        status.equals("finished", ignoreCase = true) -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.primary
    }
    val displayText = when (status.lowercase()) {
        "running" -> stringResource(R.string.status_live)
        "finished" -> stringResource(R.string.status_finished)
        "not_started" -> stringResource(R.string.status_upcoming)
        else -> stringResource(R.string.undefined)
    }
    val context = LocalContext.current
    val watchLabel = stringResource(R.string.watch_live_title)
    Box(
        modifier = modifier.then(
            if (isLive && isExternalWebUrl(streamUrl)) {
                Modifier.sizeIn(minHeight = 48.dp, minWidth = 48.dp).clip(MaterialTheme.shapes.small)
                    .clickable(role = Role.Button, onClickLabel = watchLabel) { openExternalWebLink(context, streamUrl) }
            } else Modifier
        ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier.clip(MaterialTheme.shapes.extraSmall)
                .background(if (isLive) LiveRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = HubSpacing.Small, vertical = HubSpacing.ExtraSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HubSpacing.ExtraSmall)
        ) {
            if (isLive) BlinkingDot()
            Text(displayText, color = badgeColor, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun BlinkingDot() {
    if (!LocalAnimationsEnabled.current) {
        Box(Modifier.size(6.dp).background(LiveRed, CircleShape))
        return
    }
    val infiniteTransition = rememberInfiniteTransition(label = "BlinkingDot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 800, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "AlphaAnimation"
    )
    Box(Modifier.size(6.dp).alpha(alpha).background(LiveRed, CircleShape))
}

@Composable
fun SkeletonMatchCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(HubSpacing.Medium), verticalArrangement = Arrangement.spacedBy(HubSpacing.Medium)) {
            Box(Modifier.height(16.dp).fillMaxWidth(0.6f).clip(MaterialTheme.shapes.extraSmall).shimmerEffect())
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(HubDimensions.TeamLogo).clip(MaterialTheme.shapes.small).shimmerEffect())
                    }
                }
            }
            Box(Modifier.height(16.dp).fillMaxWidth().clip(MaterialTheme.shapes.extraSmall).shimmerEffect())
            Box(Modifier.height(12.dp).fillMaxWidth(0.4f).clip(MaterialTheme.shapes.extraSmall).shimmerEffect())
        }
    }
}
