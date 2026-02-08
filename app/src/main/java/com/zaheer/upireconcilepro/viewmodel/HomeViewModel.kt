package com.zaheer.upireconcilepro.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.upireconcilepro.data.database.entity.ReconciliationSessionEntity
import com.zaheer.upireconcilepro.data.repository.EntitlementRepository
import com.zaheer.upireconcilepro.data.repository.ReconciliationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    application: Application,
    private val reconciliationRepository: ReconciliationRepository,
    private val entitlementRepository: EntitlementRepository
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    sealed class HomeUiState {
        object Loading : HomeUiState()
        data class Success(
            val sessions: List<ReconciliationSessionEntity>,
            val isPro: Boolean,
            val canCreateSession: Boolean,
            val remainingSessions: Int?
        ) : HomeUiState()
        data class Error(val message: String) : HomeUiState()
    }

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                combine(
                    reconciliationRepository.getAllSessions(),
                    entitlementRepository.getProStatusFlow()
                ) { sessions, isPro ->
                    val sortedSessions = sessions.sortedByDescending { it.createdAt }
                    val canCreate = if (isPro) {
                        true
                    } else {
                        sessions.size < entitlementRepository.getMaxFreeSessions()
                    }
                    val remaining = if (!isPro) {
                        entitlementRepository.getMaxFreeSessions() - sessions.size
                    } else null

                    HomeUiState.Success(
                        sessions = sortedSessions,
                        isPro = isPro,
                        canCreateSession = canCreate,
                        remainingSessions = remaining
                    )
                }.catch { e ->
                    Log.e(TAG, "Error loading home data", e)
                    emit(HomeUiState.Error("Failed to load sessions: ${e.message}"))
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadHomeData", e)
                _uiState.value = HomeUiState.Error("Failed to load data: ${e.message}")
            }
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                when (val result = reconciliationRepository.deleteSession(sessionId)) {
                    is ReconciliationRepository.Result.Success -> {
                        Log.d(TAG, "Session deleted successfully: $sessionId")
                    }
                    is ReconciliationRepository.Result.Error -> {
                        Log.e(TAG, "Failed to delete session: ${result.message}")
                        _uiState.value = HomeUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting session", e)
                _uiState.value = HomeUiState.Error("Failed to delete session: ${e.message}")
            }
        }
    }

    fun refresh() {
        _uiState.value = HomeUiState.Loading
        loadHomeData()
    }
}
