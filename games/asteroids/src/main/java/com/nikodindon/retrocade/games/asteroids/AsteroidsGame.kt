package com.nikodindon.retrocade.games.asteroids

import androidx.compose.runtime.Composable
import com.nikodindon.retrocade.core.common.ControlScheme
import com.nikodindon.retrocade.core.common.ProceduralIcon
import com.nikodindon.retrocade.games.api.Game
import com.nikodindon.retrocade.games.api.GameMeta
import com.nikodindon.retrocade.games.api.GameSession
import com.nikodindon.retrocade.games.api.GameSettings
import com.nikodindon.retrocade.games.api.GameUiContext
import com.nikodindon.retrocade.games.asteroids.ui.AsteroidsGameScreen
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AsteroidsGame @Inject constructor() : Game {
    override val meta = GameMeta(
        id = GAME_ID,
        name = "Astéroïdes",
        tagline = "Champ de débris",
        description = "Pilotez un vaisseau à inertie, pulvérisez les rochers et survivez aux soucoupes.",
        categoryId = "shooter",
        icon = ProceduralIcon(
            rows = listOf(
                "........",
                "..X..X..",
                ".X....X.",
                "X..XX..X",
                ".X....X.",
                "..X..X..",
                "...XX...",
                "........",
            ),
            color = 0xFFFFC93F,
        ),
        accentColor = 0xFFFFC93F,
        releaseLabel = "v0.3",
    )

    override fun createSession(seed: Long, settings: GameSettings): GameSession =
        AsteroidsSession(seed, settings)

    @Composable
    override fun GameScreen(context: GameUiContext) {
        AsteroidsGameScreen(context)
    }

    companion object {
        const val GAME_ID = "asteroids"
        val DEFAULT_SETTINGS = GameSettings(controlScheme = ControlScheme.TAP)
    }
}
