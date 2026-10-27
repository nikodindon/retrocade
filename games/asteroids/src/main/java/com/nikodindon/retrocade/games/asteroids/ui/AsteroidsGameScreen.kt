package com.nikodindon.retrocade.games.asteroids.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.nikodindon.retrocade.games.api.GameUiContext
import com.nikodindon.retrocade.games.asteroids.AsteroidsSession

/**
 * Adapter between the generic [GameUiContext] contract and the Asteroids screen.
 * The shared route never imports this file.
 */
@Composable
fun AsteroidsGameScreen(context: GameUiContext) {
    val session = context.session as? AsteroidsSession
        ?: error("AsteroidsGameScreen requires an AsteroidsSession")
    val localContext = LocalContext.current
    val showHelpOnStart = remember(session, localContext) {
        !AsteroidsHelpPrefs.hasSeen(localContext)
    }

    AsteroidsScreen(
        session = session,
        audio = context.audio,
        bestScore = context.bestScore,
        sfxEnabled = context.settings.sfxEnabled,
        musicEnabled = context.settings.musicEnabled,
        showHelpOnStart = showHelpOnStart,
        onHelpDismissed = { AsteroidsHelpPrefs.markSeen(localContext) },
        onExit = context.onExit,
        onReplay = context.onReplay,
        onOpenPaywall = context.onOpenPaywall,
        onRecordScore = context.onRecordScore,
    )
}
