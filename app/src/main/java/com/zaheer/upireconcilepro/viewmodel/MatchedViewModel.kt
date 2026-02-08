package com.zaheer.upireconcilepro.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.upireconcilepro.data.database.entity.MatchedItemEntity
import com.zaheer.upireconcilepro.data.repository.EntitlementRepository
import com.zaheer.upireconcilepro.data.repository.ReconciliationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MatchedViewModel(
    application: Application,
    private val reconciliationRepository: ReconciliationRepository,
    private val entitlementRepository: EntitlementRepository
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MatchedViewModel"
        private const val FREE_ITEM_LIMIT = 20
    }

    sealed class MatchedUiState {
        object Loading : MatchedUiState()
        data class Success(
            val items: List<MatchedItemEntity>,
            val displayItems: List<MatchedItemEntity>,
            val isPro: Boolean,
            val isLimited: Boolean,
            val totalCount: Int,
            val limitedCount: Int?
        ) : MatchedUiState()
        data class Empty(val isPro: Boolean) : MatchedUiState()
        data class Error(val message: String) : MatchedUiState()
    }

    sealed class FilterOption {
        object All : FilterOption()
        data class AmountRange(val min: Double, val max: Double) : FilterOption()
        data class DateRange(val start: Long, val end: Long) : FilterOption()
        data class SearchQuery(val query: String) : FilterOption()
    }

    private val _uiState = MutableStateFlow<MatchedUiState>(MatchedUiState.Loading)
    val uiState: StateFlow<MatchedUiState> = _uiState.asStateFlow()

    private val _showUpgradePrompt = MutableStateFlow(false)
    val showUpgradePrompt: StateFlow<Boolean> = _showUpgradePrompt.asStateFlow()

    private var currentFilter: FilterOption = FilterOption.All
    private var currentSessionId: Long = -1

    fun loadMatchedItems(sessionId: Long) {
        if (currentSessionId == sessionId && _uiState.value is MatchedUiState.Success) {
            return
        }

        currentSessionId = sessionId
        _uiState.value = MatchedUiState.Loading

        viewModelScope.launch {
            try {
                combine(
                    reconciliationRepository.getMatchedItems(sessionId),
                    entitlementRepository.getProStatusFlow()
                ) { items, isPro ->
                    if (items.isEmpty()) {
                        MatchedUiState.Empty(isPro)
                    } else {
                        val filteredItems = applyFilter(items, currentFilter)
                        val isLimited = !isPro && filteredItems.size > FREE_ITEM_LIMIT
                        val displayItems = if (isLimited) {
                            filteredItems.take(FREE_ITEM_LIMIT)
                        } else {
                            filteredItems
                        }

                        if (isLimited && !_showUpgradePrompt.value) {
                            _showUpgradePrompt.value = true
                        }

                        MatchedUiState.Success(
                            items = items,
                            displayItems = displayItems,
                            isPro = isPro,
                            isLimited = isLimited,
                            totalCount = filteredItems.size,
                            limitedCount = if (isLimited) FREE_ITEM_LIMIT else null
                        )
                    }
                }.catch { e ->
                    Log.e(TAG, "Error loading matched items", e)
                    emit(MatchedUiState.Error("Failed to load matched items: ${e.message}"))
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadMatchedItems", e)
                _uiState.value = MatchedUiState.Error("Failed to load items: ${e.message}")
            }
        }
    }

    fun applyFilter(filter: FilterOption) {
        currentFilter = filter
        val currentState = _uiState.value
        
        if (currentState is MatchedUiState.Success) {
            val filteredItems = applyFilter(currentState.items, filter)
            val isLimited = !currentState.isPro && filteredItems.size > FREE_ITEM_LIMIT
            val displayItems = if (isLimited) {
                filteredItems.take(FREE_ITEM_LIMIT)
            } else {
                filteredItems
            }

            _uiState.value = currentState.copy(
                displayItems = displayItems,
                isLimited = isLimited,
                totalCount = filteredItems.size
            )
        }
    }

    private fun applyFilter(items: List<MatchedItemEntity>, filter: FilterOption): List<MatchedItemEntity> {
        return when (filter) {
            is FilterOption.All -> items
            is FilterOption.AmountRange -> {
                items.filter { it.amount in filter.min..filter.max }
            }
            is FilterOption.DateRange -> {
                items.filter { it.date in filter.start..filter.end }
            }
            is FilterOption.SearchQuery -> {
                items.filter { item ->
                    item.reference.contains(filter.query, ignoreCase = true) ||
                    item.merchant?.contains(filter.query, ignoreCase = true) == true
                }
            }
        }
    }

    fun sortByAmount(ascending: Boolean = true) {
        val currentState = _uiState.value
        
        if (currentState is MatchedUiState.Success) {
            val sortedItems = if (ascending) {
                currentState.displayItems.sortedBy { it.amount }
            } else {
                currentState.displayItems.sortedByDescending { it.amount }
            }

            _uiState.value = currentState.copy(displayItems = sortedItems)
        }
    }

    fun sortByDate(ascending: Boolean = true) {
        val currentState = _uiState.value
        
        if (currentState is MatchedUiState.Success) {
            val sortedItems = if (ascending) {
                currentState.displayItems.sortedBy { it.date }
            } else {
                currentState.displayItems.sortedByDescending { it.date }
            }

            _uiState.value = currentState.copy(displayItems = sortedItems)
        }
    }

    fun dismissUpgradePrompt() {
        _showUpgradePrompt.value = false
    }

    fun showUpgradePrompt() {
        _showUpgradePrompt.value = true
    }

    fun exportToCSV(): Boolean {
        val currentState = _uiState.value
        
        if (currentState is MatchedUiState.Success) {
            if (!currentState.isPro) {
                Log.d(TAG, "Export requires PRO subscription")
                _showUpgradePrompt.value = true
                return false
            }

            Log.d(TAG, "Exporting ${currentState.displayItems.size} matched items to CSV")
            return true
        }

        return false
    }

    fun refresh() {
        if (currentSessionId != -1L) {
            loadMatchedItems(currentSessionId)
        }
    }
}
