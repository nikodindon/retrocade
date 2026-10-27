package com.nikodindon.retrocade.games.api

import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single source of truth for installed games.
 *
 * Hilt multibinding supplies every [Game] implementation. The registry keeps them
 * sorted for the hub and resolves routes by stable id, so neither the hub nor the
 * navigation graph references a concrete game.
 */
@Singleton
class GameRegistry @Inject constructor(
    games: Set<@JvmSuppressWildcards Game>,
) {
    /** New games only need their own Hilt binding and an app dependency. */
    val all: List<Game> = games.sortedWith(compareBy({ it.meta.name.lowercase() }, { it.meta.id }))

    private val byId: Map<String, Game> = games.associateBy { it.meta.id }

    init {
        require(games.all { it.meta.id.isNotBlank() }) { "Every game must declare a stable id" }
        require(byId.size == games.size) {
            "Duplicate game ids: ${games.groupBy { it.meta.id }.filterValues { it.size > 1 }.keys}"
        }
    }

    fun find(gameId: String): Game? = byId[gameId]
}
