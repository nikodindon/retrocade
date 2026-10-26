package com.nikodindon.retrocade.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikodindon.retrocade.core.common.ControlScheme
import com.nikodindon.retrocade.core.common.sensitivityToSwipeThresholdDp
import com.nikodindon.retrocade.core.ui.RetroColors
import com.nikodindon.retrocade.games.api.GameSettings
import com.nikodindon.retrocade.games.tetris.TetrisSession
import com.nikodindon.retrocade.games.tetris.ui.TetrisScreen
import com.nikodindon.retrocade.games.tetris.ui.TetrisScoreResult
import com.nikodindon.retrocade.navigation.findActivity
import kotlin.random.Random

@Composable
fun TetrisRoute(
    onExit: () -> Unit,
    onOpenPaywall: () -> Unit,
) {
    val viewModel: TetrisRouteViewModel = hiltViewModel()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val bestScore by viewModel.bestScore.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var seed by rememberSaveable { mutableLongStateOf(Random.nextLong()) }
    var started by remember(seed) { mutableStateOf(false) }

    val currentSettings = settings
    if (currentSettings == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RetroColors.Background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = RetroColors.NeonGreen)
        }
        return
    }

    // The session only exists once the launch ad (if any) is out of the way. The
    // first game of the process never shows an ad and starts immediately.
    LaunchedEffect(seed) {
        viewModel.startGame(activity) { started = true }
    }

    if (!started) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RetroColors.Background)
                .systemBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TETRIS",
                    style = MaterialTheme.typography.displayLarge,
                    color = RetroColors.NeonGreen,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "PRÉPARATION DE LA PARTIE…",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroColors.TextMuted,
                )
            }
        }
        return
    }

    val gameSettings = remember(seed, currentSettings.controls, currentSettings.swipeSensitivity, currentSettings.hapticsEnabled) {
        GameSettings(
            controlScheme = currentSettings.controls,
            swipeThresholdDp = sensitivityToSwipeThresholdDp(currentSettings.swipeSensitivity),
            hapticsEnabled = currentSettings.hapticsEnabled,
        )
    }
    val session = remember(seed, gameSettings) {
        viewModel.game.createSession(seed, gameSettings) as TetrisSession
    }

    TetrisScreen(
        session = session,
        audio = viewModel.audio,
        bestScore = bestScore,
        sfxEnabled = currentSettings.sfxEnabled,
        musicEnabled = currentSettings.musicEnabled,
        showHoldButton = currentSettings.showHoldButton,
        onExit = onExit,
        onReplay = { seed = Random.nextLong() },
        onOpenPaywall = onOpenPaywall,
        onRecordScore = { score, lines, level ->
            val result = viewModel.recordScore(score, lines, level)
            TetrisScoreResult(
                isNewBest = result.isNewBest,
                best = result.best,
                rank = result.rank,
            )
        },
    )
}
