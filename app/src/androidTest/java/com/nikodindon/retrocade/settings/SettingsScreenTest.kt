package com.nikodindon.retrocade.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nikodindon.retrocade.core.billing.PremiumState
import com.nikodindon.retrocade.core.storage.AppSettings
import com.nikodindon.retrocade.core.ui.RetrocadeTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun togglingMusicUpdatesThePersistedState() {
        var settings by mutableStateOf(AppSettings())
        compose.setContent {
            RetrocadeTheme {
                SettingsContent(
                    settings = settings,
                    premium = PremiumState.Free,
                    restoreMessage = null,
                    scoresMessage = null,
                    onBack = {},
                    onStore = {},
                    actions = SettingsActions(
                        setMusicEnabled = { enabled ->
                            settings = settings.copy(musicEnabled = enabled)
                        },
                    ),
                )
            }
        }

        compose.onNodeWithTag("settings_music_switch").assertIsOn()
        compose.onNodeWithTag("settings_music_switch").performClick()
        compose.onNodeWithTag("settings_music_switch").assertIsOff()
    }

    @Test
    fun scoreResetRequiresConfirmation() {
        var resetCalled = false
        compose.setContent {
            RetrocadeTheme {
                SettingsContent(
                    settings = AppSettings(),
                    premium = PremiumState.Free,
                    restoreMessage = null,
                    scoresMessage = null,
                    onBack = {},
                    onStore = {},
                    actions = SettingsActions(
                        resetScores = { resetCalled = true },
                    ),
                )
            }
        }

        compose.onNodeWithTag("settings_reset_scores")
            .performScrollTo()
            .performClick()
        compose.onNodeWithTag("settings_reset_dialog").assertIsDisplayed()
        compose.onNodeWithTag("settings_reset_confirm").performClick()

        assertTrue(resetCalled)
    }
}
