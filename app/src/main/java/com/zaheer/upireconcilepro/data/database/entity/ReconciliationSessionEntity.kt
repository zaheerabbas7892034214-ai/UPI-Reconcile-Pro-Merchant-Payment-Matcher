package com.zaheer.upireconcilepro.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reconciliation_sessions")
data class ReconciliationSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val date: Long,
    val totalInvoices: Int,
    val totalPayments: Int,
    val matchedCount: Int,
    val unmatchedCount: Int,
    val duplicatesCount: Int,
    val riskScore: Float,
    val amountReconciled: Double,
    val discrepancy: Double
)
