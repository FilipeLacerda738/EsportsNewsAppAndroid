package com.example.esportsnews.core.ui.components

import com.example.esportsnews.R
import com.example.esportsnews.core.links.isExternalWebUrl
import com.example.esportsnews.core.links.openExternalWebLink

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.esportsnews.core.ui.theme.*

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
