# UPI Reconcile Pro - Utility Classes

This directory contains production-ready utility classes for the UPI Reconcile Pro application.

## Classes Overview

### CSVParser.kt
Robust CSV parsing with kotlin-csv-jvm library.

**Features:**
- Parse CSV files from Android URIs
- Support for multiple delimiters (comma, semicolon, tab, pipe)
- Handle quoted fields and escape characters
- Multiple encoding support (UTF-8, ISO-8859-1, Windows-1252, UTF-16)
- Auto-detect delimiter and encoding
- Structure validation

**Usage:**
```kotlin
val result = CSVParser.parseCSV(context, fileUri)
when (result) {
    is CSVParser.ParseResult.Success -> {
        val csvData = result.data
        // Process csvData.headers and csvData.rows
    }
    is CSVParser.ParseResult.Error -> {
        // Handle error: result.message
    }
}
```

### ColumnDetector.kt
Auto-detect CSV columns using pattern matching.

**Features:**
- Detect amount columns (currency symbols, decimal numbers)
- Detect date columns (multiple date formats, timestamps)
- Detect reference columns (transaction IDs, UTR numbers)
- Detect merchant columns (vendor names)
- Confidence scoring

**Usage:**
```kotlin
val mapping = ColumnDetector.detectColumns(csvData)
if (mapping != null) {
    // Use mapping.amountIndex, dateIndex, referenceIndex, merchantIndex
} else {
    // Auto-detection failed, prompt user for manual mapping
}
```

### ReconciliationEngine.kt
Core matching algorithm for invoice-payment reconciliation.

**Features:**
- Exact amount and reference matching
- Date tolerance checking (configurable)
- Partial payment detection
- Duplicate payment detection
- Comprehensive metrics calculation
- Risk score computation

**Usage:**
```kotlin
val output = ReconciliationEngine.reconcile(
    invoices = invoiceCsvData,
    payments = paymentCsvData,
    invoiceMapping = invoiceMapping,
    paymentMapping = paymentMapping,
    config = ReconciliationConfig(dateTolerance = 2)
)

// Access results
val metrics = output.metrics
val matched = output.matched
val unmatched = output.unmatchedInvoices + output.unmatchedPayments
val duplicates = output.duplicates
```

### ExcelExporter.kt
Export reconciliation results to Excel format using Apache POI.

**Features:**
- Multi-sheet workbook (Summary, Matched, Unmatched, Duplicates)
- Professional formatting with headers, borders, colors
- Auto-sizing columns
- FileProvider integration for sharing

**Usage:**
```kotlin
val result = ExcelExporter.exportToExcel(
    context = context,
    metrics = reconciliationMetrics,
    matched = matchedItems,
    unmatched = unmatchedItems,
    sessionName = "Q1_2024_Reconciliation"
)

when (result) {
    is ExcelExporter.ExportResult.Success -> {
        // Share or save file using result.uri
        shareFile(result.uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }
    is ExcelExporter.ExportResult.Error -> {
        // Handle error: result.message
    }
}
```

### PDFExporter.kt
Generate professional PDF reports using iText.

**Features:**
- Executive summary section
- Detailed metrics tables
- Risk analysis with color-coded levels
- Automated recommendations
- Professional layout and styling

**Usage:**
```kotlin
val result = PDFExporter.exportToPDF(
    context = context,
    metrics = reconciliationMetrics,
    sessionName = "Q1_2024_Reconciliation"
)

when (result) {
    is PDFExporter.ExportResult.Success -> {
        // Share or save PDF using result.uri
        shareFile(result.uri, "application/pdf")
    }
    is PDFExporter.ExportResult.Error -> {
        // Handle error: result.message
    }
}
```

### BiometricHelper.kt
Biometric authentication wrapper for Android.

**Features:**
- Check biometric availability
- Fallback to device credentials
- Comprehensive error handling
- Support for fingerprint and face authentication

**Usage:**
```kotlin
// Check availability
val canUseBiometric = BiometricHelper.isBiometricAvailable(activity)

// Authenticate
BiometricHelper.authenticate(
    activity = activity,
    onSuccess = {
        // Authentication successful
        proceedWithSensitiveOperation()
    },
    onError = { errorMessage ->
        // Handle authentication error
        showError(errorMessage)
    }
)
```

## Dependencies Required

All required dependencies are already configured in `app/build.gradle.kts`:

```kotlin
// CSV parsing
implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")

// Excel export
implementation("org.apache.poi:poi:5.2.5")
implementation("org.apache.poi:poi-ooxml:5.2.5")

// PDF export
implementation("com.itextpdf:itext7-core:7.2.5")

// Biometric
implementation("androidx.biometric:biometric:1.1.0")
```

## Error Handling

All utility classes follow consistent error handling patterns:
- Sealed classes for operation results (Success/Error)
- Null safety with proper Kotlin null handling
- Try-catch blocks for exception handling
- Meaningful error messages
- No silent failures

## Thread Safety

- All object classes are stateless
- Safe for concurrent use from multiple threads
- IO operations properly managed
- No shared mutable state

## Testing Considerations

When testing these utilities:
- Mock Android Context for unit tests
- Use test CSV files with various formats
- Test edge cases (empty files, malformed data, special characters)
- Verify FileProvider configuration in AndroidManifest.xml
- Test biometric with hardware and without hardware scenarios

## Security Considerations

- Files are saved to app cache directory (private to app)
- FileProvider properly configured for secure file sharing
- Biometric authentication uses Android BiometricPrompt API
- No sensitive data logged
- Proper permission handling for file access
