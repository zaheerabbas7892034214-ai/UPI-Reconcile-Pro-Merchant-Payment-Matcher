package com.zaheer.upireconcilepro.data.model

data class ReconciliationMetrics(
    val totalInvoices: Int,
    val totalPayments: Int,
    val matchedCount: Int,
    val unmatchedInvoicesCount: Int,
    val unmatchedPaymentsCount: Int,
    val duplicatesCount: Int,
    val matchRate: Float,
    val riskScore: Float,
    val amountReconciled: Double,
    val totalInvoiceAmount: Double,
    val totalPaymentAmount: Double,
    val discrepancy: Double
)
