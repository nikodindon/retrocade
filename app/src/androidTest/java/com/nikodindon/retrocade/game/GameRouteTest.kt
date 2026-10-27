package com.nikodindon.retrocade.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nikodindon.retrocade.core.ui.RetrocadeTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameRouteTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun unknownGameShowsACleanErrorInsteadOfCrashing() {
        var exited = false
        compose.setContent {
            RetrocadeTheme {
                UnknownGameScreen(onExit = { exited = true })
            }
        }

        compose.onNodeWithTag("game_unknown").assertIsDisplayed()
        compose.onNodeWithText("JEU INTROUVABLE").assertIsDisplayed()
        compose.onNodeWithText("RETOUR AU HUB").performClick()

        assertTrue(exited)
    }
}
