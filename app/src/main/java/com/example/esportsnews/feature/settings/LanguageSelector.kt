package com.example.esportsnews.feature.settings

import com.example.esportsnews.R

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat

@Composable
fun LanguageSelector() {
    var menuAberto by remember { mutableStateOf(false) }
    val locale = LocalConfiguration.current.locales[0]

    Box {
        SettingsAction(
            title = stringResource(R.string.settings_language),
            subtitle = locale.getDisplayName(locale).replaceFirstChar { it.titlecase(locale) },
            icon = Icons.Default.Language,
            onClick = { menuAberto = true },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = menuAberto,
            onDismissRequest = { menuAberto = false }
        ) {
            DropdownMenuItem(
                text = { Text("Português") },
                onClick = {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("pt-BR"))
                    menuAberto = false
                }
            )
            DropdownMenuItem(
                text = { Text("English") },
                onClick = {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                    menuAberto = false
                }
            )
            DropdownMenuItem(
                text = { Text("Español") },
                onClick = {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))
                    menuAberto = false
                }
            )
        }
    }
}
