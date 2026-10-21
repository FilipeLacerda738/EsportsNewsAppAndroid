package com.example.esportsnews

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.esportsnews.ui.theme.HubSpacing

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MatchesViewModel, onOpenMenu: () -> Unit) {
    val updateState by viewModel.updateCheckState.collectAsState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.open_menu))
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(HubSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(HubSpacing.Large)) {
            SettingsOptions(updateState, onCheckUpdates = viewModel::checkForUpdates)
            SettingsSection(stringResource(R.string.settings_about), Icons.Default.Info) {
                HubBrand()
                Text(stringResource(R.string.hub_tagline), color = MaterialTheme.colorScheme.onSurfaceVariant)
                VersionFooter()
            }
        }
    }
}

@Composable
private fun SettingsOptions(updateState: UpdateCheckState, onCheckUpdates: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(HubSpacing.Medium)) {
        SettingsSection(stringResource(R.string.settings_language), Icons.Default.Language) {
            LanguageSelector()
        }
        SettingsSection(stringResource(R.string.settings_updates), Icons.Default.Sync) {
            SettingsAction(
                title = stringResource(R.string.update_check),
                subtitle = updateStatus(updateState)?.let { stringResource(it) },
                icon = Icons.Default.Sync,
                enabled = updateState != UpdateCheckState.Checking,
                onClick = onCheckUpdates,
                checking = updateState == UpdateCheckState.Checking
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(HubSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(HubSpacing.Medium)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            }
            content()
        }
    }
}

@Composable
internal fun SettingsAction(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checking: Boolean = false
) {
    Surface(onClick = onClick, enabled = enabled,
        modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.padding(HubSpacing.Large).heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HubSpacing.Large)) {
            if (checking) CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp)
            else Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HubSpacing.ExtraSmall)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                subtitle?.let {
                    Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun VersionFooter() {
    Column(verticalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleSmall)
        Text(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

private fun updateStatus(state: UpdateCheckState): Int? = when (state) {
    UpdateCheckState.Idle -> null
    UpdateCheckState.Checking -> R.string.update_checking
    UpdateCheckState.UpToDate -> R.string.update_up_to_date
    UpdateCheckState.Available -> R.string.update_available_title
    UpdateCheckState.Failed -> R.string.update_check_failed
}

@Composable
internal fun AppUpdateDialog(viewModel: MatchesViewModel) {
    val updateData by viewModel.updateAvailable.collectAsState()
    val context = LocalContext.current
    updateData?.let { updateInfo ->
        AlertDialog(
            onDismissRequest = viewModel::dismissUpdateDialog,
            title = { Text(stringResource(R.string.update_available_title)) },
            text = {
                Text(stringResource(R.string.update_available_desc,
                    updateInfo.version.orEmpty(), updateInfo.releaseNotes.orEmpty()),
                    modifier = Modifier.verticalScroll(rememberScrollState()))
            },
            confirmButton = {
                TextButton(onClick = {
                    if (openExternalWebLink(context, updateInfo.downloadUrl)) viewModel.dismissUpdateDialog()
                }) { Text(stringResource(R.string.update_open_release)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissUpdateDialog) { Text(stringResource(R.string.update_download_no)) }
            }
        )
    }
}
