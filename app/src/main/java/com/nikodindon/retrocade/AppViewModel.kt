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
    settingsRepository: SettingsRepository,
    private val audio: AudioController,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    init {
        viewModelScope.launch {
            settings.collect {
                audio.setSfxEnabled(it.sfxEnabled)
                audio.setMusicEnabled(it.musicEnabled)
            }
        }
    }
}
