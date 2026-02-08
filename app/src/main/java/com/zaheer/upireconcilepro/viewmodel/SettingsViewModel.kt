package com.zaheer.upireconcilepro.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.upireconcilepro.data.repository.EntitlementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val entitlementRepository: EntitlementRepository
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SettingsViewModel"
        private const val PREFS_NAME = "app_settings"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_DATE_TOLERANCE_DAYS = "date_tolerance_days"
        private const val KEY_AUTO_BACKUP = "auto_backup"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val DEFAULT_DATE_TOLERANCE = 2
    }

    sealed class SettingsUiState {
        object Loading : SettingsUiState()
        data class Success(
            val isPro: Boolean,
            val remainingDays: Long?,
            val settings: AppSettings
        ) : SettingsUiState()
        data class Error(val message: String) : SettingsUiState()
    }

    data class AppSettings(
        val appLockEnabled: Boolean = false,
        val biometricEnabled: Boolean = false,
        val dateToleranceDays: Int = DEFAULT_DATE_TOLERANCE,
        val autoBackupEnabled: Boolean = false,
        val themeMode: ThemeMode = ThemeMode.SYSTEM
    )

    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val sharedPrefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val isPro = entitlementRepository.isProActive()
                val remainingDays = entitlementRepository.getRemainingDays()
                
                val settings = AppSettings(
                    appLockEnabled = sharedPrefs.getBoolean(KEY_APP_LOCK_ENABLED, false),
                    biometricEnabled = sharedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false),
                    dateToleranceDays = sharedPrefs.getInt(KEY_DATE_TOLERANCE_DAYS, DEFAULT_DATE_TOLERANCE),
                    autoBackupEnabled = sharedPrefs.getBoolean(KEY_AUTO_BACKUP, false),
                    themeMode = ThemeMode.valueOf(
                        sharedPrefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
                    )
                )

                _uiState.value = SettingsUiState.Success(
                    isPro = isPro,
                    remainingDays = remainingDays,
                    settings = settings
                )

                Log.d(TAG, "Settings loaded: isPro=$isPro, dateTolerance=${settings.dateToleranceDays}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading settings", e)
                _uiState.value = SettingsUiState.Error("Failed to load settings: ${e.message}")
            }
        }
    }

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                sharedPrefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
                updateSettingsState { it.copy(appLockEnabled = enabled) }
                Log.d(TAG, "App lock enabled: $enabled")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting app lock", e)
                _uiState.value = SettingsUiState.Error("Failed to update setting: ${e.message}")
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                sharedPrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
                updateSettingsState { it.copy(biometricEnabled = enabled) }
                Log.d(TAG, "Biometric enabled: $enabled")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting biometric", e)
                _uiState.value = SettingsUiState.Error("Failed to update setting: ${e.message}")
            }
        }
    }

    fun setDateToleranceDays(days: Int) {
        viewModelScope.launch {
            try {
                if (days < 0 || days > 30) {
                    _uiState.value = SettingsUiState.Error("Date tolerance must be between 0 and 30 days")
                    return@launch
                }

                sharedPrefs.edit().putInt(KEY_DATE_TOLERANCE_DAYS, days).apply()
                updateSettingsState { it.copy(dateToleranceDays = days) }
                Log.d(TAG, "Date tolerance set to: $days days")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting date tolerance", e)
                _uiState.value = SettingsUiState.Error("Failed to update setting: ${e.message}")
            }
        }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val isPro = entitlementRepository.isProActive()
                
                if (enabled && !isPro) {
                    _uiState.value = SettingsUiState.Error("Auto backup is a PRO feature")
                    return@launch
                }

                sharedPrefs.edit().putBoolean(KEY_AUTO_BACKUP, enabled).apply()
                updateSettingsState { it.copy(autoBackupEnabled = enabled) }
                Log.d(TAG, "Auto backup enabled: $enabled")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting auto backup", e)
                _uiState.value = SettingsUiState.Error("Failed to update setting: ${e.message}")
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                sharedPrefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
                updateSettingsState { it.copy(themeMode = mode) }
                Log.d(TAG, "Theme mode set to: $mode")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting theme mode", e)
                _uiState.value = SettingsUiState.Error("Failed to update setting: ${e.message}")
            }
        }
    }

    private fun updateSettingsState(update: (AppSettings) -> AppSettings) {
        val currentState = _uiState.value
        if (currentState is SettingsUiState.Success) {
            _uiState.value = currentState.copy(settings = update(currentState.settings))
        }
    }

    fun getDateToleranceDays(): Int {
        return sharedPrefs.getInt(KEY_DATE_TOLERANCE_DAYS, DEFAULT_DATE_TOLERANCE)
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Clearing all app data")
                sharedPrefs.edit().clear().apply()
                loadSettings()
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing data", e)
                _uiState.value = SettingsUiState.Error("Failed to clear data: ${e.message}")
            }
        }
    }

    fun exportSettings(): Map<String, Any> {
        return mapOf(
            "appLockEnabled" to sharedPrefs.getBoolean(KEY_APP_LOCK_ENABLED, false),
            "biometricEnabled" to sharedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false),
            "dateToleranceDays" to sharedPrefs.getInt(KEY_DATE_TOLERANCE_DAYS, DEFAULT_DATE_TOLERANCE),
            "autoBackupEnabled" to sharedPrefs.getBoolean(KEY_AUTO_BACKUP, false),
            "themeMode" to sharedPrefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)!!
        )
    }

    fun importSettings(settings: Map<String, Any>) {
        viewModelScope.launch {
            try {
                sharedPrefs.edit().apply {
                    settings.forEach { (key, value) ->
                        when (value) {
                            is Boolean -> putBoolean(key, value)
                            is Int -> putInt(key, value)
                            is String -> putString(key, value)
                        }
                    }
                }.apply()
                
                loadSettings()
                Log.d(TAG, "Settings imported successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error importing settings", e)
                _uiState.value = SettingsUiState.Error("Failed to import settings: ${e.message}")
            }
        }
    }

    fun refresh() {
        _uiState.value = SettingsUiState.Loading
        loadSettings()
    }
}
