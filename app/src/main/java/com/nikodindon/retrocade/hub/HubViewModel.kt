package com.nikodindon.retrocade.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikodindon.retrocade.core.billing.BillingRepository
import com.nikodindon.retrocade.core.billing.PremiumState
import com.nikodindon.retrocade.core.storage.ScoreBook
import com.nikodindon.retrocade.core.storage.ScoreRepository
import com.nikodindon.retrocade.games.api.Game
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HubViewModel @Inject constructor(
    games: Set<@JvmSuppressWildcards Game>,
    scoreRepository: ScoreRepository,
    billingRepository: BillingRepository,
) : ViewModel() {
    val games: List<Game> = games.sortedBy { it.meta.name }

    val scores: StateFlow<ScoreBook> = scoreRepository.scores.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScoreBook(),
    )

    val premium: StateFlow<Boolean> = billingRepository.premiumState
        .map { it is PremiumState.Premium }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )
}
