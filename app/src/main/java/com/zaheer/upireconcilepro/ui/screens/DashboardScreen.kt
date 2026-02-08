package com.zaheer.upireconcilepro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaheer.upireconcilepro.ui.components.MetricCard
import com.zaheer.upireconcilepro.ui.components.RiskScoreGauge
import com.zaheer.upireconcilepro.ui.components.LoadingDialog
import com.zaheer.upireconcilepro.ui.components.ErrorDialog
import com.zaheer.upireconcilepro.ui.theme.UPIReconcileProTheme
import com.zaheer.upireconcilepro.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardViewModel.DashboardUiState,
    onViewMatched: () -> Unit,
    onViewUnmatched: () -> Unit,
    onExport: () -> Unit,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onExport) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export")
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is DashboardViewModel.DashboardUiState.Loading -> {
                LoadingDialog(
                    message = "Loading dashboard...",
                    onDismiss = {}
                )
            }
            
            is DashboardViewModel.DashboardUiState.Success -> {
                DashboardContent(
                    metrics = uiState.metrics,
                    isPro = uiState.isPro,
                    onViewMatched = onViewMatched,
                    onViewUnmatched = onViewUnmatched,
                    modifier = Modifier.padding(padding)
                )
            }
            
            is DashboardViewModel.DashboardUiState.Error -> {
                errorMessage = uiState.message
            }
        }
    }

    errorMessage?.let { message ->
        ErrorDialog(
            message = message,
            onDismiss = { errorMessage = null }
        )
    }
}

@Composable
private fun DashboardContent(
    metrics: DashboardViewModel.ReconciliationMetrics,
    isPro: Boolean,
    onViewMatched: () -> Unit,
    onViewUnmatched: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Reconciliation Summary",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        RiskScoreGauge(
            score = calculateRiskScore(metrics),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Transaction Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Total Invoices",
                value = metrics.totalInvoices.toString(),
                icon = Icons.Default.Receipt,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Total Payments",
                value = metrics.totalPayments.toString(),
                icon = Icons.Default.Payment,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        MetricCard(
            title = "Match Rate",
            value = "${String.format("%.1f", metrics.matchRate)}%",
            icon = Icons.Default.TrendingUp,
            valueColor = if (metrics.matchRate >= 90f) 
                MaterialTheme.colorScheme.primary 
            else if (metrics.matchRate >= 70f)
                MaterialTheme.colorScheme.tertiary
            else
                MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Matched Transactions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Count",
                value = metrics.matchedCount.toString(),
                icon = Icons.Default.CheckCircle,
                valueColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Amount",
                value = currencyFormat.format(metrics.totalMatchedAmount),
                icon = Icons.Default.AccountBalance,
                valueColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Unmatched Items",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Invoices",
                value = metrics.unmatchedInvoices.toString(),
                icon = Icons.Default.Warning,
                valueColor = if (metrics.unmatchedInvoices > 0) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Payments",
                value = metrics.unmatchedPayments.toString(),
                icon = Icons.Default.Warning,
                valueColor = if (metrics.unmatchedPayments > 0) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Duplicates",
                value = metrics.duplicates.toString(),
                icon = Icons.Default.FileCopy,
                valueColor = if (metrics.duplicates > 0) 
                    MaterialTheme.colorScheme.tertiary 
                else 
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Unmatched Amount",
                value = currencyFormat.format(metrics.totalUnmatchedAmount),
                icon = Icons.Default.Error,
                valueColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onViewMatched,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Matched Items")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onViewUnmatched,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Warning, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text("View Unmatched Items")
                if (!isPro) {
                    Text(
                        "PRO Feature",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (!isPro) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Upgrade to PRO",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Unlock unmatched analysis, unlimited export, and more!",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

private fun calculateRiskScore(metrics: DashboardViewModel.ReconciliationMetrics): Float {
    val unmatchedRate = if (metrics.totalInvoices > 0) {
        ((metrics.unmatchedInvoices + metrics.unmatchedPayments).toFloat() / 
         (metrics.totalInvoices + metrics.totalPayments).toFloat()) * 100f
    } else {
        0f
    }
    
    val duplicateWeight = if (metrics.duplicates > 0) 10f else 0f
    
    return (unmatchedRate + duplicateWeight).coerceIn(0f, 100f)
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    UPIReconcileProTheme {
        DashboardScreen(
            uiState = DashboardViewModel.DashboardUiState.Success(
                session = com.zaheer.upireconcilepro.data.database.entity.ReconciliationSessionEntity(
                    id = 1,
                    name = "Test Session",
                    createdAt = System.currentTimeMillis(),
                    notes = ""
                ),
                metrics = DashboardViewModel.ReconciliationMetrics(
                    totalInvoices = 150,
                    totalPayments = 148,
                    matchedCount = 145,
                    unmatchedInvoices = 5,
                    unmatchedPayments = 3,
                    duplicates = 2,
                    matchRate = 96.7f,
                    totalMatchedAmount = 1250000.50,
                    totalUnmatchedAmount = 45000.00
                ),
                isPro = false
            ),
            onViewMatched = {},
            onViewUnmatched = {},
            onExport = {},
            onNavigateBack = {},
            onRefresh = {}
        )
    }
}
