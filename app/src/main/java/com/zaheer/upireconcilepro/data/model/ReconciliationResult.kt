package com.zaheer.upireconcilepro.data.model

sealed class ReconciliationResult {
    data class MatchedItem(
        val invoiceRef: String,
        val upiRef: String,
        val amount: Double,
        val invoiceDate: Long,
        val upiDate: Long,
        val matchType: MatchType,
        val merchant: String
    ) : ReconciliationResult()
    
    data class UnmatchedInvoice(
        val reference: String,
        val amount: Double,
        val date: Long,
        val reason: String
    ) : ReconciliationResult()
    
    data class UnmatchedPayment(
        val reference: String,
        val amount: Double,
        val date: Long,
        val reason: String
    ) : ReconciliationResult()
    
    data class DuplicatePayment(
        val reference: String,
        val amount: Double,
        val date: Long,
        val reason: String
    ) : ReconciliationResult()
}
