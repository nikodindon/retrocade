package com.nikodindon.retrocade.core.billing

import android.app.Activity
import com.nikodindon.retrocade.core.storage.CachedPremium
import com.nikodindon.retrocade.core.storage.PremiumCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayBillingRepository @Inject constructor(
    private val gateway: BillingGateway,
    private val cache: PremiumCache,
    private val ids: BillingProductIds,
    @ApplicationScope private val scope: CoroutineScope,
) : BillingRepository {

    private val _premiumState = MutableStateFlow<PremiumState>(PremiumState.Unknown)
    override val premiumState: StateFlow<PremiumState> = _premiumState.asStateFlow()

    private val _products = MutableStateFlow<List<BillingProduct>>(emptyList())
    override val products: StateFlow<List<BillingProduct>> = _products.asStateFlow()

    private val _initialized = MutableStateFlow(false)
    override val initialized: StateFlow<Boolean> = _initialized.asStateFlow()

    private val started = AtomicBoolean(false)
    private var productsLoaded = false

    override fun initialize() {
        if (!started.compareAndSet(false, true)) return
        gateway.setPurchasesUpdatedListener {
            scope.launch { refreshPurchases() }
        }
        scope.launch {
            restoreCachedPremium()
            val connected = runCatching { gateway.connect() }.getOrDefault(false)
            _initialized.value = connected
            if (connected) {
                loadProducts()
                refreshPurchases()
            }
        }
    }

    override suspend fun refreshPurchases() {
        if (runCatching { gateway.connect() }.getOrDefault(false).not()) return
        val purchases = runCatching { gateway.queryPurchases() }.getOrNull() ?: return
        acknowledgeUnacknowledged(purchases)
        val newState = mapPremium(purchases, ids)
        _premiumState.value = newState
        cachePremium(newState)
    }

    override suspend fun loadProducts() {
        val loaded = runCatching { gateway.queryProducts(ids) }.getOrDefault(emptyList())
        if (loaded.isNotEmpty()) {
            _products.value = loaded
            productsLoaded = true
        }
    }

    override suspend fun purchase(activity: Activity, type: BillingProductType): Boolean {
        if (!productsLoaded) loadProducts()
        val product = _products.value.firstOrNull { it.type == type } ?: return false
        val launched = runCatching { gateway.launchPurchase(activity, product) }.getOrDefault(false)
        if (launched) {
            // The purchases-updated callback also triggers a refresh; doing one here
            // makes the state deterministic for clients that call purchase() directly.
            refreshPurchases()
        }
        return launched
    }

    private suspend fun acknowledgeUnacknowledged(purchases: List<PurchaseInfo>) {
        purchases.filter { it.purchased && !it.acknowledged }.forEach { purchase ->
            runCatching { gateway.acknowledge(purchase.purchaseToken) }
        }
    }

    private fun mapPremium(
        purchases: List<PurchaseInfo>,
        ids: BillingProductIds,
    ): PremiumState {
        val active = purchases.filter { it.purchased }
        return when {
            active.any { it.productId == ids.lifetime } ->
                PremiumState.Premium(PremiumType.LIFETIME)

            active.any { it.productId == ids.monthly } ->
                PremiumState.Premium(PremiumType.MONTHLY)

            else -> PremiumState.Free
        }
    }

    private suspend fun restoreCachedPremium() {
        val cached = runCatching { cache.read() }.getOrNull() ?: return
        _premiumState.value = when (cached.type) {
            PremiumType.LIFETIME.name -> PremiumState.Premium(PremiumType.LIFETIME)
            PremiumType.MONTHLY.name -> PremiumState.Premium(PremiumType.MONTHLY)
            FREE_CACHE -> PremiumState.Free
            else -> PremiumState.Unknown
        }
    }

    private suspend fun cachePremium(state: PremiumState) {
        val cached = when (state) {
            is PremiumState.Premium -> CachedPremium(
                type = state.type.name,
                cachedAtEpochMillis = System.currentTimeMillis(),
            )

            PremiumState.Free -> CachedPremium(
                type = FREE_CACHE,
                cachedAtEpochMillis = System.currentTimeMillis(),
            )

            PremiumState.Unknown -> null
        }
        runCatching { cache.write(cached) }
    }

    private companion object {
        const val FREE_CACHE = "FREE"
    }
}
