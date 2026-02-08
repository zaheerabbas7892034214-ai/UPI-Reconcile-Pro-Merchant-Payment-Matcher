package com.zaheer.upireconcilepro.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.upireconcilepro.data.database.entity.MatchedItemEntity
import com.zaheer.upireconcilepro.data.database.entity.ReconciliationSessionEntity
import com.zaheer.upireconcilepro.data.database.entity.UnmatchedItemEntity
import com.zaheer.upireconcilepro.data.repository.EntitlementRepository
import com.zaheer.upireconcilepro.data.repository.ReconciliationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DashboardViewModel(
    application: Application,
    private val reconciliationRepository: ReconciliationRepository,
    private val entitlementRepository: EntitlementRepository
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "DashboardViewModel"
    }

    sealed class DashboardUiState {
        object Loading : DashboardUiState()
        data class Success(
            val session: ReconciliationSessionEntity,
            val metrics: ReconciliationMetrics,
            val isPro: Boolean
        ) : DashboardUiState()
        data class Error(val message: String) : DashboardUiState()
    }

    data class ReconciliationMetrics(
        val totalInvoices: Int,
        val totalPayments: Int,
        val matchedCount: Int,
        val unmatchedInvoices: Int,
        val unmatchedPayments: Int,
        val duplicates: Int,
        val matchRate: Float,
        val totalMatchedAmount: Double,
        val totalUnmatchedAmount: Double
    )

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var currentSessionId: Long = -1

    fun loadSession(sessionId: Long) {
        if (currentSessionId == sessionId && _uiState.value is DashboardUiState.Success) {
            return
        }

        currentSessionId = sessionId
        _uiState.value = DashboardUiState.Loading

        viewModelScope.launch {
            try {
                when (val sessionResult = reconciliationRepository.getSessionById(sessionId)) {
                    is ReconciliationRepository.Result.Success -> {
                        val session = sessionResult.data
                        loadMetrics(session)
                    }
                    is ReconciliationRepository.Result.Error -> {
                        Log.e(TAG, "Failed to load session: ${sessionResult.message}")
                        _uiState.value = DashboardUiState.Error(sessionResult.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading session", e)
                _uiState.value = DashboardUiState.Error("Failed to load session: ${e.message}")
            }
        }
    }

    private fun loadMetrics(session: ReconciliationSessionEntity) {
        viewModelScope.launch {
            try {
                combine(
                    reconciliationRepository.getMatchedItems(session.id),
                    reconciliationRepository.getUnmatchedItems(session.id),
                    entitlementRepository.getProStatusFlow()
                ) { matchedItems, unmatchedItems, isPro ->
                    val metrics = calculateMetrics(matchedItems, unmatchedItems, session)
                    
                    DashboardUiState.Success(
                        session = session,
                        metrics = metrics,
                        isPro = isPro
                    )
                }.catch { e ->
                    Log.e(TAG, "Error loading metrics", e)
                    emit(DashboardUiState.Error("Failed to load metrics: ${e.message}"))
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadMetrics", e)
                _uiState.value = DashboardUiState.Error("Failed to calculate metrics: ${e.message}")
            }
        }
    }

    private fun calculateMetrics(
        matchedItems: List<MatchedItemEntity>,
        unmatchedItems: List<UnmatchedItemEntity>,
        session: ReconciliationSessionEntity
    ): ReconciliationMetrics {
        val unmatchedInvoices = unmatchedItems.count { it.type == "INVOICE" }
        val unmatchedPayments = unmatchedItems.count { it.type == "PAYMENT" }
        val duplicates = unmatchedItems.count { it.type == "DUPLICATE" }

        val totalInvoices = matchedItems.size + unmatchedInvoices
        val totalPayments = matchedItems.size + unmatchedPayments

        val matchRate = if (totalInvoices > 0) {
            (matchedItems.size.toFloat() / totalInvoices.toFloat()) * 100f
        } else {
            0f
        }

        val totalMatchedAmount = matchedItems.sumOf { it.amount }
        val totalUnmatchedAmount = unmatchedItems.sumOf { it.amount }

        return ReconciliationMetrics(
            totalInvoices = totalInvoices,
            totalPayments = totalPayments,
            matchedCount = matchedItems.size,
            unmatchedInvoices = unmatchedInvoices,
            unmatchedPayments = unmatchedPayments,
            duplicates = duplicates,
            matchRate = matchRate,
            totalMatchedAmount = totalMatchedAmount,
            totalUnmatchedAmount = totalUnmatchedAmount
        )
    }

    fun navigateToMatched() {
        Log.d(TAG, "Navigating to matched items")
    }

    fun navigateToUnmatched() {
        viewModelScope.launch {
            try {
                val isPro = entitlementRepository.isProActive()
                if (!isPro) {
                    Log.d(TAG, "Unmatched view requires PRO")
                    _uiState.value = DashboardUiState.Error("Unmatched items view requires PRO subscription")
                } else {
                    Log.d(TAG, "Navigating to unmatched items")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking PRO status", e)
            }
        }
    }

    fun refresh() {
        if (currentSessionId != -1L) {
            loadSession(currentSessionId)
        }
    }
}
