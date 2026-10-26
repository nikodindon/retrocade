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

fun Int.coerceAtLeastZero(): Int = if (this < 0) 0 else this

/** Converts a 0..1 sensitivity into a swipe threshold in dp (lower = more sensitive). */
fun sensitivityToSwipeThresholdDp(sensitivity: Float): Float {
    val clamped = sensitivity.coerceIn(0f, 1f)
    return 36f - clamped * 24f
}
