package com.zaheer.upireconcilepro.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.zaheer.upireconcilepro.data.model.ReconciliationMetrics
import com.zaheer.upireconcilepro.data.model.ReconciliationResult
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelExporter {
    
    sealed class ExportResult {
        data class Success(val uri: Uri) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }
    
    fun exportToExcel(
        context: Context,
        metrics: ReconciliationMetrics,
        matched: List<ReconciliationResult.MatchedItem>,
        unmatched: List<ReconciliationResult>,
        sessionName: String = "reconciliation"
    ): ExportResult {
        return try {
            val workbook = XSSFWorkbook()
            
            createSummarySheet(workbook, metrics)
            createMatchedSheet(workbook, matched)
            createUnmatchedSheet(workbook, unmatched)
            createDuplicatesSheet(workbook, unmatched.filterIsInstance<ReconciliationResult.DuplicatePayment>())
            
            val fileName = generateFileName(sessionName)
            val file = File(context.cacheDir, fileName)
            
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            ExportResult.Success(uri)
            
        } catch (e: Exception) {
            ExportResult.Error("Export failed: ${e.message ?: "Unknown error"}")
        }
    }
    
    private fun createSummarySheet(workbook: Workbook, metrics: ReconciliationMetrics) {
        val sheet = workbook.createSheet("Summary")
        val headerStyle = createHeaderStyle(workbook)
        val dataStyle = createDataStyle(workbook)
        val titleStyle = createTitleStyle(workbook)
        
        var rowNum = 0
        
        val titleRow = sheet.createRow(rowNum++)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("Reconciliation Summary Report")
        titleCell.cellStyle = titleStyle
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 3))
        rowNum++
        
        val dateRow = sheet.createRow(rowNum++)
        dateRow.createCell(0).setCellValue("Generated:")
        dateRow.createCell(1).setCellValue(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()))
        rowNum++
        
        createMetricRow(sheet, rowNum++, "Total Invoices", metrics.totalInvoices.toString(), headerStyle, dataStyle)
        createMetricRow(sheet, rowNum++, "Total Payments", metrics.totalPayments.toString(), headerStyle, dataStyle)
        createMetricRow(sheet, rowNum++, "Matched", metrics.matchedCount.toString(), headerStyle, dataStyle)
        createMetricRow(sheet, rowNum++, "Unmatched Invoices", metrics.unmatchedInvoicesCount.toString(), headerStyle, dataStyle)
        createMetricRow(sheet, rowNum++, "Unmatched Payments", metrics.unmatchedPaymentsCount.toString(), headerStyle, dataStyle)
        createMetricRow(sheet, rowNum++, "Duplicates", metrics.duplicatesCount.toString(), headerStyle, dataStyle)
        rowNum++
        
        createMetricRow(sheet, rowNum++, "Match Rate", String.format("%.2f%%", metrics.matchRate), headerStyle, dataStyle)
        createMetricRow(sheet, rowNum++, "Risk Score", String.format("%.2f%%", metrics.riskScore), headerStyle, dataStyle)
        rowNum++
        
        createMetricRow(sheet, rowNum++, "Total Invoice Amount", formatCurrency(metrics.totalInvoiceAmount), headerStyle, dataStyle)
        createMetricRow(sheet, rowNum++, "Total Payment Amount", formatCurrency(metrics.totalPaymentAmount), headerStyle, dataStyle)
        createMetricRow(sheet, rowNum++, "Amount Reconciled", formatCurrency(metrics.amountReconciled), headerStyle, dataStyle)
        createMetricRow(sheet, rowNum++, "Discrepancy", formatCurrency(metrics.discrepancy), headerStyle, dataStyle)
        
        sheet.setColumnWidth(0, 6000)
        sheet.setColumnWidth(1, 5000)
    }
    
    private fun createMatchedSheet(workbook: Workbook, matched: List<ReconciliationResult.MatchedItem>) {
        val sheet = workbook.createSheet("Matched Items")
        val headerStyle = createHeaderStyle(workbook)
        val dataStyle = createDataStyle(workbook)
        
        val headerRow = sheet.createRow(0)
        val headers = listOf("Invoice Ref", "UPI Ref", "Amount", "Invoice Date", "UPI Date", "Match Type", "Merchant")
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }
        
        matched.forEachIndexed { index, item ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).apply {
                setCellValue(item.invoiceRef)
                cellStyle = dataStyle
            }
            row.createCell(1).apply {
                setCellValue(item.upiRef)
                cellStyle = dataStyle
            }
            row.createCell(2).apply {
                setCellValue(formatCurrency(item.amount))
                cellStyle = dataStyle
            }
            row.createCell(3).apply {
                setCellValue(formatDate(item.invoiceDate))
                cellStyle = dataStyle
            }
            row.createCell(4).apply {
                setCellValue(formatDate(item.upiDate))
                cellStyle = dataStyle
            }
            row.createCell(5).apply {
                setCellValue(item.matchType.name)
                cellStyle = dataStyle
            }
            row.createCell(6).apply {
                setCellValue(item.merchant)
                cellStyle = dataStyle
            }
        }
        
        for (i in 0..6) {
            sheet.autoSizeColumn(i)
        }
    }
    
    private fun createUnmatchedSheet(workbook: Workbook, unmatched: List<ReconciliationResult>) {
        val sheet = workbook.createSheet("Unmatched Items")
        val headerStyle = createHeaderStyle(workbook)
        val dataStyle = createDataStyle(workbook)
        
        val headerRow = sheet.createRow(0)
        val headers = listOf("Type", "Reference", "Amount", "Date", "Reason")
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }
        
        val unmatchedItems = unmatched.filterIsInstance<ReconciliationResult.UnmatchedInvoice>() +
                             unmatched.filterIsInstance<ReconciliationResult.UnmatchedPayment>()
        
        unmatchedItems.forEachIndexed { index, item ->
            val row = sheet.createRow(index + 1)
            when (item) {
                is ReconciliationResult.UnmatchedInvoice -> {
                    row.createCell(0).apply {
                        setCellValue("Invoice")
                        cellStyle = dataStyle
                    }
                    row.createCell(1).apply {
                        setCellValue(item.reference)
                        cellStyle = dataStyle
                    }
                    row.createCell(2).apply {
                        setCellValue(formatCurrency(item.amount))
                        cellStyle = dataStyle
                    }
                    row.createCell(3).apply {
                        setCellValue(formatDate(item.date))
                        cellStyle = dataStyle
                    }
                    row.createCell(4).apply {
                        setCellValue(item.reason)
                        cellStyle = dataStyle
                    }
                }
                is ReconciliationResult.UnmatchedPayment -> {
                    row.createCell(0).apply {
                        setCellValue("Payment")
                        cellStyle = dataStyle
                    }
                    row.createCell(1).apply {
                        setCellValue(item.reference)
                        cellStyle = dataStyle
                    }
                    row.createCell(2).apply {
                        setCellValue(formatCurrency(item.amount))
                        cellStyle = dataStyle
                    }
                    row.createCell(3).apply {
                        setCellValue(formatDate(item.date))
                        cellStyle = dataStyle
                    }
                    row.createCell(4).apply {
                        setCellValue(item.reason)
                        cellStyle = dataStyle
                    }
                }
                else -> {}
            }
        }
        
        for (i in 0..4) {
            sheet.autoSizeColumn(i)
        }
    }
    
    private fun createDuplicatesSheet(workbook: Workbook, duplicates: List<ReconciliationResult.DuplicatePayment>) {
        val sheet = workbook.createSheet("Duplicates")
        val headerStyle = createHeaderStyle(workbook)
        val dataStyle = createDataStyle(workbook)
        
        val headerRow = sheet.createRow(0)
        val headers = listOf("Reference", "Amount", "Date", "Reason")
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }
        
        duplicates.forEachIndexed { index, item ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).apply {
                setCellValue(item.reference)
                cellStyle = dataStyle
            }
            row.createCell(1).apply {
                setCellValue(formatCurrency(item.amount))
                cellStyle = dataStyle
            }
            row.createCell(2).apply {
                setCellValue(formatDate(item.date))
                cellStyle = dataStyle
            }
            row.createCell(3).apply {
                setCellValue(item.reason)
                cellStyle = dataStyle
            }
        }
        
        for (i in 0..3) {
            sheet.autoSizeColumn(i)
        }
    }
    
    private fun createHeaderStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        font.color = IndexedColors.WHITE.index
        style.setFont(font)
        style.fillForegroundColor = IndexedColors.DARK_BLUE.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        style.borderBottom = BorderStyle.THIN
        style.borderTop = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        return style
    }
    
    private fun createDataStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        style.borderBottom = BorderStyle.THIN
        style.borderTop = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        style.verticalAlignment = VerticalAlignment.CENTER
        return style
    }
    
    private fun createTitleStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        font.fontHeightInPoints = 16
        style.setFont(font)
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        return style
    }
    
    private fun createMetricRow(
        sheet: Sheet,
        rowNum: Int,
        label: String,
        value: String,
        headerStyle: CellStyle,
        dataStyle: CellStyle
    ) {
        val row = sheet.createRow(rowNum)
        row.createCell(0).apply {
            setCellValue(label)
            cellStyle = headerStyle
        }
        row.createCell(1).apply {
            setCellValue(value)
            cellStyle = dataStyle
        }
    }
    
    private fun formatCurrency(amount: Double): String {
        return String.format(Locale.getDefault(), "₹%.2f", amount)
    }
    
    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    }
    
    private fun generateFileName(sessionName: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val cleanName = sessionName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return "${cleanName}_${timestamp}.xlsx"
    }
}
