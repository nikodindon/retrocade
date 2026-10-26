package com.nikodindon.retrocade.core.ads

import com.nikodindon.retrocade.core.billing.BillingRepository
import com.nikodindon.retrocade.core.billing.PremiumState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The only monetization question a game route is allowed to ask.
 *
 * The call site is the launch of a game. The policy returns false when the user is
 * Premium, when the entitlement is not yet known (safer than showing an ad to a
 * paying user), on the very first game of the process, or when no interstitial is
 * loaded. A missing ad never delays the game.
 */
class AdPolicy(
    private val isPremium: () -> Boolean,
    private val isInterstitialReady: () -> Boolean,
) {
    fun shouldShowInterstitial(isFirstGame: Boolean): Boolean =
        !isFirstGame && !isPremium() && isInterstitialReady()
}

/**
 * Counts game launches for the whole process. The first game never carries an ad:
 * it protects the very first impression and keeps the app free of ads before the
 * user has actually played once.
 */
@Singleton
class GameLaunchTracker @Inject constructor() {
    private val launches = AtomicInteger(0)

    fun next(): Int = launches.incrementAndGet()
}

data class AdsConfig(
    val interstitialUnitId: String,
    val useTestAds: Boolean,
)

@Module
@InstallIn(SingletonComponent::class)
object AdsModule {
    @Provides
    @Singleton
    fun provideAdPolicy(
        billingRepository: BillingRepository,
        interstitialAdManager: InterstitialAdManager,
    ): AdPolicy = AdPolicy(
        isPremium = {
            // Unknown is treated like Premium for the purpose of showing ads: never
            // risk interrupting a payer while the entitlement is being verified.
            billingRepository.premiumState.value !is PremiumState.Free
        },
        isInterstitialReady = { interstitialAdManager.isReady },
    )
}
