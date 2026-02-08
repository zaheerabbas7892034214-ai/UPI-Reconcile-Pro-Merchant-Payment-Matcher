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
import com.zaheer.upireconcilepro.ui.components.ProFeatureBadge
import com.zaheer.upireconcilepro.ui.components.LoadingDialog
import com.zaheer.upireconcilepro.ui.theme.UPIReconcileProTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    isPro: Boolean,
    isExporting: Boolean = false,
    onNavigateBack: () -> Unit,
    onExportExcel: () -> Unit,
    onExportPdf: () -> Unit,
    onUpgrade: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Data") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!isPro) {
            ProRequiredExport(
                onUpgrade = onUpgrade,
                modifier = Modifier.padding(padding)
            )
        } else {
            ExportContent(
                isExporting = isExporting,
                onExportExcel = onExportExcel,
                onExportPdf = onExportPdf,
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (isExporting) {
        LoadingDialog(
            message = "Exporting data...",
            onDismiss = {}
        )
    }
}

@Composable
private fun ProRequiredExport(
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ProFeatureBadge()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "PRO Feature",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Export functionality is available for PRO users. Upgrade to export your reconciliation data to Excel and PDF formats.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onUpgrade,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Stars, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upgrade to PRO")
        }
    }
}

@Composable
private fun ExportContent(
    isExporting: Boolean,
    onExportExcel: () -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Export Options",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Choose a format to export your reconciliation data",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        ExportOptionCard(
            icon = Icons.Default.TableChart,
            title = "Export to Excel",
            description = "Export all data to Microsoft Excel format (.xlsx)\n\n• Matched items\n• Unmatched items\n• Summary statistics\n• Formatted and ready for analysis",
            buttonText = "Export Excel",
            onExport = onExportExcel,
            enabled = !isExporting
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExportOptionCard(
            icon = Icons.Default.PictureAsPdf,
            title = "Export to PDF",
            description = "Generate a professional PDF report\n\n• Executive summary\n• Detailed transaction lists\n• Charts and visualizations\n• Print-ready format",
            buttonText = "Export PDF",
            onExport = onExportPdf,
            enabled = !isExporting
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Export Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "• Exports include all reconciliation data\n" +
                          "• Files are saved to your Downloads folder\n" +
                          "• Large exports may take a few moments\n" +
                          "• Excel files support further data analysis\n" +
                          "• PDF reports are ideal for sharing",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

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
                    Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "PRO Feature Active",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Unlimited exports available",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    buttonText: String,
    onExport: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExportScreenPreview() {
    UPIReconcileProTheme {
        ExportScreen(
            isPro = true,
            isExporting = false,
            onNavigateBack = {},
            onExportExcel = {},
            onExportPdf = {},
            onUpgrade = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExportScreenProRequiredPreview() {
    UPIReconcileProTheme {
        ExportScreen(
            isPro = false,
            isExporting = false,
            onNavigateBack = {},
            onExportExcel = {},
            onExportPdf = {},
            onUpgrade = {}
        )
    }
}
