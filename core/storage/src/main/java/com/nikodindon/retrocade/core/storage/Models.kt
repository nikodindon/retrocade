package com.nikodindon.retrocade.core.storage

import com.nikodindon.retrocade.core.common.ControlScheme
import com.nikodindon.retrocade.core.common.GestureSensitivity
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val sfxEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    /** 0..100, persisted as a percentage. */
    val sfxVolume: Int = 75,
    /** 0..100, persisted as a percentage. */
    val musicVolume: Int = 60,
    val hapticsEnabled: Boolean = true,
    val controlScheme: String = ControlScheme.TAP.name,
    /** Stable name of a [GestureSensitivity]. Empty means "migrate from swipeSensitivity". */
    val gestureSensitivity: String = "",
    /** v0.1 field kept for migration when gestureSensitivity is absent. */
    val swipeSensitivity: Float = 0.5f,
    val showHoldButton: Boolean = true,
    /** Set once the first-run tour has been completed or skipped. */
    val onboardingDone: Boolean = false,
) {
    val controls: ControlScheme
        get() = ControlScheme.fromStorage(controlScheme)

    val gestures: GestureSensitivity
        get() = if (gestureSensitivity.isBlank()) {
            GestureSensitivity.fromLegacySwipe(swipeSensitivity)
        } else {
            GestureSensitivity.fromStorage(gestureSensitivity)
        }
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
) {
    val hasPlayed: Boolean get() = entries.isNotEmpty()
}

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
