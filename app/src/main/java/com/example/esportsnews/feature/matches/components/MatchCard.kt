package com.example.esportsnews.feature.matches.components

import com.example.esportsnews.R
import com.example.esportsnews.core.time.formatMatchDate
import com.example.esportsnews.core.ui.components.StatusBadge
import com.example.esportsnews.core.ui.components.TeamProfile

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.esportsnews.data.remote.dto.Match
import com.example.esportsnews.core.ui.theme.*
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
