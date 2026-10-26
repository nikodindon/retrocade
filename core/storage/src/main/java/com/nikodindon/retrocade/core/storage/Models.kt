package com.nikodindon.retrocade.core.storage

import com.nikodindon.retrocade.core.common.ControlScheme
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val sfxEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val controlScheme: String = ControlScheme.TAP.name,
    val swipeSensitivity: Float = 0.5f,
    val showHoldButton: Boolean = true,
) {
    val controls: ControlScheme
        get() = ControlScheme.fromStorage(controlScheme)
}

@Serializable
data class ScoreEntry(
    val score: Int,
    val lines: Int,
    val level: Int,
    val timestamp: Long,
)

@Serializable
data class GameScores(
    val best: Int = 0,
    val entries: List<ScoreEntry> = emptyList(),
)

@Serializable
data class ScoreBook(
    val games: Map<String, GameScores> = emptyMap(),
) {
    fun forGame(gameId: String): GameScores = games[gameId] ?: GameScores()
}

data class RecordResult(
    val isNewBest: Boolean,
    val rank: Int?,
    val best: Int,
)

data class CachedPremium(
    val type: String,
    val cachedAtEpochMillis: Long,
)

internal object ScoreRanking {
    fun insert(
        book: ScoreBook,
        gameId: String,
        entry: ScoreEntry,
        limit: Int = 10,
    ): Pair<ScoreBook, RecordResult> {
        val current = book.forGame(gameId)
        val previousBest = current.best
        val merged = (current.entries + entry)
            .sortedWith(compareByDescending<ScoreEntry> { it.score }.thenBy { it.timestamp })
            .take(limit)
        val rank = merged.indexOfFirst { it === entry }.takeIf { it >= 0 }?.plus(1)
        val best = maxOf(previousBest, entry.score)
        val updated = GameScores(best = best, entries = merged)
        return book.copy(games = book.games + (gameId to updated)) to
            RecordResult(
                isNewBest = entry.score > previousBest,
                rank = rank,
                best = best,
            )
    }
}
