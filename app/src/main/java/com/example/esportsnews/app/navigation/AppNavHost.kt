package com.example.esportsnews.app.navigation

import com.example.esportsnews.feature.matchdetail.MatchDetailRoute
import com.example.esportsnews.feature.matchdetail.MatchDetailViewModel
import com.example.esportsnews.feature.matches.MatchesRoute
import com.example.esportsnews.feature.matches.MatchesViewModel
import com.example.esportsnews.feature.settings.SettingsRoute
import com.example.esportsnews.feature.updates.AppUpdatesRoute
import com.example.esportsnews.feature.updates.AppUpdatesViewModel

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(viewModel: MatchesViewModel, detailViewModel: MatchDetailViewModel,
    updatesViewModel: AppUpdatesViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val openMenu: () -> Unit = { scope.launch { drawerState.open() } }
    val closeMenu: () -> Unit = { scope.launch { drawerState.close() } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = route == AppRoutes.MATCHES || route == AppRoutes.SETTINGS,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.background) {
                HubDrawer(
                    isSettings = route == AppRoutes.SETTINGS,
                    onClose = closeMenu,
                    onMatches = {
                        scope.launch {
                            drawerState.close()
                            if (navController.currentDestination?.route != AppRoutes.MATCHES) {
                                navController.popBackStack(AppRoutes.MATCHES, inclusive = false)
                            }
                        }
                    },
                    onSettings = {
                        scope.launch {
                            drawerState.close()
                            if (navController.currentDestination?.route != AppRoutes.SETTINGS) {
                                navController.navigate(AppRoutes.SETTINGS) {
                                    popUpTo(AppRoutes.MATCHES)
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                )
            }
        }
    ) {
        NavHost(navController = navController, startDestination = AppRoutes.MATCHES) {
            composable(AppRoutes.MATCHES) { backStackEntry ->
                MatchesRoute(
                    viewModel = viewModel,
                    lifecycle = backStackEntry.lifecycle,
                    onMatchClick = { matchId ->
                        navController.navigate(AppRoutes.detail(matchId)) { launchSingleTop = true }
                    },
                    onOpenMenu = openMenu
                )
            }
            composable(AppRoutes.SETTINGS) {
                SettingsRoute(viewModel = updatesViewModel, onOpenMenu = openMenu)
            }
            composable(
                route = AppRoutes.DETAIL,
                arguments = listOf(navArgument("matchId") { type = NavType.IntType })
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getInt("matchId") ?: return@composable
                MatchDetailRoute(
                    matchId = matchId,
                    viewModel = detailViewModel,
                    lifecycle = backStackEntry.lifecycle,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
    AppUpdatesRoute(updatesViewModel)
}
