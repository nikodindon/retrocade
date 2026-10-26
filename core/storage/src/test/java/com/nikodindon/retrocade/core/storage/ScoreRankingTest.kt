package com.nikodindon.retrocade.core.storage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScoreRankingTest {
    private fun entry(score: Int, timestamp: Long = score.toLong()) =
        ScoreEntry(score = score, lines = 1, level = 1, timestamp = timestamp)

    @Test
    fun `keeps only the ten best scores sorted descending`() {
        var book = ScoreBook()
        repeat(15) { index ->
            val (updated, _) = ScoreRanking.insert(book, "tetris", entry(index * 10))
            book = updated
        }

        val game = book.forGame("tetris")
        assertEquals(10, game.entries.size)
        assertEquals(140, game.best)
        assertEquals(listOf(140, 130, 120, 110, 100, 90, 80, 70, 60, 50), game.entries.map { it.score })
    }

    @Test
    fun `flags a first score as best and rank one`() {
        val (book, result) = ScoreRanking.insert(ScoreBook(), "tetris", entry(500))

        assertTrue(result.isNewBest)
        assertEquals(1, result.rank)
        assertEquals(500, result.best)
        assertEquals(1, book.forGame("tetris").entries.size)
    }

    @Test
    fun `a lower score is ranked but not a new best`() {
        val (book, _) = ScoreRanking.insert(ScoreBook(), "tetris", entry(500))
        val (updated, result) = ScoreRanking.insert(book, "tetris", entry(200))

        assertFalse(result.isNewBest)
        assertEquals(2, result.rank)
        assertEquals(500, updated.forGame("tetris").best)
    }

    @Test
    fun `a score outside the top ten has no rank`() {
        var book = ScoreBook()
        repeat(10) { index ->
            val (updated, _) = ScoreRanking.insert(book, "tetris", entry(1000 - index * 10))
            book = updated
        }
        val (_, result) = ScoreRanking.insert(book, "tetris", entry(5))

        assertNull(result.rank)
        assertFalse(result.isNewBest)
        assertEquals(1000, result.best)
    }

    @Test
    fun `games are isolated from each other`() {
        val (book, _) = ScoreRanking.insert(ScoreBook(), "tetris", entry(300))
        val (updated, _) = ScoreRanking.insert(book, "arkanoid", entry(900))

        assertEquals(300, updated.forGame("tetris").best)
        assertEquals(900, updated.forGame("arkanoid").best)
    }
}
