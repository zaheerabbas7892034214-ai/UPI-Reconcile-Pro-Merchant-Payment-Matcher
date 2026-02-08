package com.zaheer.upireconcilepro.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.zaheer.upireconcilepro.data.database.dao.EntitlementDao
import com.zaheer.upireconcilepro.data.database.entity.EntitlementEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingManager(
    private val context: Context,
    private val entitlementDao: EntitlementDao,
    private val scope: CoroutineScope
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        private const val PRODUCT_ID = "upireconcile_pro_yearly"
        private const val BASE_PLAN_ID = "yearly_base"
    }

    sealed class SubscriptionState {
        object Loading : SubscriptionState()
        object NotSubscribed : SubscriptionState()
        data class Subscribed(val expiryTime: Long?) : SubscriptionState()
        data class Pending : SubscriptionState()
        data class Error(val message: String) : SubscriptionState()
    }

    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Loading)
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private var billingClient: BillingClient? = null
    private var productDetails: ProductDetails? = null
    private var offerToken: String? = null

    init {
        initializeBillingClient()
    }

    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        startConnection()
    }

    private fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected")
                    queryProductDetails()
                    queryPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                    _subscriptionState.value = SubscriptionState.Error(
                        "Failed to connect to billing service"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected, will retry connection")
                _subscriptionState.value = SubscriptionState.Error(
                    "Billing service disconnected"
                )
            }
        })
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (productDetailsList.isNotEmpty()) {
                    productDetails = productDetailsList[0]
                    extractOfferToken()
                    Log.d(TAG, "Product details loaded: ${productDetails?.productId}")
                } else {
                    Log.e(TAG, "No product details found")
                    _subscriptionState.value = SubscriptionState.Error(
                        "Product not available"
                    )
                }
            } else {
                Log.e(TAG, "Failed to query product details: ${billingResult.debugMessage}")
                _subscriptionState.value = SubscriptionState.Error(
                    "Failed to load subscription details"
                )
            }
        }
    }

    private fun extractOfferToken() {
        productDetails?.subscriptionOfferDetails?.let { offers ->
            val basePlanOffer = offers.find { offer ->
                offer.basePlanId == BASE_PLAN_ID
            }
            
            if (basePlanOffer != null) {
                offerToken = basePlanOffer.offerToken
                Log.d(TAG, "Offer token extracted for base plan: $BASE_PLAN_ID")
            } else {
                if (offers.isNotEmpty()) {
                    offerToken = offers[0].offerToken
                    Log.d(TAG, "Using first available offer token")
                } else {
                    Log.e(TAG, "No subscription offers available")
                }
            }
        } ?: run {
            Log.e(TAG, "No subscription offer details found")
        }
    }

    fun launchBillingFlow(activity: Activity): Boolean {
        val details = productDetails
        val token = offerToken

        if (details == null || token == null) {
            Log.e(TAG, "Cannot launch billing flow: product details or offer token missing")
            _subscriptionState.value = SubscriptionState.Error(
                "Subscription not available"
            )
            return false
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(token)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val billingResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
        
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "Billing flow launched successfully")
            return true
        } else {
            Log.e(TAG, "Failed to launch billing flow: ${billingResult?.debugMessage}")
            _subscriptionState.value = SubscriptionState.Error(
                "Failed to start purchase flow"
            )
            return false
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            Log.d(TAG, "Purchases updated: ${purchases.size} purchase(s)")
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled purchase")
            _subscriptionState.value = SubscriptionState.NotSubscribed
        } else {
            Log.e(TAG, "Purchase update failed: ${billingResult.debugMessage}")
            _subscriptionState.value = SubscriptionState.Error(
                "Purchase failed: ${billingResult.debugMessage}"
            )
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        scope.launch {
            try {
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PURCHASED -> {
                        Log.d(TAG, "Purchase state: PURCHASED")
                        
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        }
                        
                        saveEntitlement(purchase)
                        
                        val expiryTime = getExpiryTimeFromPurchase(purchase)
                        _subscriptionState.value = SubscriptionState.Subscribed(expiryTime)
                    }
                    
                    Purchase.PurchaseState.PENDING -> {
                        Log.d(TAG, "Purchase state: PENDING")
                        _subscriptionState.value = SubscriptionState.Pending
                        
                        saveEntitlement(purchase, isPending = true)
                    }
                    
                    else -> {
                        Log.d(TAG, "Purchase state: UNSPECIFIED_STATE")
                        _subscriptionState.value = SubscriptionState.NotSubscribed
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling purchase", e)
                _subscriptionState.value = SubscriptionState.Error(
                    "Error processing purchase: ${e.message}"
                )
            }
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) {
        withContext(Dispatchers.IO) {
            try {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                val result = billingClient?.acknowledgePurchase(acknowledgePurchaseParams)
                
                if (result?.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Purchase acknowledged successfully")
                } else {
                    Log.e(TAG, "Failed to acknowledge purchase: ${result?.debugMessage}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error acknowledging purchase", e)
            }
        }
    }

    private suspend fun saveEntitlement(purchase: Purchase, isPending: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                val expiryTime = getExpiryTimeFromPurchase(purchase)
                
                val entitlement = EntitlementEntity(
                    id = 1,
                    isProActive = !isPending,
                    purchaseToken = purchase.purchaseToken,
                    expiryTime = expiryTime,
                    lastVerified = System.currentTimeMillis()
                )

                entitlementDao.insert(entitlement)
                Log.d(TAG, "Entitlement saved: isProActive=${entitlement.isProActive}, expiry=$expiryTime")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving entitlement", e)
            }
        }
    }

    private fun getExpiryTimeFromPurchase(purchase: Purchase): Long? {
        return try {
            val productId = purchase.products.firstOrNull()
            if (productId == PRODUCT_ID) {
                purchase.purchaseTime + (365L * 24 * 60 * 60 * 1000)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating expiry time", e)
            null
        }
    }

    fun queryPurchases() {
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val params = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()

                    billingClient?.queryPurchasesAsync(params) { billingResult, purchases ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d(TAG, "Query purchases successful: ${purchases.size} purchase(s)")
                            
                            if (purchases.isEmpty()) {
                                scope.launch {
                                    clearEntitlement()
                                    _subscriptionState.value = SubscriptionState.NotSubscribed
                                }
                            } else {
                                val activePurchase = purchases.firstOrNull { purchase ->
                                    purchase.products.contains(PRODUCT_ID) &&
                                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                                }

                                if (activePurchase != null) {
                                    handlePurchase(activePurchase)
                                } else {
                                    val pendingPurchase = purchases.firstOrNull { purchase ->
                                        purchase.products.contains(PRODUCT_ID) &&
                                        purchase.purchaseState == Purchase.PurchaseState.PENDING
                                    }

                                    if (pendingPurchase != null) {
                                        handlePurchase(pendingPurchase)
                                    } else {
                                        scope.launch {
                                            clearEntitlement()
                                            _subscriptionState.value = SubscriptionState.NotSubscribed
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.e(TAG, "Failed to query purchases: ${billingResult.debugMessage}")
                            _subscriptionState.value = SubscriptionState.Error(
                                "Failed to verify subscription"
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error querying purchases", e)
                    _subscriptionState.value = SubscriptionState.Error(
                        "Error checking subscription: ${e.message}"
                    )
                }
            }
        }
    }

    fun restorePurchases() {
        Log.d(TAG, "Restoring purchases...")
        _subscriptionState.value = SubscriptionState.Loading
        queryPurchases()
    }

    private suspend fun clearEntitlement() {
        withContext(Dispatchers.IO) {
            try {
                val entitlement = EntitlementEntity(
                    id = 1,
                    isProActive = false,
                    purchaseToken = null,
                    expiryTime = null,
                    lastVerified = System.currentTimeMillis()
                )
                entitlementDao.insert(entitlement)
                Log.d(TAG, "Entitlement cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing entitlement", e)
            }
        }
    }

    suspend fun checkSubscriptionStatus(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val entitlement = entitlementDao.getOnce()
                
                if (entitlement?.isProActive == true) {
                    val expiryTime = entitlement.expiryTime
                    if (expiryTime != null && expiryTime > System.currentTimeMillis()) {
                        Log.d(TAG, "Valid subscription found in cache")
                        
                        val lastVerified = entitlement.lastVerified
                        val daysSinceVerification = (System.currentTimeMillis() - lastVerified) / (24 * 60 * 60 * 1000)
                        
                        if (daysSinceVerification > 1) {
                            Log.d(TAG, "Subscription needs re-verification with Google")
                            queryPurchases()
                        }
                        
                        return@withContext true
                    } else {
                        Log.d(TAG, "Subscription expired")
                        clearEntitlement()
                        return@withContext false
                    }
                } else {
                    Log.d(TAG, "No active subscription")
                    return@withContext false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking subscription status", e)
                false
            }
        }
    }

    fun onResume() {
        Log.d(TAG, "onResume: Checking purchases on background return")
        queryPurchases()
    }

    fun endConnection() {
        try {
            billingClient?.endConnection()
            Log.d(TAG, "Billing client connection ended")
        } catch (e: Exception) {
            Log.e(TAG, "Error ending billing connection", e)
        }
    }

    fun getProductPrice(): String? {
        return try {
            productDetails?.subscriptionOfferDetails?.find { 
                it.basePlanId == BASE_PLAN_ID 
            }?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product price", e)
            null
        }
    }
}
