package com.nikodindon.retrocade.games.asteroids.ui

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nikodindon.retrocade.core.audio.AudioController
import com.nikodindon.retrocade.core.audio.MusicTrack
import com.nikodindon.retrocade.core.audio.Sfx
import com.nikodindon.retrocade.core.common.ControlScheme
import com.nikodindon.retrocade.core.ui.RetrocadeTheme
import com.nikodindon.retrocade.games.api.GameScoreResult
import com.nikodindon.retrocade.games.api.GameSettings
import com.nikodindon.retrocade.games.asteroids.AsteroidsSession
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AsteroidsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val settings = GameSettings(controlScheme = ControlScheme.TAP)
    private val audio = FakeAudio()

    @Test
    fun hudShowsScoreLivesWaveAndBest() {
        val session = AsteroidsSession(seed = 101L, settings = settings)
        setScreen(session)
        composeRule.mainClock.advanceTimeByFrame()

        composeRule.onNodeWithTag("asteroids_board").assertExists()
        composeRule.onNodeWithTag("asteroids_score").assertTextEquals("000000")
        composeRule.onNodeWithTag("asteroids_lives").assertExists()
        composeRule.onNodeWithTag("asteroids_wave").assertTextEquals("VAGUE 1")
    }

    @Test
    fun pauseAndResume() {
        val session = AsteroidsSession(seed = 102L, settings = settings)
        setScreen(session)
        composeRule.mainClock.advanceTimeByFrame()

        composeRule.onNodeWithTag("asteroids_pause").performClick()
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.onNodeWithTag("asteroids_pause_overlay").assertExists()

        composeRule.onNodeWithTag("asteroids_resume").performClick()
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.onNodeWithTag("asteroids_pause_overlay").assertDoesNotExist()
    }

    @Test
    fun firstLaunchHelpCanBeDismissed() {
        val session = AsteroidsSession(seed = 103L, settings = settings)
        var dismissed = false
        composeRule.setContent {
            RetrocadeTheme {
                AsteroidsScreen(
                    session = session,
                    audio = audio,
                    bestScore = 0,
                    sfxEnabled = false,
                    musicEnabled = false,
                    showHelpOnStart = true,
                    onHelpDismissed = { dismissed = true },
                    onExit = {},
                    onReplay = {},
                    onOpenPaywall = {},
                    onRecordScore = { _, _, _ -> GameScoreResult(false, 0, null) },
                )
            }
        }

        composeRule.onNodeWithTag("asteroids_help").assertExists()
        composeRule.onNodeWithTag("asteroids_help_dismiss").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("asteroids_help").assertDoesNotExist()
        assertTrue(dismissed)
    }

    private fun setScreen(session: AsteroidsSession) {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            RetrocadeTheme {
                AsteroidsScreen(
                    session = session,
                    audio = audio,
                    bestScore = 1_234,
                    sfxEnabled = false,
                    musicEnabled = false,
                    showHelpOnStart = false,
                    onHelpDismissed = {},
                    onExit = {},
                    onReplay = {},
                    onOpenPaywall = {},
                    onRecordScore = { _, _, _ -> GameScoreResult(false, 1_234, null) },
                )
            }
        }
    }

    private class FakeAudio : AudioController {
        override fun play(sfx: Sfx) = Unit
        override fun setSfxEnabled(enabled: Boolean) = Unit
        override fun setMusicEnabled(enabled: Boolean) = Unit
        override fun setSfxVolume(volume: Float) = Unit
        override fun setMusicVolume(volume: Float) = Unit
        override fun playMusic(track: MusicTrack) = Unit
        override fun stopMusic() = Unit
    }
}
