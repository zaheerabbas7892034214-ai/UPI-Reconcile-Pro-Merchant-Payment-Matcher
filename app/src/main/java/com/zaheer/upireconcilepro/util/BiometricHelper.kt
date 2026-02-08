package com.zaheer.upireconcilepro.util

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {
    
    sealed class BiometricResult {
        object Success : BiometricResult()
        data class Error(val message: String) : BiometricResult()
        object NotAvailable : BiometricResult()
    }
    
    fun isBiometricAvailable(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    fun getBiometricCapabilityStatus(activity: FragmentActivity): String {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Available"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric hardware"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric hardware unavailable"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometric enrolled"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "Security update required"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "Biometric unsupported"
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "Status unknown"
            else -> "Unknown status"
        }
    }
    
    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isBiometricAvailable(activity)) {
            onError("Biometric authentication is not available on this device")
            return
        }
        
        val executor = ContextCompat.getMainExecutor(activity)
        
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    val message = when (errorCode) {
                        BiometricPrompt.ERROR_HW_UNAVAILABLE -> "Biometric hardware is currently unavailable"
                        BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> "Unable to process biometric data"
                        BiometricPrompt.ERROR_TIMEOUT -> "Authentication timeout"
                        BiometricPrompt.ERROR_NO_SPACE -> "Not enough storage space"
                        BiometricPrompt.ERROR_CANCELED -> "Authentication canceled"
                        BiometricPrompt.ERROR_LOCKOUT -> "Too many attempts. Please try again later"
                        BiometricPrompt.ERROR_VENDOR -> "Vendor-specific error occurred"
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> "Biometric authentication is permanently locked"
                        BiometricPrompt.ERROR_USER_CANCELED -> "User canceled authentication"
                        BiometricPrompt.ERROR_NO_BIOMETRICS -> "No biometrics enrolled"
                        BiometricPrompt.ERROR_HW_NOT_PRESENT -> "No biometric hardware present"
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> "User pressed negative button"
                        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> "No device credential available"
                        else -> errString.toString()
                    }
                    onError(message)
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Don't call onError here, as the user can try again
                }
            }
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Verify your identity to continue")
            .setDescription("Use your fingerprint or face to authenticate")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setConfirmationRequired(true)
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    fun authenticateWithFallback(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricManager = BiometricManager.from(activity)
        val authenticators = when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricManager.Authenticators.BIOMETRIC_STRONG
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                } else {
                    onError("Biometric authentication is not available")
                    return
                }
            }
        }
        
        val executor = ContextCompat.getMainExecutor(activity)
        
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // User can try again
                }
            }
        )
        
        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authentication Required")
            .setSubtitle("Verify your identity")
            .setAllowedAuthenticators(authenticators)
            .setConfirmationRequired(true)
        
        if (authenticators == BiometricManager.Authenticators.BIOMETRIC_STRONG) {
            promptInfoBuilder.setNegativeButtonText("Cancel")
        }
        
        val promptInfo = promptInfoBuilder.build()
        biometricPrompt.authenticate(promptInfo)
    }
    
    fun canUseBiometric(activity: FragmentActivity): BiometricResult {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricResult.Success
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> 
                BiometricResult.Error("This device doesn't have biometric hardware")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> 
                BiometricResult.Error("Biometric hardware is currently unavailable")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> 
                BiometricResult.Error("No biometric credentials enrolled. Please set up fingerprint or face unlock in Settings")
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                BiometricResult.Error("Security update required for biometric authentication")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                BiometricResult.Error("Biometric authentication is not supported")
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                BiometricResult.Error("Biometric status is unknown")
            else -> BiometricResult.NotAvailable
        }
    }
    
    fun hasEnrolledBiometrics(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != 
               BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }
}
