package com.zaheer.upireconcilepro.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.upireconcilepro.billing.BillingManager
import com.zaheer.upireconcilepro.data.repository.EntitlementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PaywallViewModel(
    application: Application,
    private val billingManager: BillingManager,
    private val entitlementRepository: EntitlementRepository
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "PaywallViewModel"
    }

    sealed class PaywallUiState {
        object Loading : PaywallUiState()
        data class Available(
            val price: String,
            val features: List<ProFeature>,
            val isPurchasing: Boolean = false
        ) : PaywallUiState()
        data class Subscribed(
            val expiryTime: Long?,
            val remainingDays: Long?
        ) : PaywallUiState()
        data class Error(val message: String) : PaywallUiState()
        data class PurchaseSuccess(val message: String) : PaywallUiState()
    }

    data class ProFeature(
        val title: String,
        val description: String,
        val icon: String
    )

    private val _uiState = MutableStateFlow<PaywallUiState>(PaywallUiState.Loading)
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        loadPaywallData()
        observeBillingState()
    }

    private fun loadPaywallData() {
        viewModelScope.launch {
            try {
                val isPro = entitlementRepository.isProActive()
                
                if (isPro) {
                    val remainingDays = entitlementRepository.getRemainingDays()
                    val status = entitlementRepository.getEntitlementStatus()
                    
                    val expiryTime = when (status) {
                        is EntitlementRepository.EntitlementStatus.Pro -> {
                            remainingDays?.let { System.currentTimeMillis() + (it * 24 * 60 * 60 * 1000) }
                        }
                        else -> null
                    }
                    
                    _uiState.value = PaywallUiState.Subscribed(
                        expiryTime = expiryTime,
                        remainingDays = remainingDays
                    )
                    
                    Log.d(TAG, "User is already PRO, remaining days: $remainingDays")
                } else {
                    val price = billingManager.getProductPrice() ?: "Loading..."
                    val features = getProFeatures()
                    
                    _uiState.value = PaywallUiState.Available(
                        price = price,
                        features = features
                    )
                    
                    Log.d(TAG, "Paywall loaded with price: $price")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading paywall data", e)
                _uiState.value = PaywallUiState.Error("Failed to load subscription info: ${e.message}")
            }
        }
    }

    private fun observeBillingState() {
        viewModelScope.launch {
            combine(
                billingManager.subscriptionState,
                entitlementRepository.getProStatusFlow()
            ) { billingState, isPro ->
                when (billingState) {
                    is BillingManager.SubscriptionState.Loading -> {
                        _uiState.value = PaywallUiState.Loading
                    }
                    
                    is BillingManager.SubscriptionState.Subscribed -> {
                        val remainingDays = entitlementRepository.getRemainingDays()
                        _uiState.value = PaywallUiState.Subscribed(
                            expiryTime = billingState.expiryTime,
                            remainingDays = remainingDays
                        )
                        Log.d(TAG, "Subscription active")
                    }
                    
                    is BillingManager.SubscriptionState.NotSubscribed -> {
                        if (!isPro) {
                            val price = billingManager.getProductPrice() ?: "Loading..."
                            val features = getProFeatures()
                            _uiState.value = PaywallUiState.Available(
                                price = price,
                                features = features
                            )
                        }
                    }
                    
                    is BillingManager.SubscriptionState.Pending -> {
                        val currentState = _uiState.value
                        if (currentState is PaywallUiState.Available) {
                            _uiState.value = currentState.copy(isPurchasing = true)
                        }
                        Log.d(TAG, "Purchase pending")
                    }
                    
                    is BillingManager.SubscriptionState.Error -> {
                        _uiState.value = PaywallUiState.Error(billingState.message)
                        Log.e(TAG, "Billing error: ${billingState.message}")
                    }
                }
            }.collect { }
        }
    }

    fun purchaseSubscription(activity: Activity) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                
                if (currentState is PaywallUiState.Available) {
                    _uiState.value = currentState.copy(isPurchasing = true)
                }

                Log.d(TAG, "Initiating purchase flow")
                val success = billingManager.launchBillingFlow(activity)
                
                if (!success) {
                    val price = billingManager.getProductPrice() ?: "Loading..."
                    val features = getProFeatures()
                    _uiState.value = PaywallUiState.Available(
                        price = price,
                        features = features,
                        isPurchasing = false
                    )
                    
                    Log.e(TAG, "Failed to launch billing flow")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initiating purchase", e)
                _uiState.value = PaywallUiState.Error("Failed to start purchase: ${e.message}")
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            try {
                _uiState.value = PaywallUiState.Loading
                Log.d(TAG, "Restoring purchases")
                
                billingManager.restorePurchases()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring purchases", e)
                _uiState.value = PaywallUiState.Error("Failed to restore purchases: ${e.message}")
            }
        }
    }

    fun checkSubscriptionStatus() {
        viewModelScope.launch {
            try {
                val isActive = billingManager.checkSubscriptionStatus()
                
                if (isActive) {
                    val remainingDays = entitlementRepository.getRemainingDays()
                    val expiryTime = remainingDays?.let { 
                        System.currentTimeMillis() + (it * 24 * 60 * 60 * 1000) 
                    }
                    
                    _uiState.value = PaywallUiState.Subscribed(
                        expiryTime = expiryTime,
                        remainingDays = remainingDays
                    )
                    
                    Log.d(TAG, "Subscription is active")
                } else {
                    val price = billingManager.getProductPrice() ?: "Loading..."
                    val features = getProFeatures()
                    
                    _uiState.value = PaywallUiState.Available(
                        price = price,
                        features = features
                    )
                    
                    Log.d(TAG, "No active subscription")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking subscription status", e)
                _uiState.value = PaywallUiState.Error("Failed to check status: ${e.message}")
            }
        }
    }

    private fun getProFeatures(): List<ProFeature> {
        return listOf(
            ProFeature(
                title = "Unlimited Sessions",
                description = "Create unlimited reconciliation sessions",
                icon = "∞"
            ),
            ProFeature(
                title = "Unlimited Matched Items",
                description = "View all matched items without 20-item limit",
                icon = "✓"
            ),
            ProFeature(
                title = "Unmatched Analysis",
                description = "Access detailed unmatched invoices, payments, and duplicates",
                icon = "📊"
            ),
            ProFeature(
                title = "Advanced Export",
                description = "Export to Excel, PDF, and custom formats",
                icon = "📤"
            ),
            ProFeature(
                title = "Auto Backup",
                description = "Automatic cloud backup of all your data",
                icon = "☁️"
            ),
            ProFeature(
                title = "Priority Support",
                description = "Get faster response times for your queries",
                icon = "⚡"
            ),
            ProFeature(
                title = "Advanced Analytics",
                description = "Comprehensive insights and reporting",
                icon = "📈"
            ),
            ProFeature(
                title = "Custom Date Tolerance",
                description = "Fine-tune matching with custom date ranges",
                icon = "📅"
            )
        )
    }

    fun dismissError() {
        val currentState = _uiState.value
        if (currentState is PaywallUiState.Error) {
            loadPaywallData()
        }
    }

    fun refresh() {
        _uiState.value = PaywallUiState.Loading
        loadPaywallData()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "PaywallViewModel cleared")
    }
}
