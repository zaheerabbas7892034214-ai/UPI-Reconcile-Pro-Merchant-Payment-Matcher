package com.zaheer.upireconcilepro.util

import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.core.content.FileProvider
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.zaheer.upireconcilepro.data.model.ReconciliationMetrics
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PDFExporter {
    
    sealed class ExportResult {
        data class Success(val uri: Uri) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }
    
    fun exportToPDF(
        context: Context,
        metrics: ReconciliationMetrics,
        sessionName: String = "reconciliation"
    ): ExportResult {
        return try {
            val fileName = generateFileName(sessionName)
            val file = File(context.cacheDir, fileName)
            
            val pdfWriter = PdfWriter(FileOutputStream(file))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)
            
            document.setMargins(40f, 40f, 40f, 40f)
            
            addHeader(document, sessionName)
            addSummarySection(document, metrics)
            addMetricsTable(document, metrics)
            addRiskAnalysis(document, metrics)
            addFooter(document)
            
            document.close()
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            ExportResult.Success(uri)
            
        } catch (e: Exception) {
            ExportResult.Error("PDF export failed: ${e.message ?: "Unknown error"}")
        }
    }
    
    private fun addHeader(document: Document, sessionName: String) {
        val title = Paragraph("UPI Reconciliation Report")
            .setFontSize(24f)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(5f)
        
        val subtitle = Paragraph(sessionName)
            .setFontSize(14f)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DeviceRgb(100, 100, 100))
            .setMarginBottom(10f)
        
        val date = Paragraph("Generated: ${SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date())}")
            .setFontSize(10f)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DeviceRgb(128, 128, 128))
            .setMarginBottom(20f)
        
        document.add(title)
        document.add(subtitle)
        document.add(date)
        
        val divider = Paragraph()
            .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 1f))
            .setMarginBottom(20f)
        document.add(divider)
    }
    
    private fun addSummarySection(document: Document, metrics: ReconciliationMetrics) {
        val sectionTitle = Paragraph("Executive Summary")
            .setFontSize(16f)
            .setBold()
            .setMarginBottom(10f)
        document.add(sectionTitle)
        
        val summaryText = buildString {
            append("This reconciliation processed ${metrics.totalInvoices} invoices against ")
            append("${metrics.totalPayments} payments. ")
            append("${metrics.matchedCount} transactions were successfully matched (${String.format("%.1f%%", metrics.matchRate)} match rate). ")
            
            if (metrics.unmatchedInvoicesCount > 0) {
                append("${metrics.unmatchedInvoicesCount} invoices remain unmatched. ")
            }
            
            if (metrics.duplicatesCount > 0) {
                append("${metrics.duplicatesCount} duplicate payments were detected. ")
            }
            
            val discrepancy = metrics.discrepancy
            if (kotlin.math.abs(discrepancy) > 0.01) {
                val sign = if (discrepancy > 0) "excess" else "shortfall"
                append("A financial discrepancy of ${formatCurrency(kotlin.math.abs(discrepancy))} ($sign) was identified.")
            }
        }
        
        val summary = Paragraph(summaryText)
            .setFontSize(11f)
            .setTextAlignment(TextAlignment.JUSTIFIED)
            .setMarginBottom(20f)
        document.add(summary)
    }
    
    private fun addMetricsTable(document: Document, metrics: ReconciliationMetrics) {
        val sectionTitle = Paragraph("Detailed Metrics")
            .setFontSize(16f)
            .setBold()
            .setMarginBottom(10f)
        document.add(sectionTitle)
        
        val table = Table(UnitValue.createPercentArray(floatArrayOf(3f, 2f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setMarginBottom(20f)
        
        addMetricRow(table, "Transaction Counts", "", isHeader = true)
        addMetricRow(table, "Total Invoices", metrics.totalInvoices.toString())
        addMetricRow(table, "Total Payments", metrics.totalPayments.toString())
        addMetricRow(table, "Matched Transactions", metrics.matchedCount.toString())
        addMetricRow(table, "Unmatched Invoices", metrics.unmatchedInvoicesCount.toString())
        addMetricRow(table, "Unmatched Payments", metrics.unmatchedPaymentsCount.toString())
        addMetricRow(table, "Duplicate Payments", metrics.duplicatesCount.toString())
        
        addMetricRow(table, "Performance Metrics", "", isHeader = true)
        addMetricRow(table, "Match Rate", String.format("%.2f%%", metrics.matchRate))
        addMetricRow(table, "Risk Score", String.format("%.2f%%", metrics.riskScore))
        
        addMetricRow(table, "Financial Summary", "", isHeader = true)
        addMetricRow(table, "Total Invoice Amount", formatCurrency(metrics.totalInvoiceAmount))
        addMetricRow(table, "Total Payment Amount", formatCurrency(metrics.totalPaymentAmount))
        addMetricRow(table, "Amount Reconciled", formatCurrency(metrics.amountReconciled))
        addMetricRow(table, "Discrepancy", formatCurrency(metrics.discrepancy))
        
        document.add(table)
    }
    
    private fun addRiskAnalysis(document: Document, metrics: ReconciliationMetrics) {
        val sectionTitle = Paragraph("Risk Analysis")
            .setFontSize(16f)
            .setBold()
            .setMarginBottom(10f)
        document.add(sectionTitle)
        
        val riskLevel = when {
            metrics.riskScore < 20f -> "Low"
            metrics.riskScore < 50f -> "Medium"
            metrics.riskScore < 75f -> "High"
            else -> "Critical"
        }
        
        val riskColor = when {
            metrics.riskScore < 20f -> DeviceRgb(76, 175, 80)
            metrics.riskScore < 50f -> DeviceRgb(255, 193, 7)
            metrics.riskScore < 75f -> DeviceRgb(255, 152, 0)
            else -> DeviceRgb(244, 67, 54)
        }
        
        val riskParagraph = Paragraph()
            .add("Risk Level: ")
            .add(Paragraph(riskLevel).setFontColor(riskColor).setBold())
            .add(" (Score: ${String.format("%.1f", metrics.riskScore)}/100)")
            .setFontSize(12f)
            .setMarginBottom(10f)
        document.add(riskParagraph)
        
        val recommendations = mutableListOf<String>()
        
        if (metrics.matchRate < 90f) {
            recommendations.add("Match rate is below 90%. Review unmatched items to identify missing payments or invoicing errors.")
        }
        
        if (metrics.duplicatesCount > 0) {
            recommendations.add("Duplicate payments detected. Investigate potential refund scenarios or payment processing errors.")
        }
        
        if (kotlin.math.abs(metrics.discrepancy) > metrics.totalInvoiceAmount * 0.05) {
            recommendations.add("Significant financial discrepancy (>5%). Urgent review recommended.")
        }
        
        if (metrics.unmatchedInvoicesCount > metrics.totalInvoices * 0.1) {
            recommendations.add("High percentage of unmatched invoices. Check for payment delays or data quality issues.")
        }
        
        if (recommendations.isNotEmpty()) {
            val recTitle = Paragraph("Recommendations:")
                .setFontSize(12f)
                .setBold()
                .setMarginBottom(5f)
            document.add(recTitle)
            
            recommendations.forEachIndexed { index, recommendation ->
                val recPara = Paragraph("${index + 1}. $recommendation")
                    .setFontSize(10f)
                    .setMarginLeft(10f)
                    .setMarginBottom(3f)
                document.add(recPara)
            }
        } else {
            val goodNews = Paragraph("No significant issues detected. Reconciliation appears healthy.")
                .setFontSize(10f)
                .setFontColor(DeviceRgb(76, 175, 80))
                .setMarginBottom(10f)
            document.add(goodNews)
        }
    }
    
    private fun addFooter(document: Document) {
        val footer = Paragraph()
            .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 1f))
            .setMarginTop(20f)
            .setMarginBottom(10f)
        document.add(footer)
        
        val footerText = Paragraph("Generated by UPI Reconcile Pro • Confidential Document")
            .setFontSize(8f)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DeviceRgb(128, 128, 128))
        document.add(footerText)
    }
    
    private fun addMetricRow(table: Table, label: String, value: String, isHeader: Boolean = false) {
        val labelCell = Cell()
            .add(Paragraph(label).setFontSize(10f).apply {
                if (isHeader) setBold()
            })
            .setPadding(8f)
        
        val valueCell = Cell()
            .add(Paragraph(value).setFontSize(10f).apply {
                if (isHeader) setBold()
            })
            .setPadding(8f)
            .setTextAlignment(TextAlignment.RIGHT)
        
        if (isHeader) {
            val headerColor = DeviceRgb(63, 81, 181)
            labelCell.setBackgroundColor(headerColor)
                .setFontColor(ColorConstants.WHITE)
            valueCell.setBackgroundColor(headerColor)
                .setFontColor(ColorConstants.WHITE)
        } else {
            labelCell.setBackgroundColor(DeviceRgb(245, 245, 245))
        }
        
        table.addCell(labelCell)
        table.addCell(valueCell)
    }
    
    private fun formatCurrency(amount: Double): String {
        return String.format(Locale.getDefault(), "₹%.2f", amount)
    }
    
    private fun generateFileName(sessionName: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val cleanName = sessionName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return "${cleanName}_${timestamp}.pdf"
    }
}
