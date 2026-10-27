package com.nikodindon.retrocade.core.common

/** How the player drives a game. Shared by storage and game settings. */
enum class ControlScheme {
    TAP,
    BUTTONS,
    ;

    companion object {
        fun fromStorage(value: String?): ControlScheme =
            entries.firstOrNull { it.name == value } ?: TAP
    }
}

/**
 * Gesture sensitivity has exactly three levels. A lower sensitivity requires a
 * longer swipe, which is friendlier on small screens or for new players.
 */
enum class GestureSensitivity(
    val label: String,
    val thresholdDp: Float,
) {
    LOW("Faible", 38f),
    NORMAL("Normale", 26f),
    HIGH("Élevée", 16f),
    ;

    companion object {
        fun fromStorage(value: String?): GestureSensitivity =
            entries.firstOrNull { it.name == value } ?: NORMAL

        /** Maps the v0.1 0..1 slider onto the three-level model. */
        fun fromLegacySwipe(value: Float): GestureSensitivity = when {
            value < 0.34f -> LOW
            value > 0.66f -> HIGH
            else -> NORMAL
        }
    }
}

fun Int.coerceAtLeastZero(): Int = if (this < 0) 0 else this

/** Converts a 0..1 sensitivity into a swipe threshold in dp (lower = more sensitive). */
fun sensitivityToSwipeThresholdDp(sensitivity: Float): Float {
    val clamped = sensitivity.coerceIn(0f, 1f)
    return 36f - clamped * 24f
}
