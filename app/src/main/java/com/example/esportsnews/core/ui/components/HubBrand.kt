package com.example.esportsnews.core.ui.components

import com.example.esportsnews.core.ui.components.VersionFooter
import com.example.esportsnews.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.esportsnews.core.ui.theme.HubSpacing

@Composable
internal fun HubBrand(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f, fill = false).semantics { heading() })
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.extraSmall) {
            Text(stringResource(R.string.beta_tag), color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = HubSpacing.Small, vertical = HubSpacing.ExtraSmall))
        }
    }
}
