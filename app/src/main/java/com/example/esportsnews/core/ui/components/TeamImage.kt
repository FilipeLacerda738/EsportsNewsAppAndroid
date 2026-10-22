package com.example.esportsnews.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/** Try the standard logo when the dark variant fails; keep initials for missing images. */
@Composable
fun TeamImage(name: String?, acronym: String?, darkUrl: String?, imageUrl: String?, modifier: Modifier = Modifier) {
    val urls = remember(darkUrl, imageUrl) { listOfNotNull(darkUrl, imageUrl).filter { it.isNotBlank() }.distinct() }
    var index by remember(urls) { mutableStateOf(0) }
    var loaded by remember(urls, index) { mutableStateOf(false) }
    val initials = acronym?.takeIf { it.isNotBlank() }?.take(3)
        ?: name.orEmpty().split(Regex("\\s+")).filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1) }.ifBlank { "?" }
    Box(modifier, contentAlignment = Alignment.Center) {
        if (!loaded) Text(initials.uppercase(java.util.Locale.ROOT), style = MaterialTheme.typography.titleMedium)
        urls.getOrNull(index)?.let { url ->
            AsyncImage(model = url, contentDescription = null, modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Fit,
                onSuccess = { loaded = true }, onError = { index++ })
        }
    }
}
