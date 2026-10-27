package com.nikodindon.retrocade.game

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikodindon.retrocade.core.ads.AdPolicy
import com.nikodindon.retrocade.core.ads.GameLaunchTracker
import com.nikodindon.retrocade.core.ads.InterstitialAdManager
import com.nikodindon.retrocade.core.audio.AudioController
import com.nikodindon.retrocade.core.storage.AppSettings
import com.nikodindon.retrocade.core.storage.RecordResult
import com.nikodindon.retrocade.core.storage.ScoreRepository
import com.nikodindon.retrocade.core.storage.SettingsRepository
import com.nikodindon.retrocade.games.api.Game
import com.nikodindon.retrocade.games.api.GameRegistry
import com.nikodindon.retrocade.games.api.GameScoreResult
import com.nikodindon.retrocade.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Hosts any game. The route only knows the stable id from the navigation graph;
 * the registry resolves the implementation. Adding a game never changes this class.
 */
@HiltViewModel
class GameRouteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    registry: GameRegistry,
    val audio: AudioController,
    private val scoreRepository: ScoreRepository,
    private val adPolicy: AdPolicy,
    private val interstitialAdManager: InterstitialAdManager,
    private val gameLaunchTracker: GameLaunchTracker,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val gameId: String = savedStateHandle.get<String>(Routes.GAME_ID_ARG).orEmpty()
    val game: Game? = registry.find(gameId)

    val settings: StateFlow<AppSettings?> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    val bestScore: StateFlow<Int> = scoreRepository.scores
        .map { book -> game?.let { book.forGame(it.meta.id).best } ?: 0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0,
        )

    suspend fun recordScore(score: Int, lines: Int, level: Int): GameScoreResult {
        val current = game ?: return GameScoreResult(isNewBest = false, best = 0, rank = null)
        val result: RecordResult = scoreRepository.record(
            gameId = current.meta.id,
            score = score,
            lines = lines,
            level = level,
        )
        return GameScoreResult(
            isNewBest = result.isNewBest,
            best = result.best,
            rank = result.rank,
        )
    }

    /**
     * Called exactly when a game session is about to start. The session itself is
     * only created after [onReady], so no gameplay runs behind a fullscreen ad.
     * Non-blocking: when no ad is ready, [onReady] runs immediately.
     */
    fun startGame(activity: Activity?, onReady: () -> Unit) {
        var ready = false
        fun readyOnce() {
            if (ready) return
            ready = true
            onReady()
        }

        val isFirstGame = gameLaunchTracker.next() == 1
        val canShow = activity != null &&
            adPolicy.shouldShowInterstitial(isFirstGame) &&
            interstitialAdManager.show(activity, ::readyOnce)
        if (!canShow) readyOnce()
    }
}
