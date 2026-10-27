package com.nikodindon.retrocade.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/** The only haptic vocabulary a game may use. */
enum class HapticIntensity {
    LIGHT,
    STRONG,
}

/**
 * Abstraction over Compose haptics. Games never touch [LocalHapticFeedback]
 * directly, so the global vibration setting is always honoured.
 */
fun interface HapticsController {
    fun perform(intensity: HapticIntensity)
}

@Composable
fun rememberHapticsController(enabled: Boolean): HapticsController {
    val haptics = LocalHapticFeedback.current
    return remember(enabled, haptics) {
        HapticsController { intensity ->
            if (!enabled) return@HapticsController
            haptics.performHapticFeedback(
                when (intensity) {
                    HapticIntensity.LIGHT -> HapticFeedbackType.TextHandleMove
                    HapticIntensity.STRONG -> HapticFeedbackType.LongPress
                },
            )
        }
    }
}
