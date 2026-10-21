/* Esports Hub - Plataforma de acompanhamento de e-sports
Copyright (C) 2026 George Filipe Rodrigues de Lacerda

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>. */

package com.example.esportsnews

import android.os.Bundle
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.esportsnews.ui.theme.DarkBackground
import com.example.esportsnews.ui.theme.EsportsNewsTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MatchesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            EsportsNewsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val entry by navController.currentBackStackEntryAsState()
                    val route = entry?.destination?.route
                    val openMenu: () -> Unit = { scope.launch { drawerState.open() } }
                    val closeMenu: () -> Unit = { scope.launch { drawerState.close() } }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = route == "matches_list" || route == "settings",
                        drawerContent = {
                            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.background) {
                                HubDrawer(
                                    isSettings = route == "settings",
                                    onClose = closeMenu,
                                    onMatches = {
                                        scope.launch {
                                            drawerState.close()
                                            if (navController.currentDestination?.route != "matches_list") {
                                                navController.popBackStack("matches_list", inclusive = false)
                                            }
                                        }
                                    },
                                    onSettings = {
                                        scope.launch {
                                            drawerState.close()
                                            if (navController.currentDestination?.route != "settings") {
                                                navController.navigate("settings") {
                                                    popUpTo("matches_list")
                                                    launchSingleTop = true
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    ) {
                        NavHost(navController = navController, startDestination = "matches_list") {
                            composable("matches_list") { backStackEntry ->
                                DisposableEffect(backStackEntry) {
                                    val lifecycle = backStackEntry.lifecycle
                                    val observer = LifecycleEventObserver { _, _ ->
                                        viewModel.setListActive(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
                                    }
                                    lifecycle.addObserver(observer)
                                    viewModel.setListActive(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
                                    onDispose {
                                        lifecycle.removeObserver(observer)
                                        viewModel.setListActive(false)
                                    }
                                }
                                MatchesScreen(
                                    viewModel = viewModel,
                                    onMatchClick = { matchId ->
                                        navController.navigate("match_detail/$matchId") { launchSingleTop = true }
                                    },
                                    onOpenMenu = openMenu
                                )
                            }
                            composable("settings") {
                                SettingsScreen(viewModel = viewModel, onOpenMenu = openMenu)
                            }
                            composable(
                                route = "match_detail/{matchId}",
                                arguments = listOf(navArgument("matchId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val matchId = backStackEntry.arguments?.getInt("matchId") ?: return@composable
                                MatchDetailScreen(
                                    matchId = matchId,
                                    viewModel = viewModel,
                                    lifecycle = backStackEntry.lifecycle,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                    AppUpdateDialog(viewModel)
                }
            }
        }
    }
}
