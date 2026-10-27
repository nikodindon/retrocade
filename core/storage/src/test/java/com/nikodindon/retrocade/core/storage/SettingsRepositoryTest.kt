package com.nikodindon.retrocade.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SettingsRepositoryTest {
    @Test
    fun `new install uses documented defaults`() = runTest {
        val settings = SettingsRepository(FakePreferencesDataStore()).current()

        assertTrue(settings.sfxEnabled)
        assertTrue(settings.musicEnabled)
        assertEquals(75, settings.sfxVolume)
        assertEquals(60, settings.musicVolume)
        assertTrue(settings.hapticsEnabled)
        assertEquals(com.nikodindon.retrocade.core.common.ControlScheme.TAP, settings.controls)
        assertEquals(com.nikodindon.retrocade.core.common.GestureSensitivity.NORMAL, settings.gestures)
        assertTrue(settings.showHoldButton)
        assertFalse(settings.onboardingDone)
    }

    @Test
    fun `settings survive a repository restart`() = runTest {
        val store = FakePreferencesDataStore()
        val repository = SettingsRepository(store)

        repository.setMusicEnabled(false)
        repository.setSfxVolume(42)
        repository.setMusicVolume(17)
        repository.setHapticsEnabled(false)
        repository.setControlScheme(com.nikodindon.retrocade.core.common.ControlScheme.BUTTONS)
        repository.setGestureSensitivity(com.nikodindon.retrocade.core.common.GestureSensitivity.HIGH)
        repository.setShowHoldButton(false)

        val reopened = SettingsRepository(store).current()
        assertFalse(reopened.musicEnabled)
        assertEquals(42, reopened.sfxVolume)
        assertEquals(17, reopened.musicVolume)
        assertFalse(reopened.hapticsEnabled)
        assertEquals(com.nikodindon.retrocade.core.common.ControlScheme.BUTTONS, reopened.controls)
        assertEquals(com.nikodindon.retrocade.core.common.GestureSensitivity.HIGH, reopened.gestures)
        assertFalse(reopened.showHoldButton)
    }

    @Test
    fun `volume values outside 0 to 100 are clamped`() = runTest {
        val repository = SettingsRepository(FakePreferencesDataStore())

        repository.setSfxVolume(-25)
        repository.setMusicVolume(250)

        assertEquals(0, repository.current().sfxVolume)
        assertEquals(100, repository.current().musicVolume)
    }

    @Test
    fun `v0_1 settings still load and migrate the swipe slider`() = runTest {
        val store = FakePreferencesDataStore().apply {
            writeRaw(
                "settings_json",
                """
                {
                  "sfxEnabled": false,
                  "musicEnabled": true,
                  "hapticsEnabled": false,
                  "controlScheme": "BUTTONS",
                  "swipeSensitivity": 0.9,
                  "showHoldButton": false
                }
                """.trimIndent(),
            )
        }

        val migrated = SettingsRepository(store).current()

        assertFalse(migrated.sfxEnabled)
        assertFalse(migrated.hapticsEnabled)
        assertEquals(com.nikodindon.retrocade.core.common.ControlScheme.BUTTONS, migrated.controls)
        assertEquals(com.nikodindon.retrocade.core.common.GestureSensitivity.HIGH, migrated.gestures)
        assertEquals(75, migrated.sfxVolume)
        assertFalse(migrated.onboardingDone)
    }

    @Test
    fun `onboarding flag is persisted for subsequent launches`() = runTest {
        val store = FakePreferencesDataStore()
        val firstLaunch = SettingsRepository(store)

        assertFalse(firstLaunch.current().onboardingDone)
        firstLaunch.setOnboardingDone(true)
        assertTrue(SettingsRepository(store).current().onboardingDone)
    }
}

internal class FakePreferencesDataStore(
    initial: Preferences = emptyPreferences(),
) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)

    override val data: Flow<Preferences> = state

    override suspend fun updateData(
        transform: suspend (t: Preferences) -> Preferences,
    ): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }

    suspend fun writeRaw(key: String, value: String) {
        updateData { preferences ->
            preferences.toMutablePreferences().apply {
                this[stringPreferencesKey(key)] = value
            }
        }
    }
}
