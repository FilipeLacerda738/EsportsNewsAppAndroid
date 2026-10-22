package com.example.esportsnews.app.navigation

import com.example.esportsnews.core.ui.components.VersionFooter
import com.example.esportsnews.core.ui.components.HubBrand
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
internal fun HubDrawer(
    isSettings: Boolean,
    onClose: () -> Unit,
    onMatches: () -> Unit,
    onSettings: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxHeight()) {
        val minimumHeight = maxHeight
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = minimumHeight).padding(HubSpacing.Large),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(HubSpacing.Medium)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close_menu))
                    }
                }
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(HubSpacing.Medium).size(40.dp))
                }
                HubBrand()
                Text(stringResource(R.string.hub_tagline), color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(HubSpacing.Small))
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.matches_section)) }, selected = !isSettings,
                    onClick = onMatches, icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    shape = MaterialTheme.shapes.large,
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.tertiary,
                        selectedIconColor = MaterialTheme.colorScheme.tertiary
                    )
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.settings_title)) }, selected = isSettings,
                    onClick = onSettings, icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    shape = MaterialTheme.shapes.large,
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.tertiary,
                        selectedIconColor = MaterialTheme.colorScheme.tertiary
                    )
                )
                Spacer(Modifier.height(HubSpacing.Large))
            }
            VersionFooter()
        }
    }
}
