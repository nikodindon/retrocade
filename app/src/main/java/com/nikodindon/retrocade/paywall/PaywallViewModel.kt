package com.nikodindon.retrocade.paywall

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikodindon.retrocade.core.billing.BillingProduct
import com.nikodindon.retrocade.core.billing.BillingProductType
import com.nikodindon.retrocade.core.billing.BillingRepository
import com.nikodindon.retrocade.core.billing.PremiumState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
) : ViewModel() {
    val premium: StateFlow<PremiumState> = billingRepository.premiumState
    val products: StateFlow<List<BillingProduct>> = billingRepository.products
    val initialized: StateFlow<Boolean> = billingRepository.initialized

    var busy by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            billingRepository.loadProducts()
            billingRepository.refreshPurchases()
        }
    }

    fun purchase(activity: Activity, type: BillingProductType) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            val launched = billingRepository.purchase(activity, type)
            message = if (launched) {
                "Achat lancé. Google Play va confirmer la transaction."
            } else {
                "Achat indisponible pour le moment. Vérifiez votre connexion et Google Play."
            }
            busy = false
        }
    }

    fun restore() {
        if (busy) return
        busy = true
        viewModelScope.launch {
            val restored = billingRepository.refreshPurchases()
            message = if (restored) {
                "Achats restaurés depuis Google Play."
            } else {
                "Impossible de vérifier les achats. Vérifiez votre connexion et réessayez."
            }
            busy = false
        }
    }

    fun clearMessage() {
        message = null
    }
}
