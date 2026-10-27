package com.nikodindon.retrocade.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikodindon.retrocade.core.billing.BillingRepository
import com.nikodindon.retrocade.core.billing.PremiumState
import com.nikodindon.retrocade.core.common.ControlScheme
import com.nikodindon.retrocade.core.common.GestureSensitivity
import com.nikodindon.retrocade.core.storage.AppSettings
import com.nikodindon.retrocade.core.storage.ScoreRepository
import com.nikodindon.retrocade.core.storage.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val scoreRepository: ScoreRepository,
    private val billingRepository: BillingRepository,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    val premium: StateFlow<PremiumState> = billingRepository.premiumState

    var restoreMessage by mutableStateOf<String?>(null)
        private set

    var scoresMessage by mutableStateOf<String?>(null)
        private set

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setMusicEnabled(enabled) }
    }

    fun setSfxEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSfxEnabled(enabled) }
    }

    fun setMusicVolume(volume: Int) {
        viewModelScope.launch { settingsRepository.setMusicVolume(volume) }
    }

    fun setSfxVolume(volume: Int) {
        viewModelScope.launch { settingsRepository.setSfxVolume(volume) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHapticsEnabled(enabled) }
    }

    fun setShowHoldButton(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setShowHoldButton(enabled) }
    }

    fun setControlScheme(scheme: ControlScheme) {
        viewModelScope.launch { settingsRepository.setControlScheme(scheme) }
    }

    fun setGestureSensitivity(sensitivity: GestureSensitivity) {
        viewModelScope.launch { settingsRepository.setGestureSensitivity(sensitivity) }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            billingRepository.refreshPurchases()
            restoreMessage = "Achats vérifiés auprès de Google Play."
        }
    }

    fun resetScores() {
        viewModelScope.launch {
            scoreRepository.reset()
            scoresMessage = "Scores locaux effacés."
        }
    }
}
