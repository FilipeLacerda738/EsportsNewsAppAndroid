package com.example.esportsnews

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.example.esportsnews.ui.theme.TextPrimary

@Composable
fun LanguageSelector() {
    var menuAberto by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { menuAberto = true }) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = stringResource(R.string.change_language),
                tint = TextPrimary
            )
        }
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