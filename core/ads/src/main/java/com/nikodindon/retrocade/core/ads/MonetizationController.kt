package com.nikodindon.retrocade.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs the UMP flow once, then initializes AdMob and preloads the interstitial.
 *
 * It is only invoked by the Activity, never from Application.onCreate, and it is
 * never awaited by navigation or by the game loop.
 */
@Singleton
class MonetizationController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val interstitialAdManager: InterstitialAdManager,
) {
    private val started = AtomicBoolean(false)
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    fun ensureInitialized(activity: Activity) {
        if (!started.compareAndSet(false, true)) return
        gatherConsent(activity)
    }

    private fun gatherConsent(activity: Activity) {
        val parameters = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            parameters,
            {
                Log.d(TAG, "Consent info updated; canRequestAds=${consentInformation.canRequestAds()}")
                loadAndShowForm(activity)
            },
            { error ->
                Log.w(TAG, "Consent update failed: ${error.message}")
                initializeAds()
            },
        )
    }

    private fun loadAndShowForm(activity: Activity) {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
            if (error != null) {
                Log.w(TAG, "Consent form error: ${error.message}")
            }
            initializeAds()
        }
    }

    private fun initializeAds() {
        // Consent callbacks may arrive on a binder thread; Mobile Ads wants main.
        mainScope.launch {
            MobileAds.initialize(context) {
                Log.d(TAG, "Mobile Ads initialized")
                interstitialAdManager.onSdkInitialized()
            }
        }
    }

    private companion object {
        const val TAG = "RetrocadeAds"
    }
}
