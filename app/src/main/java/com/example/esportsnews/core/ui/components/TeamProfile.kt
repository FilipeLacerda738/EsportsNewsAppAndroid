package com.example.esportsnews.core.ui.components

import com.example.esportsnews.R
import com.example.esportsnews.feature.matchdetail.countryFlagUrl

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.esportsnews.data.remote.dto.Team
import com.example.esportsnews.core.ui.theme.*

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
