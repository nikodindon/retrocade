package com.nikodindon.retrocade.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.nikodindon.retrocade.core.billing.BillingRepository
import com.nikodindon.retrocade.core.billing.PremiumState
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
 * Runs the UMP flow once, then initializes AdMob only when consent allows ads.
 *
 * It is invoked by the Activity, never from Application.onCreate, and it is never
 * awaited by navigation or by the game loop. A consent failure keeps ads disabled:
 * a missing ad never blocks the app.
 */
@Singleton
class MonetizationController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val interstitialAdManager: InterstitialAdManager,
    private val billingRepository: BillingRepository,
) {
    private val started = AtomicBoolean(false)
    private val adsInitialized = AtomicBoolean(false)
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    init {
        // Billing may finish after the consent callback. Re-evaluate when the
        // entitlement becomes known, while still requiring canRequestAds().
        mainScope.launch {
            billingRepository.premiumState.collect { initializeAdsIfAllowed() }
        }
    }

    fun ensureInitialized(activity: Activity) {
        if (!started.compareAndSet(false, true)) return
        gatherConsent(activity)
    }

    /**
     * Entry point for the mandatory "Options de confidentialité" settings item.
     * The callback receives a short French status message for the UI.
     */
    fun showPrivacyOptions(activity: Activity, onResult: (String) -> Unit) {
        if (consentInformation.privacyOptionsRequirementStatus !=
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        ) {
            onResult("Aucune option de confidentialité n'est requise dans votre région.")
            return
        }
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            val message = if (error != null) {
                Log.w(TAG, "Privacy options form error: ${error.message}")
                "Impossible d'ouvrir les options de confidentialité pour le moment."
            } else {
                "Préférences de confidentialité enregistrées."
            }
            mainScope.launch { onResult(message) }
            // The user may have granted or withdrawn consent: only load ads when
            // canRequestAds() is now true.
            initializeAdsIfAllowed()
        }
    }

    private fun gatherConsent(activity: Activity) {
        val parameters = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            parameters,
            {
                Log.d(TAG, "Consent info updated; canRequestAds=${consentInformation.canRequestAds()}")
                loadAndShowForm(activity)
            },
            { error ->
                Log.w(TAG, "Consent update failed: ${error.message}")
                // Do not initialize AdMob unless canRequestAds() explicitly allows it.
                initializeAdsIfAllowed()
            },
        )
    }

    private fun loadAndShowForm(activity: Activity) {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
            if (error != null) {
                Log.w(TAG, "Consent form error: ${error.message}")
            }
            initializeAdsIfAllowed()
        }
    }

    private fun initializeAdsIfAllowed() {
        // Unknown is treated like Premium: never load an ad for a payer or while
        // the entitlement is being verified.
        if (billingRepository.premiumState.value !is PremiumState.Free) {
            Log.d(TAG, "Ads disabled: user is Premium or entitlement is unknown")
            interstitialAdManager.setAdsAllowed(false)
            return
        }
        if (!consentInformation.canRequestAds()) {
            Log.d(TAG, "Ads disabled: consent does not allow ad requests")
            interstitialAdManager.setAdsAllowed(false)
            return
        }
        interstitialAdManager.setAdsAllowed(true)
        if (!adsInitialized.compareAndSet(false, true)) {
            // Consent changed after initialization: drop or reload as needed.
            interstitialAdManager.preload()
            return
        }
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
