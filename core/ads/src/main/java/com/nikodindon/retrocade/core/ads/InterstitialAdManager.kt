package com.nikodindon.retrocade.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads and shows the game-start interstitial. Showing is always best-effort:
 * a missing or failed ad never blocks navigation or a game session.
 */
@Singleton
class InterstitialAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val config: AdsConfig,
) {
    private val loading = AtomicBoolean(false)

    @Volatile
    private var ad: InterstitialAd? = null

    @Volatile
    private var sdkReady = false

    @Volatile
    private var adsAllowed = false

    val isReady: Boolean
        get() = adsAllowed && ad != null

    fun setAdsAllowed(allowed: Boolean) {
        adsAllowed = allowed
        if (!allowed) {
            ad = null
        }
    }

    fun onSdkInitialized() {
        sdkReady = true
        preload()
    }

    fun preload() {
        if (!adsAllowed || !sdkReady || config.interstitialUnitId.isBlank()) return
        if (ad != null) return
        if (!loading.compareAndSet(false, true)) return

        Log.d(TAG, "Loading interstitial (test=${config.useTestAds})")
        InterstitialAd.load(
            context,
            config.interstitialUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(loadedAd: InterstitialAd) {
                    loading.set(false)
                    // Consent may have been withdrawn while the request was in flight.
                    ad = if (adsAllowed) loadedAd else null
                    Log.d(TAG, "Interstitial loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading.set(false)
                    ad = null
                    Log.w(TAG, "Interstitial failed to load: ${error.code} ${error.message}")
                }
            },
        )
    }

    /**
     * Shows the current ad if available and immediately starts loading the next one.
     * Returns false without side effects when no ad is ready. [onFinished] runs once
     * when the ad is dismissed or fails to display, so callers can start the game
     * without ever running a session behind a fullscreen ad.
     */
    fun show(activity: Activity, onFinished: () -> Unit): Boolean {
        if (!adsAllowed) return false
        val current = ad ?: return false
        ad = null
        var finished = false
        fun finish() {
            if (finished) return
            finished = true
            onFinished()
        }
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial dismissed")
                finish()
                preload()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "Interstitial failed to show: ${error.code} ${error.message}")
                finish()
                preload()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial shown")
            }
        }
        return runCatching {
            current.show(activity)
            true
        }.getOrElse {
            Log.w(TAG, "Interstitial show threw", it)
            finish()
            preload()
            false
        }
    }

    private companion object {
        const val TAG = "RetrocadeAds"
    }
}
