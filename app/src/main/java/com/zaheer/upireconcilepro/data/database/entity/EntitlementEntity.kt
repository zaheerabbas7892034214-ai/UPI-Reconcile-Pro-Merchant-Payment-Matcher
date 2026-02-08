package com.zaheer.upireconcilepro.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entitlement")
data class EntitlementEntity(
    @PrimaryKey
    val id: Int = 1,
    val isProActive: Boolean,
    val purchaseToken: String?,
    val expiryTime: Long?,
    val lastVerified: Long
)
