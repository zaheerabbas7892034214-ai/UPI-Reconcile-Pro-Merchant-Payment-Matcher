package com.zaheer.upireconcilepro.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.zaheer.upireconcilepro.ui.components.LoadingDialog
import com.zaheer.upireconcilepro.ui.components.ErrorDialog
import com.zaheer.upireconcilepro.ui.theme.UPIReconcileProTheme
import com.zaheer.upireconcilepro.viewmodel.UploadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    uiState: UploadViewModel.UploadUiState,
    onSelectFile: (Uri, UploadViewModel.FileType) -> Unit,
    onRemoveFile: (UploadViewModel.FileType) -> Unit,
    onProceed: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val invoiceFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onSelectFile(it, UploadViewModel.FileType.INVOICE) }
    }

    val paymentFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onSelectFile(it, UploadViewModel.FileType.PAYMENT) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Files") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Select CSV Files",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Upload your UPI statement and sales data to begin reconciliation",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FREE Plan Limits",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 1 Invoice file per session\n• 1 Payment file per session\n• 20 matched items visible\n\nUpgrade to PRO for unlimited files and full access!",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FileUploadCard(
                title = "Invoice/Sales CSV",
                description = "Upload your sales or invoice data",
                fileType = UploadViewModel.FileType.INVOICE,
                uiState = uiState,
                onSelectFile = { invoiceFileLauncher.launch("text/*") },
                onRemoveFile = onRemoveFile
            )

            Spacer(modifier = Modifier.height(16.dp))

            FileUploadCard(
                title = "UPI Payment CSV",
                description = "Upload your UPI statement",
                fileType = UploadViewModel.FileType.PAYMENT,
                uiState = uiState,
                onSelectFile = { paymentFileLauncher.launch("text/*") },
                onRemoveFile = onRemoveFile
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (uiState) {
                is UploadViewModel.UploadUiState.BothFilesReady -> {
                    if (uiState.canProceed) {
                        Button(
                            onClick = onProceed,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Proceed to Reconciliation")
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = "⚠️ Column detection confidence is low. Please verify the files contain valid CSV data.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                is UploadViewModel.UploadUiState.LimitReached -> {
                    errorMessage = uiState.message
                }
                else -> {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    ) {
                        Text("Select both files to proceed")
                    }
                }
            }

            if (uiState is UploadViewModel.UploadUiState.BothFilesReady) {
                Spacer(modifier = Modifier.height(16.dp))
                ColumnMappingInfo(
                    invoiceData = uiState.invoiceData,
                    paymentData = uiState.paymentData
                )
            }
        }

        if (uiState is UploadViewModel.UploadUiState.Loading) {
            LoadingDialog(
                message = "Processing file...",
                onDismiss = {}
            )
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
private fun FileUploadCard(
    title: String,
    description: String,
    fileType: UploadViewModel.FileType,
    uiState: UploadViewModel.UploadUiState,
    onSelectFile: () -> Unit,
    onRemoveFile: (UploadViewModel.FileType) -> Unit
) {
    val hasFile = when (uiState) {
        is UploadViewModel.UploadUiState.FileSelected -> uiState.fileType == fileType
        is UploadViewModel.UploadUiState.FileParsed -> uiState.fileType == fileType
        is UploadViewModel.UploadUiState.BothFilesReady -> true
        else -> false
    }

    val fileName = when (uiState) {
        is UploadViewModel.UploadUiState.FileSelected -> if (uiState.fileType == fileType) uiState.fileName else null
        is UploadViewModel.UploadUiState.FileParsed -> if (uiState.fileType == fileType) "File parsed successfully" else null
        is UploadViewModel.UploadUiState.BothFilesReady -> {
            if (fileType == UploadViewModel.FileType.INVOICE) uiState.invoiceData.fileName
            else uiState.paymentData.fileName
        }
        else -> null
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    if (hasFile) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                    contentDescription = null,
                    tint = if (hasFile) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }

            if (hasFile && fileName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onRemoveFile(fileType) }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSelectFile,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select File")
                }
            }
        }
    }
}

@Composable
private fun ColumnMappingInfo(
    invoiceData: UploadViewModel.ParsedFile,
    paymentData: UploadViewModel.ParsedFile
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Auto-Detection Results",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Invoice:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${(invoiceData.confidence * 100).toInt()}% confidence",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (invoiceData.confidence > 0.7f) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
                Column {
                    Text("Payment:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${(paymentData.confidence * 100).toInt()}% confidence",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (paymentData.confidence > 0.7f) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UploadScreenPreview() {
    UPIReconcileProTheme {
        UploadScreen(
            uiState = UploadViewModel.UploadUiState.Initial,
            onSelectFile = { _, _ -> },
            onRemoveFile = {},
            onProceed = {},
            onNavigateBack = {}
        )
    }
}
