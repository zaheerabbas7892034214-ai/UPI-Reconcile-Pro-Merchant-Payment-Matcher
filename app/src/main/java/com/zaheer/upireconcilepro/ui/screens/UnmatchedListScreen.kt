package com.zaheer.upireconcilepro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaheer.upireconcilepro.data.database.entity.UnmatchedItemEntity
import com.zaheer.upireconcilepro.ui.components.EmptyStateView
import com.zaheer.upireconcilepro.ui.components.LoadingDialog
import com.zaheer.upireconcilepro.ui.components.ErrorDialog
import com.zaheer.upireconcilepro.ui.components.ProFeatureBadge
import com.zaheer.upireconcilepro.ui.theme.UPIReconcileProTheme
import com.zaheer.upireconcilepro.viewmodel.UnmatchedViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnmatchedListScreen(
    uiState: UnmatchedViewModel.UnmatchedUiState,
    onNavigateBack: () -> Unit,
    onUpgrade: () -> Unit,
    onTabSelected: (UnmatchedViewModel.TabType) -> Unit,
    onExport: () -> Unit,
    onRefresh: () -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unmatched Items") },
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
            is UnmatchedViewModel.UnmatchedUiState.Loading -> {
                LoadingDialog(
                    message = "Loading unmatched items...",
                    onDismiss = {}
                )
            }
            
            is UnmatchedViewModel.UnmatchedUiState.ProRequired -> {
                ProRequiredContent(
                    message = uiState.message,
                    onUpgrade = onUpgrade,
                    modifier = Modifier.padding(padding)
                )
            }
            
            is UnmatchedViewModel.UnmatchedUiState.Success -> {
                UnmatchedListContent(
                    invoices = uiState.invoices,
                    payments = uiState.payments,
                    duplicates = uiState.duplicates,
                    selectedTab = uiState.selectedTab,
                    onTabSelected = onTabSelected,
                    modifier = Modifier.padding(padding)
                )
            }
            
            is UnmatchedViewModel.UnmatchedUiState.Empty -> {
                Column(modifier = Modifier.padding(padding)) {
                    TabSelector(
                        selectedTab = uiState.selectedTab,
                        invoiceCount = 0,
                        paymentCount = 0,
                        duplicateCount = 0,
                        onTabSelected = onTabSelected
                    )
                    EmptyStateView(
                        icon = Icons.Default.CheckCircle,
                        title = "No Unmatched Items",
                        message = "All transactions have been successfully matched!",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            is UnmatchedViewModel.UnmatchedUiState.Error -> {
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
private fun ProRequiredContent(
    message: String,
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
            text = message,
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
private fun UnmatchedListContent(
    invoices: List<UnmatchedItemEntity>,
    payments: List<UnmatchedItemEntity>,
    duplicates: List<UnmatchedItemEntity>,
    selectedTab: UnmatchedViewModel.TabType,
    onTabSelected: (UnmatchedViewModel.TabType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        TabSelector(
            selectedTab = selectedTab,
            invoiceCount = invoices.size,
            paymentCount = payments.size,
            duplicateCount = duplicates.size,
            onTabSelected = onTabSelected
        )

        val currentItems = when (selectedTab) {
            UnmatchedViewModel.TabType.INVOICES -> invoices
            UnmatchedViewModel.TabType.PAYMENTS -> payments
            UnmatchedViewModel.TabType.DUPLICATES -> duplicates
        }

        if (currentItems.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.CheckCircle,
                title = "No ${selectedTab.name.lowercase()} found",
                message = "Great! All ${selectedTab.name.lowercase()} have been matched.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentItems, key = { it.id }) { item ->
                    UnmatchedItemCard(
                        item = item,
                        type = selectedTab
                    )
                }
            }
        }
    }
}

@Composable
private fun TabSelector(
    selectedTab: UnmatchedViewModel.TabType,
    invoiceCount: Int,
    paymentCount: Int,
    duplicateCount: Int,
    onTabSelected: (UnmatchedViewModel.TabType) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = Modifier.fillMaxWidth()
    ) {
        Tab(
            selected = selectedTab == UnmatchedViewModel.TabType.INVOICES,
            onClick = { onTabSelected(UnmatchedViewModel.TabType.INVOICES) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Invoices")
                    Text(
                        text = invoiceCount.toString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            icon = { Icon(Icons.Default.Receipt, contentDescription = null) }
        )

        Tab(
            selected = selectedTab == UnmatchedViewModel.TabType.PAYMENTS,
            onClick = { onTabSelected(UnmatchedViewModel.TabType.PAYMENTS) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Payments")
                    Text(
                        text = paymentCount.toString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            icon = { Icon(Icons.Default.Payment, contentDescription = null) }
        )

        Tab(
            selected = selectedTab == UnmatchedViewModel.TabType.DUPLICATES,
            onClick = { onTabSelected(UnmatchedViewModel.TabType.DUPLICATES) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Duplicates")
                    Text(
                        text = duplicateCount.toString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            icon = { Icon(Icons.Default.FileCopy, contentDescription = null) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnmatchedItemCard(
    item: UnmatchedItemEntity,
    type: UnmatchedViewModel.TabType
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val cardColor = when (type) {
        UnmatchedViewModel.TabType.INVOICES -> MaterialTheme.colorScheme.errorContainer
        UnmatchedViewModel.TabType.PAYMENTS -> MaterialTheme.colorScheme.tertiaryContainer
        UnmatchedViewModel.TabType.DUPLICATES -> MaterialTheme.colorScheme.secondaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
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
                Text(
                    text = currencyFormat.format(item.amount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    when (type) {
                        UnmatchedViewModel.TabType.INVOICES -> Icons.Default.Receipt
                        UnmatchedViewModel.TabType.PAYMENTS -> Icons.Default.Payment
                        UnmatchedViewModel.TabType.DUPLICATES -> Icons.Default.FileCopy
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Reference",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = item.reference,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            if (item.merchant != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.merchant,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateFormat.format(Date(item.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.reason != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = item.reason,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UnmatchedListScreenPreview() {
    UPIReconcileProTheme {
        UnmatchedListScreen(
            uiState = UnmatchedViewModel.UnmatchedUiState.Success(
                invoices = List(5) { index ->
                    UnmatchedItemEntity(
                        id = index.toLong(),
                        sessionId = 1,
                        type = "INVOICE",
                        reference = "INV${1000 + index}",
                        amount = 5000.0 + (index * 100),
                        date = System.currentTimeMillis() - (index * 86400000L),
                        merchant = "Merchant ${index + 1}",
                        reason = "No matching payment"
                    )
                },
                payments = List(3) { index ->
                    UnmatchedItemEntity(
                        id = (index + 10).toLong(),
                        sessionId = 1,
                        type = "PAYMENT",
                        reference = "PAY${2000 + index}",
                        amount = 3000.0 + (index * 50),
                        date = System.currentTimeMillis() - (index * 86400000L),
                        merchant = "Merchant ${index + 5}",
                        reason = "No matching invoice"
                    )
                },
                duplicates = List(2) { index ->
                    UnmatchedItemEntity(
                        id = (index + 20).toLong(),
                        sessionId = 1,
                        type = "DUPLICATE",
                        reference = "DUP${3000 + index}",
                        amount = 1000.0,
                        date = System.currentTimeMillis(),
                        merchant = "Merchant ${index + 8}",
                        reason = "Duplicate entry"
                    )
                },
                selectedTab = UnmatchedViewModel.TabType.INVOICES,
                isPro = true
            ),
            onNavigateBack = {},
            onUpgrade = {},
            onTabSelected = {},
            onExport = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProRequiredPreview() {
    UPIReconcileProTheme {
        UnmatchedListScreen(
            uiState = UnmatchedViewModel.UnmatchedUiState.ProRequired(
                "Unmatched items view is a PRO feature. Upgrade to access detailed unmatched analysis."
            ),
            onNavigateBack = {},
            onUpgrade = {},
            onTabSelected = {},
            onExport = {},
            onRefresh = {}
        )
    }
}
