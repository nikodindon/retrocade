package com.nikodindon.retrocade.core.billing

import android.app.Activity
import com.nikodindon.retrocade.core.storage.CachedPremium
import com.nikodindon.retrocade.core.storage.PremiumCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BillingRepositoryTest {
    private val ids = BillingProductIds("monthly", "lifetime")

    @Test
    fun `no purchase becomes free and is cached`() = runTest {
        val gateway = FakeGateway()
        val cache = FakeCache()
        val repository = repository(gateway, cache, this)

        repository.initialize()
        advanceUntilIdle()

        assertEquals(PremiumState.Free, repository.premiumState.value)
        assertEquals("FREE", cache.value?.type)
        assertTrue(repository.initialized.value)
    }

    @Test
    fun `monthly purchase becomes monthly premium`() = runTest {
        val gateway = FakeGateway(
            purchases = listOf(purchase("monthly", acknowledged = true)),
        )
        val repository = repository(gateway, FakeCache(), this)

        repository.initialize()
        advanceUntilIdle()

        assertEquals(PremiumState.Premium(PremiumType.MONTHLY), repository.premiumState.value)
    }

    @Test
    fun `lifetime wins over monthly`() = runTest {
        val gateway = FakeGateway(
            purchases = listOf(
                purchase("monthly", acknowledged = true),
                purchase("lifetime", acknowledged = true),
            ),
        )
        val repository = repository(gateway, FakeCache(), this)

        repository.initialize()
        advanceUntilIdle()

        assertEquals(PremiumState.Premium(PremiumType.LIFETIME), repository.premiumState.value)
    }

    @Test
    fun `unacknowledged purchases are acknowledged`() = runTest {
        val gateway = FakeGateway(
            purchases = listOf(purchase("lifetime", acknowledged = false)),
        )
        val repository = repository(gateway, FakeCache(), this)

        repository.initialize()
        advanceUntilIdle()

        assertEquals(listOf("token-lifetime"), gateway.acknowledged)
    }

    @Test
    fun `pending purchase does not grant premium`() = runTest {
        val gateway = FakeGateway(
            purchases = listOf(purchase("lifetime", acknowledged = true, purchased = false)),
        )
        val repository = repository(gateway, FakeCache(), this)

        repository.initialize()
        advanceUntilIdle()

        assertEquals(PremiumState.Free, repository.premiumState.value)
        assertNull(gateway.acknowledged.firstOrNull())
    }

    @Test
    fun `cached premium survives an offline verification failure`() = runTest {
        val cache = FakeCache(
            CachedPremium(PremiumType.LIFETIME.name, cachedAtEpochMillis = 123L),
        )
        val gateway = FakeGateway(connectResult = false)
        val repository = repository(gateway, cache, this)

        repository.initialize()
        advanceUntilIdle()

        assertEquals(PremiumState.Premium(PremiumType.LIFETIME), repository.premiumState.value)
        assertFalse(repository.initialized.value)
    }

    @Test
    fun `query failure keeps the cached state`() = runTest {
        val cache = FakeCache(
            CachedPremium(PremiumType.MONTHLY.name, cachedAtEpochMillis = 123L),
        )
        val gateway = FakeGateway(queryFails = true)
        val repository = repository(gateway, cache, this)

        repository.initialize()
        advanceUntilIdle()

        assertEquals(PremiumState.Premium(PremiumType.MONTHLY), repository.premiumState.value)
    }

    @Test
    fun `restore drops premium when Play reports no purchases`() = runTest {
        val cache = FakeCache(
            CachedPremium(PremiumType.MONTHLY.name, cachedAtEpochMillis = 123L),
        )
        val gateway = FakeGateway()
        val repository = repository(gateway, cache, this)

        repository.initialize()
        advanceUntilIdle()
        repository.refreshPurchases()
        advanceUntilIdle()

        assertEquals(PremiumState.Free, repository.premiumState.value)
    }

    @Test
    fun `products are loaded for the paywall`() = runTest {
        val gateway = FakeGateway(
            products = listOf(
                BillingProduct("monthly", BillingProductType.MONTHLY, "Mensuel", "2,99 €", "offer"),
                BillingProduct("lifetime", BillingProductType.LIFETIME, "À vie", "19,99 €", null),
            ),
        )
        val repository = repository(gateway, FakeCache(), this)

        repository.initialize()
        advanceUntilIdle()

        assertEquals(2, repository.products.value.size)
        assertEquals("19,99 €", repository.products.value.last().formattedPrice)
    }

    private fun repository(
        gateway: FakeGateway,
        cache: FakeCache,
        scope: CoroutineScope,
    ): BillingRepository =
        PlayBillingRepository(
            gateway = gateway,
            cache = cache,
            ids = ids,
            scope = scope,
        )

    private fun purchase(
        productId: String,
        acknowledged: Boolean,
        purchased: Boolean = true,
    ) = PurchaseInfo(
        productId = productId,
        purchaseToken = "token-$productId",
        acknowledged = acknowledged,
        purchased = purchased,
    )

    private class FakeGateway(
        private val connectResult: Boolean = true,
        var purchases: List<PurchaseInfo> = emptyList(),
        var products: List<BillingProduct> = emptyList(),
        private val queryFails: Boolean = false,
    ) : BillingGateway {
        val acknowledged = mutableListOf<String>()
        private var listener: (() -> Unit)? = null

        override fun setPurchasesUpdatedListener(listener: () -> Unit) {
            this.listener = listener
        }

        fun simulatePurchaseUpdate() {
            listener?.invoke()
        }

        override suspend fun connect(): Boolean = connectResult

        override suspend fun queryProducts(ids: BillingProductIds): List<BillingProduct> = products

        override suspend fun queryPurchases(): List<PurchaseInfo>? =
            if (queryFails) null else purchases

        override suspend fun acknowledge(purchaseToken: String): Boolean {
            acknowledged += purchaseToken
            return true
        }

        override suspend fun launchPurchase(activity: Activity, product: BillingProduct): Boolean = true
    }

    private class FakeCache(var value: CachedPremium? = null) : PremiumCache {
        override suspend fun read(): CachedPremium? = value

        override suspend fun write(premium: CachedPremium?) {
            value = premium
        }
    }
}
