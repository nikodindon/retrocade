package com.nikodindon.retrocade.games.tetris

import com.nikodindon.retrocade.core.common.ControlScheme
import com.nikodindon.retrocade.core.common.ProceduralIcon
import com.nikodindon.retrocade.games.api.Game
import com.nikodindon.retrocade.games.api.GameMeta
import com.nikodindon.retrocade.games.api.GameSession
import com.nikodindon.retrocade.games.api.GameSettings
import com.nikodindon.retrocade.games.api.GameUiContext
import com.nikodindon.retrocade.games.tetris.ui.TetrisGameScreen
import androidx.compose.runtime.Composable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TetrisGame @Inject constructor() : Game {
    override val meta = GameMeta(
        id = GAME_ID,
        name = "Tetris",
        tagline = "Briques de légende",
        description = "Empilez les térominos, complétez des lignes et tenez le plus longtemps possible.",
        categoryId = "puzzle",
        icon = ProceduralIcon(
            rows = listOf(
                "..XX....",
                "..XX....",
                "XXXXXXXX",
                "XXXXXXXX",
                "..XX....",
                "..XX....",
                "........",
                "........",
            ),
            color = 0xFF39FFB6,
        ),
        accentColor = 0xFF39FFB6,
        releaseLabel = "v0.1",
    )

    override fun createSession(seed: Long, settings: GameSettings): GameSession =
        TetrisSession(seed, settings)

    @Composable
    override fun GameScreen(context: GameUiContext) {
        TetrisGameScreen(context)
    }

    companion object {
        const val GAME_ID = "tetris"
        val DEFAULT_SETTINGS = GameSettings(controlScheme = ControlScheme.TAP)
    }
}
