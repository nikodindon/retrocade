package com.nikodindon.retrocade.navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nikodindon.retrocade.AppViewModel
import com.nikodindon.retrocade.core.ui.RetroColors
import com.nikodindon.retrocade.game.GameRoute
import com.nikodindon.retrocade.hub.HubScreen
import com.nikodindon.retrocade.onboarding.OnboardingScreen
import com.nikodindon.retrocade.paywall.PaywallScreen
import com.nikodindon.retrocade.scores.ScoresScreen
import com.nikodindon.retrocade.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val ONBOARDING = "onboarding"
    const val GAME = "game/{gameId}"
    const val GAME_ID_ARG = "gameId"
    const val SCORES = "scores"
    const val SETTINGS = "settings"
    const val PAYWALL = "paywall"

    fun game(gameId: String): String = "game/${Uri.encode(gameId)}"
}

@Composable
fun RetrocadeNavApp() {
    val appViewModel: AppViewModel = hiltViewModel()
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
    val currentSettings = settings

    if (currentSettings == null) {
        AppLoadingScreen()
        return
    }

    val navController = rememberNavController()
    // Chosen once: later settings emissions must not recreate the graph and reset
    // the back stack while the onboarding write is still in flight.
    val startDestination = remember {
        if (currentSettings.onboardingDone) Routes.HOME else Routes.ONBOARDING
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onDone = {
                    appViewModel.completeOnboarding()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Routes.HOME) {
            HubScreen(
                onGame = { gameId -> navController.navigate(Routes.game(gameId)) },
                onScores = { navController.navigate(Routes.SCORES) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onStore = { navController.navigate(Routes.PAYWALL) },
            )
        }
        composable(
            route = Routes.GAME,
            arguments = listOf(navArgument(Routes.GAME_ID_ARG) { type = NavType.StringType }),
        ) {
            GameRoute(
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

@Composable
private fun AppLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RetroColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "RÉTROCADE",
                style = MaterialTheme.typography.displayLarge,
                color = RetroColors.NeonGreen,
            )
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator(color = RetroColors.NeonGreen)
        }
    }
}
