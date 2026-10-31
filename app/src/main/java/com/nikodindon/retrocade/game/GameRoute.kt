package com.nikodindon.retrocade.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikodindon.retrocade.core.storage.AppSettings
import com.nikodindon.retrocade.core.ui.NeonPanel
import com.nikodindon.retrocade.core.ui.RetroBackdrop
import com.nikodindon.retrocade.core.ui.RetroButton
import com.nikodindon.retrocade.core.ui.RetroColors
import com.nikodindon.retrocade.games.api.Game
import com.nikodindon.retrocade.games.api.GameSettings
import com.nikodindon.retrocade.games.api.GameUiContext
import com.nikodindon.retrocade.navigation.findActivity
import kotlin.random.Random

/**
 * Generic game route. It resolves the game from the registry, applies the shared
 * ad gate, settings and score recording, then delegates rendering to the game.
 */
@Composable
fun GameRoute(
    onExit: () -> Unit,
    onOpenPaywall: () -> Unit,
) {
    val viewModel: GameRouteViewModel = hiltViewModel()
    val game = viewModel.game

    if (game == null) {
        UnknownGameScreen(onExit = onExit)
        return
    }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val bestScore by viewModel.bestScore.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var seed by rememberSaveable(game.meta.id) { mutableLongStateOf(Random.nextLong()) }
    // Sentinel: no seed has completed the ad gate yet.
    var readySeed by rememberSaveable(game.meta.id) {
        mutableLongStateOf(Long.MIN_VALUE)
    }

    val currentSettings = settings
    if (currentSettings == null) {
        GameLoadingScreen(game = game, message = "CHARGEMENT…")
        return
    }

    // The session only exists once the launch ad (if any) is out of the way.
    // This also covers replay: a new seed must pass through the ad gate before
    // a new session is created, so gameplay never starts behind an interstitial.
    LaunchedEffect(game.meta.id, seed) {
        val requestedSeed = seed
        viewModel.startGame(activity) { readySeed = requestedSeed }
    }

    if (readySeed != seed) {
        GameLoadingScreen(game = game, message = "PRÉPARATION DE LA PARTIE…")
        return
    }

    val gameSettings = remember(currentSettings) { currentSettings.toGameSettings() }
    val session = remember(game.meta.id, seed, gameSettings) {
        game.createSession(seed, gameSettings)
    }
    val uiContext = GameUiContext(
        session = session,
        settings = gameSettings,
        bestScore = bestScore,
        audio = viewModel.audio,
        onExit = onExit,
        onReplay = { seed = Random.nextLong() },
        onOpenPaywall = onOpenPaywall,
        onRecordScore = viewModel::recordScore,
    )

    game.GameScreen(uiContext)
}

private fun AppSettings.toGameSettings(): GameSettings = GameSettings(
    controlScheme = controls,
    swipeThresholdDp = gestures.thresholdDp,
    hapticsEnabled = hapticsEnabled,
    sfxEnabled = sfxEnabled,
    musicEnabled = musicEnabled,
    showHoldButton = showHoldButton,
)

@Composable
private fun GameLoadingScreen(game: Game, message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RetroColors.Background)
            .systemBarsPadding()
            .testTag("game_loading"),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = game.meta.name.uppercase(),
                style = MaterialTheme.typography.displayLarge,
                color = RetroColors.NeonGreen,
            )
            Spacer(Modifier.height(10.dp))
            CircularProgressIndicator(color = RetroColors.NeonGreen)
            Spacer(Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.labelSmall,
                color = RetroColors.TextMuted,
            )
        }
    }
}

/** Clean fallback for a stale or malformed route; never crashes. */
@Composable
fun UnknownGameScreen(onExit: () -> Unit) {
    RetroBackdrop(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp)
                .testTag("game_unknown"),
            contentAlignment = Alignment.Center,
        ) {
            NeonPanel(accent = RetroColors.NeonMagenta) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "JEU INTROUVABLE",
                        style = MaterialTheme.typography.headlineMedium,
                        color = RetroColors.NeonMagenta,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Ce jeu n'est pas installé dans cette version de Rétrocade.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RetroColors.TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp),
                    )
                    RetroButton(
                        label = "Retour au hub",
                        onClick = onExit,
                        accent = RetroColors.NeonCyan,
                    )
                }
            }
        }
    }
}
