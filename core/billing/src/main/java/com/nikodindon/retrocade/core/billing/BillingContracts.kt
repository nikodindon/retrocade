package com.nikodindon.retrocade.core.billing

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

enum class PremiumType {
    MONTHLY,
    LIFETIME,
}

sealed interface PremiumState {
    data object Unknown : PremiumState

    data object Free : PremiumState

    data class Premium(val type: PremiumType) : PremiumState
}

enum class BillingProductType {
    MONTHLY,
    LIFETIME,
}

data class BillingProductIds(
    val monthly: String,
    val lifetime: String,
)

data class BillingProduct(
    val id: String,
    val type: BillingProductType,
    val title: String,
    val formattedPrice: String,
    val offerToken: String?,
)

data class PurchaseInfo(
    val productId: String?,
    val purchaseToken: String,
    val acknowledged: Boolean,
    val purchased: Boolean,
)

/**
 * Thin adapter over BillingClient so the repository can be tested with fakes.
 */
interface BillingGateway {
    fun setPurchasesUpdatedListener(listener: () -> Unit)

    suspend fun connect(): Boolean

    suspend fun queryProducts(ids: BillingProductIds): List<BillingProduct>

    /** Null means the query failed; an empty list means the user has no purchase. */
    suspend fun queryPurchases(): List<PurchaseInfo>?

    suspend fun acknowledge(purchaseToken: String): Boolean

    suspend fun launchPurchase(activity: Activity, product: BillingProduct): Boolean
}

interface BillingRepository {
    val premiumState: StateFlow<PremiumState>

    val products: StateFlow<List<BillingProduct>>

    val initialized: StateFlow<Boolean>

    fun initialize()

    suspend fun refreshPurchases()

    suspend fun loadProducts()

    suspend fun purchase(activity: Activity, type: BillingProductType): Boolean

    fun premiumType(): PremiumType? = (premiumState.value as? PremiumState.Premium)?.type
}
