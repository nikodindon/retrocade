package com.nikodindon.retrocade.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikodindon.retrocade.core.billing.BillingRepository
import com.nikodindon.retrocade.core.billing.PremiumState
import com.nikodindon.retrocade.core.storage.ScoreRepository
import com.nikodindon.retrocade.games.api.Game
import com.nikodindon.retrocade.games.api.GameRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HubGame(
    val game: Game,
    /** Null when the game has never been played on this device. */
    val best: Int?,
)

data class HubUiState(
    val games: List<HubGame>,
)

@HiltViewModel
class HubViewModel @Inject constructor(
    registry: GameRegistry,
    scoreRepository: ScoreRepository,
    billingRepository: BillingRepository,
) : ViewModel() {
    /** Null while DataStore is loading, so the hub can show a loading state. */
    val uiState: StateFlow<HubUiState?> = scoreRepository.scores
        .map { scores ->
            HubUiState(
                games = registry.all.map { game ->
                    val gameScores = scores.forGame(game.meta.id)
                    HubGame(
                        game = game,
                        best = if (gameScores.hasPlayed) gameScores.best else null,
                    )
                },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val premium: StateFlow<Boolean> = billingRepository.premiumState
        .map { it is PremiumState.Premium }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )
}
