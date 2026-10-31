package com.nikodindon.retrocade.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class PlayBillingGateway @Inject constructor(
    @ApplicationContext context: Context,
) : BillingGateway {

    private var updateListener: (() -> Unit)? = null

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(
            PurchasesUpdatedListener { _, _ ->
                updateListener?.invoke()
            },
        )
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build(),
        )
        .build()

    @Volatile
    private var connected = false

    override fun setPurchasesUpdatedListener(listener: () -> Unit) {
        updateListener = listener
    }

    override suspend fun connect(): Boolean {
        if (connected && client.isReady) return true
        return suspendCancellableCoroutine { continuation ->
            client.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        connected = result.responseCode == BillingClient.BillingResponseCode.OK
                        if (continuation.isActive) continuation.resume(connected)
                    }

                    override fun onBillingServiceDisconnected() {
                        connected = false
                    }
                },
            )
        }
    }

    override suspend fun queryProducts(ids: BillingProductIds): List<BillingProduct> {
        if (ids.lifetime.isBlank()) return emptyList()
        if (!connect()) return emptyList()
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(ids.lifetime)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        return suspendCancellableCoroutine { continuation ->
            client.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build(),
            ) { result: BillingResult, queryResult: QueryProductDetailsResult ->
                if (!continuation.isActive) return@queryProductDetailsAsync
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    continuation.resume(emptyList())
                    return@queryProductDetailsAsync
                }
                continuation.resume(
                    queryResult.productDetailsList.mapNotNull { it.toProduct(ids) },
                )
            }
        }
    }

    override suspend fun queryPurchases(): List<PurchaseInfo>? {
        if (!connect()) return null
        val inApp = queryPurchases(BillingClient.ProductType.INAPP) ?: return null
        return inApp.map { purchase ->
            PurchaseInfo(
                productId = purchase.products.firstOrNull(),
                purchaseToken = purchase.purchaseToken,
                acknowledged = purchase.isAcknowledged,
                purchased = purchase.purchaseState == Purchase.PurchaseState.PURCHASED,
            )
        }
    }

    private suspend fun queryPurchases(type: String): List<Purchase>? =
        suspendCancellableCoroutine { continuation ->
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(type).build(),
            ) { result, purchases ->
                if (!continuation.isActive) return@queryPurchasesAsync
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(purchases)
                } else {
                    continuation.resume(null)
                }
            }
        }

    override suspend fun acknowledge(purchaseToken: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            client.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchaseToken)
                    .build(),
            ) { result ->
                if (continuation.isActive) {
                    continuation.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                }
            }
        }

    override suspend fun launchPurchase(activity: Activity, product: BillingProduct): Boolean {
        if (!connect()) return false
        val details = suspendCancellableCoroutine<ProductDetails?> { continuation ->
            client.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        listOf(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(product.id)
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build(),
                        ),
                    )
                    .build(),
            ) { result: BillingResult, queryResult: QueryProductDetailsResult ->
                if (!continuation.isActive) return@queryProductDetailsAsync
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(queryResult.productDetailsList.firstOrNull())
                } else {
                    continuation.resume(null)
                }
            }
        } ?: return false

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build(),
                ),
            )
            .build()

        return withContext(Dispatchers.Main) {
            val result = client.launchBillingFlow(activity, flowParams)
            result.responseCode == BillingClient.BillingResponseCode.OK
        }
    }

    private fun ProductDetails.toProduct(ids: BillingProductIds): BillingProduct? {
        if (productId != ids.lifetime) return null
        val price = oneTimePurchaseOfferDetailsList
            ?.firstOrNull()
            ?.formattedPrice
            ?: oneTimePurchaseOfferDetails?.formattedPrice
            ?: "—"
        return BillingProduct(
            id = productId,
            type = BillingProductType.LIFETIME,
            title = name,
            formattedPrice = price,
        )
    }
}
