package com.nikodindon.retrocade.core.storage

import com.nikodindon.retrocade.core.common.ControlScheme
import com.nikodindon.retrocade.core.common.GestureSensitivity
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * A v0.2 installation stores the same versioned JSON shapes as this release.
 * This test guards the upgrade path before publishing v1.0.0: no key rename,
 * no destructive decoding, and the onboarding flag stays set.
 */
class UpgradeMigrationTest {
    @Test
    fun `v0_2 settings and scores survive the v1 upgrade`() = runTest {
        val store = FakePreferencesDataStore().apply {
            writeRaw(
                "settings_json",
                """
                {
                  "sfxEnabled": false,
                  "musicEnabled": true,
                  "sfxVolume": 42,
                  "musicVolume": 17,
                  "hapticsEnabled": false,
                  "controlScheme": "BUTTONS",
                  "gestureSensitivity": "HIGH",
                  "swipeSensitivity": 0.5,
                  "showHoldButton": false,
                  "onboardingDone": true
                }
                """.trimIndent(),
            )
            writeRaw(
                "scores_json",
                """
                {
                  "games": {
                    "tetris": {
                      "best": 4321,
                      "entries": [
                        {"score": 4321, "lines": 21, "level": 4, "timestamp": 1729900000000}
                      ]
                    }
                  }
                }
                """.trimIndent(),
            )
        }

        val settings = SettingsRepository(store).current()
        val scores = ScoreRepository(store).current().forGame("tetris")

        assertFalse(settings.sfxEnabled)
        assertEquals(42, settings.sfxVolume)
        assertEquals(17, settings.musicVolume)
        assertFalse(settings.hapticsEnabled)
        assertEquals(ControlScheme.BUTTONS, settings.controls)
        assertEquals(GestureSensitivity.HIGH, settings.gestures)
        assertFalse(settings.showHoldButton)
        assertTrue(settings.onboardingDone)
        assertEquals(4321, scores.best)
        assertEquals(1, scores.entries.size)
    }
}
