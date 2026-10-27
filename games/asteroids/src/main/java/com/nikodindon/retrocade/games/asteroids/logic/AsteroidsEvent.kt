package com.nikodindon.retrocade.games.asteroids.logic

/** Events are emitted synchronously by the pure engine for audio/effects. */
sealed interface AsteroidsEvent {
    data object Shot : AsteroidsEvent
    data object ThrustStarted : AsteroidsEvent
    data object SaucerAppeared : AsteroidsEvent

    data class AsteroidDestroyed(
        val size: AsteroidSize,
        val x: Float,
        val y: Float,
        val colorIndex: Int,
    ) : AsteroidsEvent

    data class SaucerDestroyed(val x: Float, val y: Float) : AsteroidsEvent

    data class ShipHit(val lives: Int, val x: Float, val y: Float) : AsteroidsEvent

    data object ExtraLife : AsteroidsEvent

    data class WaveStarted(val wave: Int) : AsteroidsEvent

    data class GameOver(val score: Int, val wave: Int) : AsteroidsEvent
}
