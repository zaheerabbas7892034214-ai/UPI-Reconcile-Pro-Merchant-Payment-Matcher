package com.zaheer.upireconcilepro.data.repository

import android.util.Log
import com.zaheer.upireconcilepro.data.database.dao.EntitlementDao
import com.zaheer.upireconcilepro.data.database.entity.EntitlementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class EntitlementRepository(
    private val entitlementDao: EntitlementDao
) {
    companion object {
        private const val TAG = "EntitlementRepository"
        private const val MAX_FREE_SESSIONS = 3
        private const val EXPIRY_GRACE_PERIOD_MILLIS = 3 * 24 * 60 * 60 * 1000L
    }

    sealed class EntitlementStatus {
        object Free : EntitlementStatus()
        object Pro : EntitlementStatus()
        data class Expired(val expiredDaysAgo: Long) : EntitlementStatus()
        object GracePeriod : EntitlementStatus()
    }

    sealed class ProFeatureAccess {
        object Allowed : ProFeatureAccess()
        data class Denied(val reason: String) : ProFeatureAccess()
    }

    fun getEntitlementFlow(): Flow<EntitlementEntity?> {
        return entitlementDao.get()
    }

    fun getProStatusFlow(): Flow<Boolean> {
        return entitlementDao.get().map { entitlement ->
            entitlement?.isProActive == true && isEntitlementValid(entitlement)
        }
    }

    suspend fun isProActive(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val entitlement = entitlementDao.getOnce()
                
                if (entitlement?.isProActive == true) {
                    val isValid = isEntitlementValid(entitlement)
                    Log.d(TAG, "Pro status check: isProActive=${entitlement.isProActive}, isValid=$isValid")
                    isValid
                } else {
                    Log.d(TAG, "Pro status check: not active")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking pro status", e)
                false
            }
        }
    }

    suspend fun checkProFeatureAccess(featureName: String): ProFeatureAccess {
        return withContext(Dispatchers.IO) {
            try {
                val isPro = isProActive()
                
                if (isPro) {
                    Log.d(TAG, "Pro feature '$featureName' access: ALLOWED")
                    ProFeatureAccess.Allowed
                } else {
                    Log.d(TAG, "Pro feature '$featureName' access: DENIED (not subscribed)")
                    ProFeatureAccess.Denied("This feature requires UPI Reconcile Pro subscription")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking feature access for '$featureName'", e)
                ProFeatureAccess.Denied("Unable to verify subscription status")
            }
        }
    }

    suspend fun checkSessionLimit(currentSessionCount: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val isPro = isProActive()
                
                if (isPro) {
                    Log.d(TAG, "Session limit check: UNLIMITED (Pro user)")
                    true
                } else {
                    val canCreate = currentSessionCount < MAX_FREE_SESSIONS
                    Log.d(TAG, "Session limit check: $currentSessionCount/$MAX_FREE_SESSIONS, allowed=$canCreate")
                    canCreate
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking session limit", e)
                currentSessionCount < MAX_FREE_SESSIONS
            }
        }
    }

    suspend fun getEntitlementStatus(): EntitlementStatus {
        return withContext(Dispatchers.IO) {
            try {
                val entitlement = entitlementDao.getOnce()
                
                if (entitlement?.isProActive == true) {
                    val expiryTime = entitlement.expiryTime
                    
                    if (expiryTime == null) {
                        Log.d(TAG, "Entitlement status: PRO (no expiry)")
                        return@withContext EntitlementStatus.Pro
                    }
                    
                    val currentTime = System.currentTimeMillis()
                    
                    when {
                        expiryTime > currentTime -> {
                            Log.d(TAG, "Entitlement status: PRO (valid)")
                            EntitlementStatus.Pro
                        }
                        
                        (currentTime - expiryTime) < EXPIRY_GRACE_PERIOD_MILLIS -> {
                            Log.d(TAG, "Entitlement status: GRACE PERIOD")
                            EntitlementStatus.GracePeriod
                        }
                        
                        else -> {
                            val daysExpired = (currentTime - expiryTime) / (24 * 60 * 60 * 1000L)
                            Log.d(TAG, "Entitlement status: EXPIRED ($daysExpired days ago)")
                            EntitlementStatus.Expired(daysExpired)
                        }
                    }
                } else {
                    Log.d(TAG, "Entitlement status: FREE")
                    EntitlementStatus.Free
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting entitlement status", e)
                EntitlementStatus.Free
            }
        }
    }

    suspend fun getRemainingDays(): Long? {
        return withContext(Dispatchers.IO) {
            try {
                val entitlement = entitlementDao.getOnce()
                
                if (entitlement?.isProActive == true) {
                    val expiryTime = entitlement.expiryTime ?: return@withContext null
                    val currentTime = System.currentTimeMillis()
                    val remainingMillis = expiryTime - currentTime
                    
                    if (remainingMillis > 0) {
                        val remainingDays = remainingMillis / (24 * 60 * 60 * 1000L)
                        Log.d(TAG, "Remaining subscription days: $remainingDays")
                        remainingDays
                    } else {
                        Log.d(TAG, "Subscription expired")
                        0L
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting remaining days", e)
                null
            }
        }
    }

    suspend fun updateEntitlement(entitlement: EntitlementEntity) {
        withContext(Dispatchers.IO) {
            try {
                entitlementDao.insert(entitlement)
                Log.d(TAG, "Entitlement updated: isProActive=${entitlement.isProActive}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating entitlement", e)
            }
        }
    }

    suspend fun clearEntitlement() {
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

    private fun isEntitlementValid(entitlement: EntitlementEntity): Boolean {
        if (!entitlement.isProActive) {
            return false
        }

        val expiryTime = entitlement.expiryTime ?: return true
        val currentTime = System.currentTimeMillis()
        
        return expiryTime > currentTime
    }

    suspend fun shouldShowGracePeriodWarning(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val status = getEntitlementStatus()
                val shouldShow = status is EntitlementStatus.GracePeriod
                Log.d(TAG, "Should show grace period warning: $shouldShow")
                shouldShow
            } catch (e: Exception) {
                Log.e(TAG, "Error checking grace period warning", e)
                false
            }
        }
    }

    fun getMaxFreeSessions(): Int = MAX_FREE_SESSIONS

    suspend fun getProFeatures(): List<ProFeature> {
        return listOf(
            ProFeature(
                id = "unlimited_sessions",
                name = "Unlimited Reconciliation Sessions",
                description = "Create unlimited reconciliation sessions (Free: $MAX_FREE_SESSIONS sessions)",
                isEnabled = isProActive()
            ),
            ProFeature(
                id = "advanced_export",
                name = "Advanced Export Options",
                description = "Export to Excel and PDF formats",
                isEnabled = isProActive()
            ),
            ProFeature(
                id = "priority_support",
                name = "Priority Support",
                description = "Get faster response times for support queries",
                isEnabled = isProActive()
            ),
            ProFeature(
                id = "detailed_analytics",
                name = "Detailed Analytics",
                description = "Access comprehensive reconciliation analytics and insights",
                isEnabled = isProActive()
            )
        )
    }

    data class ProFeature(
        val id: String,
        val name: String,
        val description: String,
        val isEnabled: Boolean
    )
}
