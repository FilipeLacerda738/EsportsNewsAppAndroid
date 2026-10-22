package com.example.esportsnews.app

import com.example.esportsnews.app.navigation.AppNavHost
import com.example.esportsnews.feature.matchdetail.MatchDetailViewModel
import com.example.esportsnews.feature.matches.MatchesViewModel
import com.example.esportsnews.feature.updates.AppUpdatesViewModel

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.esportsnews.core.ui.theme.DarkBackground
import com.example.esportsnews.core.ui.theme.EsportsNewsTheme

@Composable
fun EsportsApp(matches: MatchesViewModel, details: MatchDetailViewModel, updates: AppUpdatesViewModel) {
    EsportsNewsTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
            AppNavHost(matches, details, updates)
        }
    }
}
