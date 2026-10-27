package com.nikodindon.retrocade.games.api

import androidx.compose.runtime.Composable
import com.nikodindon.retrocade.core.common.ProceduralIcon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class GameRegistryTest {
    @Test
    fun `registry exposes every game sorted by name`() {
        val registry = GameRegistry(setOf(game("zeta", "Zeta"), game("alpha", "Alpha")))

        assertEquals(listOf("alpha", "zeta"), registry.all.map { it.meta.id })
    }

    @Test
    fun `unknown route id resolves to null`() {
        val registry = GameRegistry(setOf(game("alpha", "Alpha")))

        assertNull(registry.find("does-not-exist"))
        assertEquals("alpha", registry.find("alpha")?.meta?.id)
    }

    @Test
    fun `duplicate stable ids are rejected`() {
        val duplicate = setOf(game("alpha", "Alpha"), game("alpha", "Another alpha"))

        assertThrows(IllegalArgumentException::class.java) {
            GameRegistry(duplicate)
        }
    }

    @Test
    fun `blank ids are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            GameRegistry(setOf(game("", "Nameless")))
        }
    }

    private fun game(id: String, name: String): Game = object : Game {
        override val meta = GameMeta(
            id = id,
            name = name,
            tagline = "Test",
            description = "Test game",
            categoryId = "test",
            icon = ProceduralIcon(rows = listOf("X"), color = 0xFFFFFFFF),
            accentColor = 0xFF39FFB6,
            releaseLabel = "test",
        )

        override fun createSession(seed: Long, settings: GameSettings): GameSession =
            error("not used by registry tests")

        @Composable
        override fun GameScreen(context: GameUiContext) = Unit
    }
}
