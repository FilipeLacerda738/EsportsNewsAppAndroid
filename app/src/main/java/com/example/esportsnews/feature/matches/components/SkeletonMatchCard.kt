package com.example.esportsnews.feature.matches.components

import androidx.compose.animation.core.*
import com.example.esportsnews.core.ui.components.shimmerEffect
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.esportsnews.core.ui.theme.*

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
