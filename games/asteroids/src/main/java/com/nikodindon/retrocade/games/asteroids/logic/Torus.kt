package com.nikodindon.retrocade.games.asteroids.logic

/** Logical square world. Rendering scales it to the available canvas. */
const val WORLD_WIDTH = 1_000f
const val WORLD_HEIGHT = 1_000f

/** Wraps a coordinate into [0, size). */
fun wrapCoordinate(value: Float, size: Float): Float {
    if (size <= 0f) return 0f
    val wrapped = value % size
    return if (wrapped < 0f) wrapped + size else wrapped
}

/**
 * Shortest signed delta from [from] to [to] on a torus of length [size].
 * A delta exactly half a world away is returned as positive.
 */
fun torusDelta(from: Float, to: Float, size: Float): Float {
    if (size <= 0f) return 0f
    var delta = (to - from) % size
    if (delta > size / 2f) delta -= size
    if (delta < -size / 2f) delta += size
    return delta
}

/** Squared distance using the shortest path on the torus. */
fun torusDistanceSquared(
    ax: Float,
    ay: Float,
    bx: Float,
    by: Float,
): Float {
    val dx = torusDelta(ax, bx, WORLD_WIDTH)
    val dy = torusDelta(ay, by, WORLD_HEIGHT)
    return dx * dx + dy * dy
}

/** Distance in pixels, used by the collision helpers and tests. */
fun torusDistance(
    ax: Float,
    ay: Float,
    bx: Float,
    by: Float,
): Float = kotlin.math.sqrt(torusDistanceSquared(ax, ay, bx, by))

/** True when two circles overlap on the torus. */
fun torusCirclesOverlap(
    ax: Float,
    ay: Float,
    ar: Float,
    bx: Float,
    by: Float,
    br: Float,
): Boolean {
    val radius = ar + br
    return torusDistanceSquared(ax, ay, bx, by) <= radius * radius
}
