package com.nikodindon.retrocade

import android.app.Application
import com.nikodindon.retrocade.core.billing.BillingRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RetrocadeApplication : Application() {
    @Inject
    lateinit var billingRepository: BillingRepository

    override fun onCreate() {
        super.onCreate()
        // Connect once at process start so the first game already knows whether
        // the user is Premium. The state is cached for offline starts.
        billingRepository.initialize()
    }
}
