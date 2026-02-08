package com.zaheer.upireconcilepro.util

import com.zaheer.upireconcilepro.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object ReconciliationEngine {
    
    data class ReconciliationConfig(
        val dateTolerance: Int = 1,
        val amountTolerance: Double = 0.01,
        val fuzzyMatchThreshold: Float = 0.8f
    )
    
    data class ReconciliationOutput(
        val matched: List<ReconciliationResult.MatchedItem>,
        val unmatchedInvoices: List<ReconciliationResult.UnmatchedInvoice>,
        val unmatchedPayments: List<ReconciliationResult.UnmatchedPayment>,
        val duplicates: List<ReconciliationResult.DuplicatePayment>,
        val metrics: ReconciliationMetrics
    )
    
    private data class Invoice(
        val reference: String,
        val amount: Double,
        val date: Long,
        val merchant: String
    )
    
    private data class Payment(
        val reference: String,
        val amount: Double,
        val date: Long,
        val merchant: String
    )
    
    fun reconcile(
        invoices: CSVData,
        payments: CSVData,
        invoiceMapping: ColumnMapping,
        paymentMapping: ColumnMapping,
        config: ReconciliationConfig = ReconciliationConfig()
    ): ReconciliationOutput {
        
        val parsedInvoices = parseInvoices(invoices, invoiceMapping)
        val parsedPayments = parsePayments(payments, paymentMapping)
        
        val matched = mutableListOf<ReconciliationResult.MatchedItem>()
        val unmatchedInvoices = mutableListOf<ReconciliationResult.UnmatchedInvoice>()
        val unmatchedPayments = mutableListOf<ReconciliationResult.UnmatchedPayment>()
        val duplicates = mutableListOf<ReconciliationResult.DuplicatePayment>()
        
        val usedPayments = mutableSetOf<Int>()
        val paymentUsageCount = mutableMapOf<Int, Int>()
        
        parsedInvoices.forEach { invoice ->
            var matchFound = false
            
            parsedPayments.forEachIndexed { paymentIndex, payment ->
                
                if (isExactMatch(invoice, payment, config)) {
                    val count = paymentUsageCount.getOrDefault(paymentIndex, 0)
                    
                    if (count == 0) {
                        matched.add(
                            ReconciliationResult.MatchedItem(
                                invoiceRef = invoice.reference,
                                upiRef = payment.reference,
                                amount = invoice.amount,
                                invoiceDate = invoice.date,
                                upiDate = payment.date,
                                matchType = MatchType.EXACT,
                                merchant = invoice.merchant
                            )
                        )
                        usedPayments.add(paymentIndex)
                        paymentUsageCount[paymentIndex] = 1
                        matchFound = true
                        return@forEachIndexed
                    } else {
                        duplicates.add(
                            ReconciliationResult.DuplicatePayment(
                                reference = payment.reference,
                                amount = payment.amount,
                                date = payment.date,
                                reason = "Duplicate payment for invoice ${invoice.reference}"
                            )
                        )
                        paymentUsageCount[paymentIndex] = count + 1
                    }
                }
            }
            
            if (!matchFound) {
                parsedPayments.forEachIndexed { paymentIndex, payment ->
                    if (paymentIndex !in usedPayments) {
                        if (isPartialMatch(invoice, payment, config)) {
                            matched.add(
                                ReconciliationResult.MatchedItem(
                                    invoiceRef = invoice.reference,
                                    upiRef = payment.reference,
                                    amount = payment.amount,
                                    invoiceDate = invoice.date,
                                    upiDate = payment.date,
                                    matchType = MatchType.PARTIAL,
                                    merchant = invoice.merchant
                                )
                            )
                            usedPayments.add(paymentIndex)
                            matchFound = true
                            return@forEachIndexed
                        }
                    }
                }
            }
            
            if (!matchFound) {
                unmatchedInvoices.add(
                    ReconciliationResult.UnmatchedInvoice(
                        reference = invoice.reference,
                        amount = invoice.amount,
                        date = invoice.date,
                        reason = "No matching payment found"
                    )
                )
            }
        }
        
        parsedPayments.forEachIndexed { index, payment ->
            if (index !in usedPayments) {
                unmatchedPayments.add(
                    ReconciliationResult.UnmatchedPayment(
                        reference = payment.reference,
                        amount = payment.amount,
                        date = payment.date,
                        reason = "No matching invoice found"
                    )
                )
            }
        }
        
        val metrics = calculateMetrics(
            invoices = parsedInvoices,
            payments = parsedPayments,
            matched = matched,
            unmatchedInvoices = unmatchedInvoices,
            unmatchedPayments = unmatchedPayments,
            duplicates = duplicates
        )
        
        return ReconciliationOutput(
            matched = matched,
            unmatchedInvoices = unmatchedInvoices,
            unmatchedPayments = unmatchedPayments,
            duplicates = duplicates,
            metrics = metrics
        )
    }
    
    private fun parseInvoices(csvData: CSVData, mapping: ColumnMapping): List<Invoice> {
        return csvData.rows.mapNotNull { row ->
            try {
                val reference = row.getOrNull(mapping.referenceIndex)?.trim() ?: return@mapNotNull null
                val amountStr = row.getOrNull(mapping.amountIndex)?.trim() ?: return@mapNotNull null
                val dateStr = row.getOrNull(mapping.dateIndex)?.trim() ?: return@mapNotNull null
                val merchant = row.getOrNull(mapping.merchantIndex ?: -1)?.trim() ?: ""
                
                val amount = parseAmount(amountStr) ?: return@mapNotNull null
                val date = parseDate(dateStr) ?: return@mapNotNull null
                
                if (reference.isNotEmpty() && amount > 0) {
                    Invoice(reference, amount, date, merchant)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun parsePayments(csvData: CSVData, mapping: ColumnMapping): List<Payment> {
        return csvData.rows.mapNotNull { row ->
            try {
                val reference = row.getOrNull(mapping.referenceIndex)?.trim() ?: return@mapNotNull null
                val amountStr = row.getOrNull(mapping.amountIndex)?.trim() ?: return@mapNotNull null
                val dateStr = row.getOrNull(mapping.dateIndex)?.trim() ?: return@mapNotNull null
                val merchant = row.getOrNull(mapping.merchantIndex ?: -1)?.trim() ?: ""
                
                val amount = parseAmount(amountStr) ?: return@mapNotNull null
                val date = parseDate(dateStr) ?: return@mapNotNull null
                
                if (reference.isNotEmpty() && amount > 0) {
                    Payment(reference, amount, date, merchant)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun parseAmount(amountStr: String): Double? {
        return try {
            val cleaned = amountStr
                .replace(Regex("[₹Rs.,INR\\s]"), "")
                .trim()
            
            cleaned.toDoubleOrNull()?.takeIf { it >= 0 }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseDate(dateStr: String): Long? {
        val patterns = listOf(
            "dd/MM/yyyy", "dd-MM-yyyy", "dd.MM.yyyy",
            "yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd",
            "dd/MM/yy", "dd-MM-yy", "MM/dd/yyyy",
            "dd MMM yyyy", "dd-MMM-yyyy", "MMM dd, yyyy"
        )
        
        patterns.forEach { pattern ->
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                sdf.isLenient = false
                val date = sdf.parse(dateStr)
                return date?.time
            } catch (e: Exception) {
                // Try next pattern
            }
        }
        
        return dateStr.toLongOrNull()?.takeIf { 
            it > 946684800000L && it < 4102444800000L 
        }
    }
    
    private fun isExactMatch(
        invoice: Invoice,
        payment: Payment,
        config: ReconciliationConfig
    ): Boolean {
        val amountMatch = abs(invoice.amount - payment.amount) <= config.amountTolerance
        val dateMatch = isDateWithinTolerance(invoice.date, payment.date, config.dateTolerance)
        val referenceMatch = invoice.reference.equals(payment.reference, ignoreCase = true)
        
        return amountMatch && (dateMatch || referenceMatch)
    }
    
    private fun isPartialMatch(
        invoice: Invoice,
        payment: Payment,
        config: ReconciliationConfig
    ): Boolean {
        val amountMatch = abs(invoice.amount - payment.amount) <= config.amountTolerance
        val dateMatch = isDateWithinTolerance(invoice.date, payment.date, config.dateTolerance)
        
        return amountMatch && dateMatch
    }
    
    private fun isDateWithinTolerance(date1: Long, date2: Long, toleranceDays: Int): Boolean {
        val dayInMillis = 24 * 60 * 60 * 1000L
        val diff = abs(date1 - date2)
        return diff <= toleranceDays * dayInMillis
    }
    
    private fun calculateMetrics(
        invoices: List<Invoice>,
        payments: List<Payment>,
        matched: List<ReconciliationResult.MatchedItem>,
        unmatchedInvoices: List<ReconciliationResult.UnmatchedInvoice>,
        unmatchedPayments: List<ReconciliationResult.UnmatchedPayment>,
        duplicates: List<ReconciliationResult.DuplicatePayment>
    ): ReconciliationMetrics {
        
        val totalInvoices = invoices.size
        val totalPayments = payments.size
        val matchedCount = matched.size
        val unmatchedInvoicesCount = unmatchedInvoices.size
        val unmatchedPaymentsCount = unmatchedPayments.size
        val duplicatesCount = duplicates.size
        
        val matchRate = if (totalInvoices > 0) {
            (matchedCount.toFloat() / totalInvoices.toFloat()) * 100f
        } else {
            0f
        }
        
        val totalInvoiceAmount = invoices.sumOf { it.amount }
        val totalPaymentAmount = payments.sumOf { it.amount }
        val amountReconciled = matched.sumOf { it.amount }
        val discrepancy = totalInvoiceAmount - totalPaymentAmount
        
        val riskScore = calculateRiskScore(
            matchRate = matchRate,
            unmatchedInvoicesCount = unmatchedInvoicesCount,
            duplicatesCount = duplicatesCount,
            discrepancy = abs(discrepancy),
            totalInvoiceAmount = totalInvoiceAmount
        )
        
        return ReconciliationMetrics(
            totalInvoices = totalInvoices,
            totalPayments = totalPayments,
            matchedCount = matchedCount,
            unmatchedInvoicesCount = unmatchedInvoicesCount,
            unmatchedPaymentsCount = unmatchedPaymentsCount,
            duplicatesCount = duplicatesCount,
            matchRate = matchRate,
            riskScore = riskScore,
            amountReconciled = amountReconciled,
            totalInvoiceAmount = totalInvoiceAmount,
            totalPaymentAmount = totalPaymentAmount,
            discrepancy = discrepancy
        )
    }
    
    private fun calculateRiskScore(
        matchRate: Float,
        unmatchedInvoicesCount: Int,
        duplicatesCount: Int,
        discrepancy: Double,
        totalInvoiceAmount: Double
    ): Float {
        var risk = 0f
        
        risk += (100f - matchRate) * 0.4f
        
        risk += (unmatchedInvoicesCount * 2).coerceAtMost(30).toFloat()
        
        risk += (duplicatesCount * 3).coerceAtMost(20).toFloat()
        
        if (totalInvoiceAmount > 0) {
            val discrepancyPercent = (discrepancy / totalInvoiceAmount) * 100
            risk += (discrepancyPercent * 0.5).coerceAtMost(20.0).toFloat()
        }
        
        return risk.coerceIn(0f, 100f)
    }
}
