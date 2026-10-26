package com.nikodindon.retrocade

import com.nikodindon.retrocade.core.ads.AdsConfig
import com.nikodindon.retrocade.core.billing.BillingProductIds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideBillingProductIds(): BillingProductIds = BillingProductIds(
        monthly = BuildConfig.BILLING_MONTHLY_ID,
        lifetime = BuildConfig.BILLING_LIFETIME_ID,
    )

    @Provides
    @Singleton
    fun provideAdsConfig(): AdsConfig = AdsConfig(
        interstitialUnitId = BuildConfig.ADMOB_INTERSTITIAL_ID,
        useTestAds = BuildConfig.USE_TEST_ADS,
    )
}
