package com.zaheer.upireconcilepro.util

import com.zaheer.upireconcilepro.data.model.CSVData
import com.zaheer.upireconcilepro.data.model.ColumnMapping
import java.text.SimpleDateFormat
import java.util.*

object ColumnDetector {
    
    private val DATE_PATTERNS = listOf(
        "dd/MM/yyyy", "dd-MM-yyyy", "dd.MM.yyyy",
        "yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd",
        "dd/MM/yy", "dd-MM-yy", "MM/dd/yyyy",
        "dd MMM yyyy", "dd-MMM-yyyy", "MMM dd, yyyy"
    )
    
    private val AMOUNT_PATTERNS = listOf(
        Regex("""₹\s*[\d,]+\.?\d*"""),
        Regex("""Rs\.?\s*[\d,]+\.?\d*"""),
        Regex("""INR\s*[\d,]+\.?\d*"""),
        Regex("""[\d,]+\.\d{2}"""),
        Regex("""^\d+\.?\d*$""")
    )
    
    private val REFERENCE_KEYWORDS = listOf(
        "ref", "reference", "transaction", "trans", "txn", "utr", 
        "id", "order", "invoice", "bill", "number", "no"
    )
    
    private val AMOUNT_KEYWORDS = listOf(
        "amount", "total", "value", "price", "payment", "sum"
    )
    
    private val DATE_KEYWORDS = listOf(
        "date", "time", "timestamp", "created", "issued", "paid"
    )
    
    private val MERCHANT_KEYWORDS = listOf(
        "merchant", "vendor", "payee", "seller", "shop", "store", "name"
    )
    
    fun detectColumns(csvData: CSVData): ColumnMapping? {
        val amountIndex = detectAmountColumn(csvData)
        val dateIndex = detectDateColumn(csvData)
        val referenceIndex = detectReferenceColumn(csvData)
        val merchantIndex = detectMerchantColumn(csvData)
        
        if (amountIndex == null || dateIndex == null || referenceIndex == null) {
            return null
        }
        
        return ColumnMapping(
            referenceIndex = referenceIndex,
            amountIndex = amountIndex,
            dateIndex = dateIndex,
            merchantIndex = merchantIndex
        )
    }
    
    fun detectAmountColumn(csvData: CSVData): Int? {
        val headers = csvData.headers
        val sampleRows = csvData.rows.take(10)
        
        headers.forEachIndexed { index, header ->
            val headerLower = header.lowercase(Locale.ROOT)
            val keywordMatch = AMOUNT_KEYWORDS.any { keyword ->
                headerLower.contains(keyword)
            }
            
            if (keywordMatch) {
                val isValid = validateAmountColumn(sampleRows, index)
                if (isValid) return index
            }
        }
        
        headers.indices.forEach { index ->
            val patternMatch = sampleRows.count { row ->
                if (index < row.size) {
                    val value = row[index].trim()
                    AMOUNT_PATTERNS.any { pattern -> pattern.matches(value) }
                } else {
                    false
                }
            }
            
            if (patternMatch >= sampleRows.size * 0.7) {
                return index
            }
        }
        
        return null
    }
    
    fun detectDateColumn(csvData: CSVData): Int? {
        val headers = csvData.headers
        val sampleRows = csvData.rows.take(10)
        
        headers.forEachIndexed { index, header ->
            val headerLower = header.lowercase(Locale.ROOT)
            val keywordMatch = DATE_KEYWORDS.any { keyword ->
                headerLower.contains(keyword)
            }
            
            if (keywordMatch) {
                val isValid = validateDateColumn(sampleRows, index)
                if (isValid) return index
            }
        }
        
        headers.indices.forEach { index ->
            val dateMatches = sampleRows.count { row ->
                if (index < row.size) {
                    isDateValue(row[index].trim())
                } else {
                    false
                }
            }
            
            if (dateMatches >= sampleRows.size * 0.7) {
                return index
            }
        }
        
        return null
    }
    
    fun detectReferenceColumn(csvData: CSVData): Int? {
        val headers = csvData.headers
        val sampleRows = csvData.rows.take(10)
        
        headers.forEachIndexed { index, header ->
            val headerLower = header.lowercase(Locale.ROOT)
            val keywordMatch = REFERENCE_KEYWORDS.any { keyword ->
                headerLower.contains(keyword)
            }
            
            if (keywordMatch) {
                val isValid = validateReferenceColumn(sampleRows, index)
                if (isValid) return index
            }
        }
        
        headers.indices.forEach { index ->
            val alphanumericCount = sampleRows.count { row ->
                if (index < row.size) {
                    val value = row[index].trim()
                    value.isNotEmpty() && 
                    value.any { it.isDigit() } && 
                    value.length >= 6
                } else {
                    false
                }
            }
            
            if (alphanumericCount >= sampleRows.size * 0.8) {
                return index
            }
        }
        
        return null
    }
    
    fun detectMerchantColumn(csvData: CSVData): Int? {
        val headers = csvData.headers
        val sampleRows = csvData.rows.take(10)
        
        headers.forEachIndexed { index, header ->
            val headerLower = header.lowercase(Locale.ROOT)
            val keywordMatch = MERCHANT_KEYWORDS.any { keyword ->
                headerLower.contains(keyword)
            }
            
            if (keywordMatch) {
                val isValid = validateMerchantColumn(sampleRows, index)
                if (isValid) return index
            }
        }
        
        return null
    }
    
    private fun validateAmountColumn(rows: List<List<String>>, index: Int): Boolean {
        var validCount = 0
        
        rows.forEach { row ->
            if (index < row.size) {
                val value = row[index].trim()
                val cleanValue = value.replace(Regex("[₹Rs.,INR\\s]"), "")
                
                try {
                    val numValue = cleanValue.toDoubleOrNull()
                    if (numValue != null && numValue >= 0) {
                        validCount++
                    }
                } catch (e: Exception) {
                    // Invalid amount
                }
            }
        }
        
        return validCount >= rows.size * 0.7
    }
    
    private fun validateDateColumn(rows: List<List<String>>, index: Int): Boolean {
        var validCount = 0
        
        rows.forEach { row ->
            if (index < row.size) {
                val value = row[index].trim()
                if (isDateValue(value)) {
                    validCount++
                }
            }
        }
        
        return validCount >= rows.size * 0.7
    }
    
    private fun validateReferenceColumn(rows: List<List<String>>, index: Int): Boolean {
        val values = mutableSetOf<String>()
        
        rows.forEach { row ->
            if (index < row.size) {
                val value = row[index].trim()
                if (value.isNotEmpty()) {
                    values.add(value)
                }
            }
        }
        
        return values.size == rows.size && values.all { it.length >= 4 }
    }
    
    private fun validateMerchantColumn(rows: List<List<String>>, index: Int): Boolean {
        var validCount = 0
        
        rows.forEach { row ->
            if (index < row.size) {
                val value = row[index].trim()
                if (value.isNotEmpty() && value.any { it.isLetter() }) {
                    validCount++
                }
            }
        }
        
        return validCount >= rows.size * 0.7
    }
    
    private fun isDateValue(value: String): Boolean {
        if (value.isBlank()) return false
        
        DATE_PATTERNS.forEach { pattern ->
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                sdf.isLenient = false
                sdf.parse(value)
                return true
            } catch (e: Exception) {
                // Try next pattern
            }
        }
        
        try {
            value.toLongOrNull()?.let { timestamp ->
                if (timestamp > 946684800000L && timestamp < 4102444800000L) {
                    return true
                }
            }
        } catch (e: Exception) {
            // Not a timestamp
        }
        
        return false
    }
    
    fun getConfidenceScore(csvData: CSVData, mapping: ColumnMapping): Float {
        var score = 0f
        val sampleRows = csvData.rows.take(10)
        
        val amountValid = validateAmountColumn(sampleRows, mapping.amountIndex)
        if (amountValid) score += 0.33f
        
        val dateValid = validateDateColumn(sampleRows, mapping.dateIndex)
        if (dateValid) score += 0.33f
        
        val refValid = validateReferenceColumn(sampleRows, mapping.referenceIndex)
        if (refValid) score += 0.34f
        
        return score
    }
}
