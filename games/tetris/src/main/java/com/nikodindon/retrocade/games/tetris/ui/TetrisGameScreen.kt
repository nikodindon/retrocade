package com.nikodindon.retrocade.games.tetris.ui

import androidx.compose.runtime.Composable
import com.nikodindon.retrocade.games.api.GameUiContext
import com.nikodindon.retrocade.games.tetris.TetrisSession

/**
 * Adapter between the generic [GameUiContext] contract and the Tetris screen.
 * The shared route never imports this file; it only calls [GameUiContext] content
 * through the registry.
 */
@Composable
fun TetrisGameScreen(context: GameUiContext) {
    val session = context.session as? TetrisSession
        ?: error("TetrisGameScreen requires a TetrisSession")

    TetrisScreen(
        session = session,
        audio = context.audio,
        bestScore = context.bestScore,
        sfxEnabled = context.settings.sfxEnabled,
        musicEnabled = context.settings.musicEnabled,
        showHoldButton = context.settings.showHoldButton,
        onExit = context.onExit,
        onReplay = context.onReplay,
        onOpenPaywall = context.onOpenPaywall,
        onRecordScore = context.onRecordScore,
    )
}
