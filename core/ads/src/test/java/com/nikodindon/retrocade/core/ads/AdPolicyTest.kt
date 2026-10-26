package com.nikodindon.retrocade.core.ads

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AdPolicyTest {
    @Test
    fun `shows for a free user after the first game when an ad is ready`() {
        val policy = AdPolicy(isPremium = { false }, isInterstitialReady = { true })
        assertTrue(policy.shouldShowInterstitial(isFirstGame = false))
    }

    @Test
    fun `never shows on the first game of the process`() {
        val policy = AdPolicy(isPremium = { false }, isInterstitialReady = { true })
        assertFalse(policy.shouldShowInterstitial(isFirstGame = true))
    }

    @Test
    fun `never shows for a premium user even when an ad is ready`() {
        val policy = AdPolicy(isPremium = { true }, isInterstitialReady = { true })
        assertFalse(policy.shouldShowInterstitial(isFirstGame = false))
    }

    @Test
    fun `does not block a game start when the ad is not loaded`() {
        val policy = AdPolicy(isPremium = { false }, isInterstitialReady = { false })
        assertFalse(policy.shouldShowInterstitial(isFirstGame = false))
    }

    @Test
    fun `never shows for a premium user without an ad`() {
        val policy = AdPolicy(isPremium = { true }, isInterstitialReady = { false })
        assertFalse(policy.shouldShowInterstitial(isFirstGame = false))
    }

    @Test
    fun `launch tracker counts every game launch`() {
        val tracker = GameLaunchTracker()
        assertTrue(tracker.next() == 1)
        assertTrue(tracker.next() == 2)
        assertTrue(tracker.next() == 3)
    }
}
