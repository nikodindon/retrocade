package com.nikodindon.retrocade.hub

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nikodindon.retrocade.core.common.ProceduralIcon
import com.nikodindon.retrocade.core.ui.RetrocadeTheme
import com.nikodindon.retrocade.games.api.Game
import com.nikodindon.retrocade.games.api.GameMeta
import com.nikodindon.retrocade.games.api.GameSession
import com.nikodindon.retrocade.games.api.GameSettings
import com.nikodindon.retrocade.games.api.GameUiContext
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HubScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun loadingStateIsShownWhileScoresLoad() {
        setHubContent(state = null)

        compose.onNodeWithTag("hub_loading").assertIsDisplayed()
    }

    @Test
    fun emptyStateIsShownWhenNoGameIsInstalled() {
        setHubContent(state = HubUiState(games = emptyList()))

        compose.onNodeWithTag("hub_empty").assertIsDisplayed()
    }

    @Test
    fun playedGameShowsItsLocalBestScore() {
        compose.setContent {
            RetrocadeTheme {
                HubContent(
                    state = HubUiState(
                        games = listOf(HubGame(game = fakeGame("tetris"), best = 1_234)),
                    ),
                    premium = false,
                    onGame = {},
                    onScores = {},
                    onSettings = {},
                    onStore = {},
                )
            }
        }

        compose.onNodeWithText("MEILLEUR SCORE", substring = true).assertIsDisplayed()
    }

    @Test
    fun unplayedGameShowsItsCallToActionAndOpensTheGenericRoute() {
        var openedGame: String? = null
        compose.setContent {
            RetrocadeTheme {
                HubContent(
                    state = HubUiState(
                        games = listOf(HubGame(game = fakeGame("tetris"), best = null)),
                    ),
                    premium = false,
                    onGame = { openedGame = it },
                    onScores = {},
                    onSettings = {},
                    onStore = {},
                )
            }
        }

        compose.onNodeWithTag("hub_game_tetris").assertIsDisplayed()
        compose.onNodeWithText("PAS ENCORE JOUÉ").assertIsDisplayed()
        compose.onNodeWithTag("hub_game_tetris").performClick()

        assertEquals("tetris", openedGame)
    }

    private fun setHubContent(state: HubUiState?) {
        compose.setContent {
            RetrocadeTheme {
                HubContent(
                    state = state,
                    premium = false,
                    onGame = {},
                    onScores = {},
                    onSettings = {},
                    onStore = {},
                )
            }
        }
    }

    private fun fakeGame(id: String): Game = object : Game {
        override val meta = GameMeta(
            id = id,
            name = "Tetris",
            tagline = "Briques de légende",
            description = "Test",
            categoryId = "puzzle",
            icon = ProceduralIcon(rows = listOf("X"), color = 0xFF39FFB6),
            accentColor = 0xFF39FFB6,
            releaseLabel = "v0.1",
        )

        override fun createSession(seed: Long, settings: GameSettings): GameSession =
            error("not used")

        @Composable
        override fun GameScreen(context: GameUiContext) = Unit
    }
}
