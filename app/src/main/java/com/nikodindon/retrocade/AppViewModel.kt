package com.nikodindon.retrocade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikodindon.retrocade.core.audio.AudioController
import com.nikodindon.retrocade.core.storage.AppSettings
import com.nikodindon.retrocade.core.storage.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val audio: AudioController,
) : ViewModel() {
    /**
     * Null until DataStore has produced its first value. Navigation waits for this
     * so an existing user never sees the onboarding for one frame.
     */
    val settings: StateFlow<AppSettings?> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    init {
        viewModelScope.launch {
            settings.collect { current ->
                if (current == null) return@collect
                audio.setSfxEnabled(current.sfxEnabled)
                audio.setMusicEnabled(current.musicEnabled)
                audio.setSfxVolume(current.sfxVolume / 100f)
                audio.setMusicVolume(current.musicVolume / 100f)
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingDone(true)
        }
    }
}
