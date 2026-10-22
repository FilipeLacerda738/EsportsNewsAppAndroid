package com.example.esportsnews.feature.matchdetail.components

import com.example.esportsnews.core.ui.components.TeamImage
import com.example.esportsnews.R
import com.example.esportsnews.feature.matchdetail.countryFlagUrl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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

@Composable
fun TeamLogoAndName(team: TeamDetail?, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
        TeamImage(team?.name, team?.acronym, team?.darkModeImageUrl, team?.imageUrl,
            Modifier.size(HubDimensions.DetailTeamLogo).clip(CircleShape)
                .background(MaterialTheme.colorScheme.background).padding(HubSpacing.Small))
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
internal fun RosterTeamHeading(teamName: String?) {
    Text(teamName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.to_be_defined), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() }, color = MaterialTheme.colorScheme.onSurface)
}
