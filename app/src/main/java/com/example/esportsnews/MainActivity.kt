package com.example.esportsnews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.esportsnews.ui.theme.DarkBackground
import com.example.esportsnews.ui.theme.EsportsNewsTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MatchesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EsportsNewsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    MatchesScreen(viewModel)
                }
            }
        }
    }
}