package com.nikodindon.retrocade.core.storage

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScoreRepositoryTest {
    @Test
    fun `scores are isolated per game and share one store`() = runTest {
        val repository = ScoreRepository(FakePreferencesDataStore())

        repository.record(gameId = "tetris", score = 1_200, lines = 12, level = 2)
        repository.record(gameId = "arkanoid", score = 4_500, lines = 5, level = 1)
        repository.record(gameId = "tetris", score = 2_400, lines = 20, level = 3)

        val book = repository.current()
        assertEquals(2_400, book.forGame("tetris").best)
        assertEquals(listOf(2_400, 1_200), book.forGame("tetris").entries.map { it.score })
        assertEquals(4_500, book.forGame("arkanoid").best)
        assertEquals(1, book.forGame("arkanoid").entries.size)
    }

    @Test
    fun `each game keeps its own top ten`() = runTest {
        val repository = ScoreRepository(FakePreferencesDataStore())
        repeat(12) { index ->
            repository.record("tetris", score = index * 100, lines = 1, level = 1)
            repository.record("arkanoid", score = 10_000 + index, lines = 1, level = 1)
        }

        val book = repository.current()
        assertEquals(10, book.forGame("tetris").entries.size)
        assertEquals(10, book.forGame("arkanoid").entries.size)
        assertEquals(1_100, book.forGame("tetris").best)
        assertEquals(10_011, book.forGame("arkanoid").best)
    }

    @Test
    fun `v0_1 score JSON is still read after upgrade`() = runTest {
        val store = FakePreferencesDataStore().apply {
            writeRaw(
                "scores_json",
                """
                {
                  "games": {
                    "tetris": {
                      "best": 9876,
                      "entries": [
                        {"score": 9876, "lines": 42, "level": 7, "timestamp": 1729900000000},
                        {"score": 1234, "lines": 12, "level": 2, "timestamp": 1729800000000}
                      ]
                    }
                  }
                }
                """.trimIndent(),
            )
        }

        val scores = ScoreRepository(store).current().forGame("tetris")

        assertEquals(9876, scores.best)
        assertEquals(2, scores.entries.size)
        assertEquals(42, scores.entries.first().lines)
        assertTrue(scores.hasPlayed)
    }

    @Test
    fun `legacy single game payload is assigned to tetris`() = runTest {
        val store = FakePreferencesDataStore().apply {
            writeRaw(
                "scores_json",
                """
                {"best": 500, "entries": [{"score": 500, "lines": 5, "level": 1, "timestamp": 1}]}
                """.trimIndent(),
            )
        }

        val book = ScoreRepository(store).current()

        assertEquals(500, book.forGame("tetris").best)
        assertEquals(1, book.forGame("tetris").entries.size)
    }

    @Test
    fun `reset removes every game score but keeps the store readable`() = runTest {
        val store = FakePreferencesDataStore()
        val repository = ScoreRepository(store)
        repository.record("tetris", 100, 1, 1)
        repository.record("arkanoid", 200, 1, 1)

        repository.reset()

        assertTrue(repository.current().games.isEmpty())
        assertFalse(repository.current().forGame("tetris").hasPlayed)
    }
}
