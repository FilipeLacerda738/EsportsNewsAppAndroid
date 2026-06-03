package com.example.esportsnews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "matches_list") {

                        composable("matches_list") {
                            MatchesScreen(
                                viewModel = viewModel,
                                onMatchClick = { matchId ->

                                    navController.navigate("match_detail/$matchId")
                                }
                            )
                        }


                        composable(
                            route = "match_detail/{matchId}",
                            arguments = listOf(navArgument("matchId") { type = NavType.IntType })
                        ) { backStackEntry ->

                            val matchId = backStackEntry.arguments?.getInt("matchId") ?: return@composable

                            MatchDetailScreen(
                                matchId = matchId,
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}