package com.nikodindon.retrocade.core.storage

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * Pure JSON codec for local scores. Kept outside the repository so migration can be
 * tested without Android or DataStore.
 *
 * v0.1 already stored a [ScoreBook] under `scores_json`; the fallback accepts a bare
 * [GameScores] object too, which covers early development builds. Both shapes keep
 * the user's top 10 and never reset an existing installation.
 */
internal object ScoreCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun decode(raw: String?): ScoreBook {
        if (raw.isNullOrBlank()) return ScoreBook()
        val element = runCatching { json.parseToJsonElement(raw) }
            .getOrNull() as? JsonObject
            ?: return ScoreBook()

        // The v0.1 format is a ScoreBook object with a top-level "games" map.
        // A bare GameScores object is accepted for very old development builds.
        return if (element.containsKey("games")) {
            runCatching { json.decodeFromString(ScoreBook.serializer(), raw) }.getOrNull()
                ?: ScoreBook()
        } else {
            runCatching { json.decodeFromString(GameScores.serializer(), raw) }
                .getOrNull()
                ?.let { legacy -> ScoreBook(games = mapOf(LEGACY_SINGLE_GAME_ID to legacy)) }
                ?: ScoreBook()
        }
    }

    fun encode(book: ScoreBook): String =
        json.encodeToString(ScoreBook.serializer(), book)

    private const val LEGACY_SINGLE_GAME_ID = "tetris"
}
