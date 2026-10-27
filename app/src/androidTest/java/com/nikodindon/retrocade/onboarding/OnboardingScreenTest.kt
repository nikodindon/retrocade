package com.nikodindon.retrocade.onboarding

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
class OnboardingScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun skipIsAvailableOnEveryPageAndCompletesOnboarding() {
        var done = false
        compose.setContent {
            RetrocadeTheme {
                OnboardingScreen(onDone = { done = true })
            }
        }

        compose.onNodeWithTag("onboarding_page_1").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_skip").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_next").performClick()
        compose.onNodeWithTag("onboarding_page_2").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_skip").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_next").performClick()
        compose.onNodeWithTag("onboarding_page_3").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_skip").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_skip").performClick()

        assertTrue(done)
    }

    @Test
    fun theLastPageStartsTheApp() {
        var done = false
        compose.setContent {
            RetrocadeTheme {
                OnboardingScreen(onDone = { done = true })
            }
        }

        repeat(2) { compose.onNodeWithTag("onboarding_next").performClick() }
        compose.onNodeWithText("COMMENCER").performClick()

        assertTrue(done)
    }
}
