package com.example.esportsnews.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import com.example.esportsnews.core.ui.theme.HubSpacing
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.example.esportsnews.BuildConfig
import com.example.esportsnews.R

@Composable
internal fun VersionFooter() {
    Column(verticalArrangement = Arrangement.spacedBy(HubSpacing.Small)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleSmall)
        Text(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}
