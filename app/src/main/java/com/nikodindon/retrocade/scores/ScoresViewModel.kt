package com.nikodindon.retrocade.scores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikodindon.retrocade.core.storage.GameScores
import com.nikodindon.retrocade.core.storage.ScoreRepository
import com.nikodindon.retrocade.games.api.GameRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class GameScoreboard(
    val gameId: String,
    val name: String,
    val accentColor: Long,
    val scores: GameScores,
)

@HiltViewModel
class ScoresViewModel @Inject constructor(
    registry: GameRegistry,
    scoreRepository: ScoreRepository,
) : ViewModel() {
    private val gamesById = registry.all.associateBy { it.meta.id }
    private val gameOrder = registry.all.map { it.meta.id }

    val boards: StateFlow<List<GameScoreboard>> = scoreRepository.scores
        .map { book ->
            gameOrder.map { id ->
                val meta = gamesById.getValue(id).meta
                GameScoreboard(
                    gameId = id,
                    name = meta.name,
                    accentColor = meta.accentColor,
                    scores = book.forGame(id),
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
