package com.example.esportsnews.feature.updates

import com.example.esportsnews.R
import com.example.esportsnews.core.links.openExternalWebLink

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.esportsnews.data.remote.dto.AppVersion
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

@Composable
internal fun AppUpdatesRoute(viewModel: AppUpdatesViewModel) {
    val updateData by viewModel.updateAvailable.collectAsStateWithLifecycle()
    AppUpdateDialog(updateData, viewModel::dismissUpdateDialog)
}

@Composable
internal fun AppUpdateDialog(updateData: AppVersion?, onDismiss: () -> Unit) {
    val context = LocalContext.current
    updateData?.let { updateInfo ->
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.update_available_title)) },
            text = {
                Text(stringResource(R.string.update_available_desc,
                    updateInfo.version.orEmpty(), updateInfo.releaseNotes.orEmpty()),
                    modifier = Modifier.verticalScroll(rememberScrollState()))
            },
            confirmButton = {
                TextButton(onClick = {
                    if (openExternalWebLink(context, updateInfo.downloadUrl)) onDismiss()
                }) { Text(stringResource(R.string.update_open_release)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.update_download_no)) }
            }
        )
    }
}
