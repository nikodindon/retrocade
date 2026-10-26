package com.nikodindon.retrocade.games.tetris.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nikodindon.retrocade.core.audio.AudioController
import com.nikodindon.retrocade.core.audio.MusicTrack
import com.nikodindon.retrocade.core.audio.Sfx
import com.nikodindon.retrocade.core.ui.RetrocadeTheme
import com.nikodindon.retrocade.games.api.GameSettings
import com.nikodindon.retrocade.games.tetris.TetrisSession
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TetrisScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val silentAudio = object : AudioController {
        override fun play(sfx: Sfx) = Unit

        override fun setSfxEnabled(enabled: Boolean) = Unit

        override fun setMusicEnabled(enabled: Boolean) = Unit

        override fun playMusic(track: MusicTrack) = Unit

        override fun stopMusic() = Unit
    }

    @Before
    fun freezeGameClock() {
        // The game runs on withFrameNanos. Advancing the clock manually keeps the
        // test deterministic instead of letting gravity stack pieces until game over.
        compose.mainClock.autoAdvance = false
    }

    private fun setTetrisContent() {
        val session = TetrisSession(seed = 42L, settings = GameSettings())
        compose.setContent {
            RetrocadeTheme {
                TetrisScreen(
                    session = session,
                    audio = silentAudio,
                    bestScore = 0,
                    sfxEnabled = false,
                    musicEnabled = false,
                    showHoldButton = true,
                    onExit = {},
                    onReplay = {},
                    onOpenPaywall = {},
                    onRecordScore = { score, lines, level ->
                        TetrisScoreResult(isNewBest = false, best = score + lines + level, rank = null)
                    },
                )
            }
        }
    }

    private fun advanceFrame() {
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
    }

    @Test
    fun boardAndHudAreRendered() {
        setTetrisContent()

        compose.onNodeWithTag("tetris_board").assertIsDisplayed()
        compose.onNodeWithTag("tetris_score").assertIsDisplayed()
        compose.onNodeWithTag("tetris_pause").assertIsDisplayed()
    }

    @Test
    fun pauseButtonShowsPauseOverlay() {
        setTetrisContent()

        compose.onNodeWithTag("tetris_pause").performClick()
        advanceFrame()

        compose.onNodeWithTag("tetris_pause_overlay").assertIsDisplayed()
        compose.onNodeWithText("PAUSE").assertIsDisplayed()
    }

    @Test
    fun pauseOverlayCanResumeTheGame() {
        setTetrisContent()

        compose.onNodeWithTag("tetris_pause").performClick()
        advanceFrame()
        compose.onNodeWithText("REPRENDRE").performClick()
        advanceFrame()

        compose.onNodeWithTag("tetris_pause_overlay").assertDoesNotExist()
        compose.onNodeWithTag("tetris_board").assertIsDisplayed()
    }
}
