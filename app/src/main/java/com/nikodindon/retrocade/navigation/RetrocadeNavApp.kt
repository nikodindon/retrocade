package com.nikodindon.retrocade.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nikodindon.retrocade.AppViewModel
import com.nikodindon.retrocade.game.TetrisRoute
import com.nikodindon.retrocade.hub.HubScreen
import com.nikodindon.retrocade.paywall.PaywallScreen
import com.nikodindon.retrocade.scores.ScoresScreen
import com.nikodindon.retrocade.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val TETRIS = "tetris"
    const val SCORES = "scores"
    const val SETTINGS = "settings"
    const val PAYWALL = "paywall"
}

@Composable
fun RetrocadeNavApp() {
    // Keep the singleton audio engine in sync with the persisted settings.
    hiltViewModel<AppViewModel>()
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HubScreen(
                onPlay = { navController.navigate(Routes.TETRIS) },
                onScores = { navController.navigate(Routes.SCORES) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onStore = { navController.navigate(Routes.PAYWALL) },
            )
        }
        composable(Routes.TETRIS) {
            TetrisRoute(
                onExit = { navController.popBackStack() },
                onOpenPaywall = { navController.navigate(Routes.PAYWALL) },
            )
        }
        composable(Routes.SCORES) {
            ScoresScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onStore = { navController.navigate(Routes.PAYWALL) },
            )
        }
        composable(Routes.PAYWALL) {
            PaywallScreen(onBack = { navController.popBackStack() })
        }
    }
}
