package com.zaheer.upireconcilepro.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.upireconcilepro.data.database.entity.UnmatchedItemEntity
import com.zaheer.upireconcilepro.data.repository.EntitlementRepository
import com.zaheer.upireconcilepro.data.repository.ReconciliationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class UnmatchedViewModel(
    application: Application,
    private val reconciliationRepository: ReconciliationRepository,
    private val entitlementRepository: EntitlementRepository
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "UnmatchedViewModel"
    }

    sealed class UnmatchedUiState {
        object Loading : UnmatchedUiState()
        data class Success(
            val invoices: List<UnmatchedItemEntity>,
            val payments: List<UnmatchedItemEntity>,
            val duplicates: List<UnmatchedItemEntity>,
            val selectedTab: TabType,
            val isPro: Boolean
        ) : UnmatchedUiState()
        data class ProRequired(val message: String) : UnmatchedUiState()
        data class Empty(val selectedTab: TabType) : UnmatchedUiState()
        data class Error(val message: String) : UnmatchedUiState()
    }

    enum class TabType {
        INVOICES, PAYMENTS, DUPLICATES
    }

    private val _uiState = MutableStateFlow<UnmatchedUiState>(UnmatchedUiState.Loading)
    val uiState: StateFlow<UnmatchedUiState> = _uiState.asStateFlow()

    private var currentSessionId: Long = -1
    private var currentTab: TabType = TabType.INVOICES

    fun loadUnmatchedItems(sessionId: Long) {
        if (currentSessionId == sessionId && _uiState.value is UnmatchedUiState.Success) {
            return
        }

        currentSessionId = sessionId
        _uiState.value = UnmatchedUiState.Loading

        viewModelScope.launch {
            try {
                val isPro = entitlementRepository.isProActive()
                
                if (!isPro) {
                    Log.d(TAG, "Unmatched items view is PRO only")
                    _uiState.value = UnmatchedUiState.ProRequired(
                        "Unmatched items view is a PRO feature. Upgrade to access detailed unmatched analysis."
                    )
                    return@launch
                }

                loadItemsForSession(sessionId)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking PRO status", e)
                _uiState.value = UnmatchedUiState.Error("Failed to verify PRO status: ${e.message}")
            }
        }
    }

    private fun loadItemsForSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                combine(
                    reconciliationRepository.getUnmatchedItemsByType(sessionId, "INVOICE"),
                    reconciliationRepository.getUnmatchedItemsByType(sessionId, "PAYMENT"),
                    reconciliationRepository.getUnmatchedItemsByType(sessionId, "DUPLICATE"),
                    entitlementRepository.getProStatusFlow()
                ) { invoices, payments, duplicates, isPro ->
                    if (!isPro) {
                        UnmatchedUiState.ProRequired(
                            "Your PRO subscription has expired. Please renew to access this feature."
                        )
                    } else if (invoices.isEmpty() && payments.isEmpty() && duplicates.isEmpty()) {
                        UnmatchedUiState.Empty(currentTab)
                    } else {
                        UnmatchedUiState.Success(
                            invoices = invoices,
                            payments = payments,
                            duplicates = duplicates,
                            selectedTab = currentTab,
                            isPro = isPro
                        )
                    }
                }.catch { e ->
                    Log.e(TAG, "Error loading unmatched items", e)
                    emit(UnmatchedUiState.Error("Failed to load unmatched items: ${e.message}"))
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadItemsForSession", e)
                _uiState.value = UnmatchedUiState.Error("Failed to load items: ${e.message}")
            }
        }
    }

    fun selectTab(tab: TabType) {
        currentTab = tab
        val currentState = _uiState.value
        
        if (currentState is UnmatchedUiState.Success) {
            _uiState.value = currentState.copy(selectedTab = tab)
        } else if (currentState is UnmatchedUiState.Empty) {
            _uiState.value = UnmatchedUiState.Empty(tab)
        }
    }

    fun getCurrentTabItems(): List<UnmatchedItemEntity> {
        val currentState = _uiState.value
        
        return if (currentState is UnmatchedUiState.Success) {
            when (currentTab) {
                TabType.INVOICES -> currentState.invoices
                TabType.PAYMENTS -> currentState.payments
                TabType.DUPLICATES -> currentState.duplicates
            }
        } else {
            emptyList()
        }
    }

    fun filterByAmount(min: Double, max: Double) {
        val currentState = _uiState.value
        
        if (currentState is UnmatchedUiState.Success) {
            val filteredInvoices = currentState.invoices.filter { it.amount in min..max }
            val filteredPayments = currentState.payments.filter { it.amount in min..max }
            val filteredDuplicates = currentState.duplicates.filter { it.amount in min..max }

            _uiState.value = currentState.copy(
                invoices = filteredInvoices,
                payments = filteredPayments,
                duplicates = filteredDuplicates
            )
        }
    }

    fun searchItems(query: String) {
        if (query.isBlank()) {
            refresh()
            return
        }

        val currentState = _uiState.value
        
        if (currentState is UnmatchedUiState.Success) {
            val filteredInvoices = currentState.invoices.filter { item ->
                item.reference.contains(query, ignoreCase = true) ||
                item.merchant?.contains(query, ignoreCase = true) == true
            }
            
            val filteredPayments = currentState.payments.filter { item ->
                item.reference.contains(query, ignoreCase = true) ||
                item.merchant?.contains(query, ignoreCase = true) == true
            }
            
            val filteredDuplicates = currentState.duplicates.filter { item ->
                item.reference.contains(query, ignoreCase = true) ||
                item.merchant?.contains(query, ignoreCase = true) == true
            }

            _uiState.value = currentState.copy(
                invoices = filteredInvoices,
                payments = filteredPayments,
                duplicates = filteredDuplicates
            )
        }
    }

    fun exportToCSV(): Boolean {
        val currentState = _uiState.value
        
        if (currentState is UnmatchedUiState.Success) {
            val items = getCurrentTabItems()
            Log.d(TAG, "Exporting ${items.size} unmatched ${currentTab.name} to CSV")
            return true
        }

        return false
    }

    fun refresh() {
        if (currentSessionId != -1L) {
            loadUnmatchedItems(currentSessionId)
        }
    }
}
