package com.nikodindon.retrocade.games.asteroids.logic

/** The three classic asteroid sizes and their scoring/fragmentation rules. */
enum class AsteroidSize(
    val radius: Float,
    val score: Int,
    val minFragments: Int,
    val maxFragments: Int,
) {
    LARGE(radius = 62f, score = 20, minFragments = 2, maxFragments = 3),
    MEDIUM(radius = 38f, score = 50, minFragments = 2, maxFragments = 3),
    SMALL(radius = 20f, score = 100, minFragments = 0, maxFragments = 0),
    ;

    fun smaller(): AsteroidSize? = when (this) {
        LARGE -> MEDIUM
        MEDIUM -> SMALL
        SMALL -> null
    }
}

/**
 * A drifting rock. [shape] holds radial multipliers around the bounding circle;
 * it is generated from the seed and never changes during the asteroid's life.
 */
data class Asteroid(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: AsteroidSize,
    val shape: FloatArray,
    var angle: Float,
    val spin: Float,
    val colorIndex: Int,
)

data class Ship(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var angle: Float,
    var invulnerabilityMs: Float,
    var alive: Boolean,
)

data class Bullet(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var lifeMs: Float,
    val fromPlayer: Boolean,
)

data class Saucer(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var shootTimerMs: Float,
    var remainingMs: Float,
)
