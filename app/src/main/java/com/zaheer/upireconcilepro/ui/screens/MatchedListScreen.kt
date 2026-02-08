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
import com.zaheer.upireconcilepro.data.database.entity.MatchedItemEntity
import com.zaheer.upireconcilepro.ui.components.EmptyStateView
import com.zaheer.upireconcilepro.ui.components.LoadingDialog
import com.zaheer.upireconcilepro.ui.components.ErrorDialog
import com.zaheer.upireconcilepro.ui.components.ProFeatureBadge
import com.zaheer.upireconcilepro.ui.theme.UPIReconcileProTheme
import com.zaheer.upireconcilepro.viewmodel.MatchedViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchedListScreen(
    uiState: MatchedViewModel.MatchedUiState,
    showUpgradePrompt: Boolean,
    onNavigateBack: () -> Unit,
    onUpgrade: () -> Unit,
    onDismissUpgradePrompt: () -> Unit,
    onExport: () -> Unit,
    onRefresh: () -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Matched Items") },
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
            is MatchedViewModel.MatchedUiState.Loading -> {
                LoadingDialog(
                    message = "Loading matched items...",
                    onDismiss = {}
                )
            }
            
            is MatchedViewModel.MatchedUiState.Success -> {
                MatchedListContent(
                    items = uiState.displayItems,
                    totalCount = uiState.totalCount,
                    isLimited = uiState.isLimited,
                    limitedCount = uiState.limitedCount,
                    isPro = uiState.isPro,
                    showUpgradePrompt = showUpgradePrompt,
                    onUpgrade = onUpgrade,
                    onDismissUpgradePrompt = onDismissUpgradePrompt,
                    modifier = Modifier.padding(padding)
                )
            }
            
            is MatchedViewModel.MatchedUiState.Empty -> {
                EmptyStateView(
                    icon = Icons.Default.Inbox,
                    title = "No Matched Items",
                    message = "No transactions were matched in this reconciliation",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            
            is MatchedViewModel.MatchedUiState.Error -> {
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
private fun MatchedListContent(
    items: List<MatchedItemEntity>,
    totalCount: Int,
    isLimited: Boolean,
    limitedCount: Int?,
    isPro: Boolean,
    showUpgradePrompt: Boolean,
    onUpgrade: () -> Unit,
    onDismissUpgradePrompt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (isLimited && !isPro) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FREE Limit Reached",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Showing $limitedCount of $totalCount items",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(onClick = onUpgrade) {
                        Text("Upgrade")
                    }
                }
            }
        }

        if (!isPro) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "FREE users can view up to 20 matched items. Upgrade to PRO for unlimited access.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                MatchedItemCard(item = item)
            }

            if (isLimited && !isPro) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "${totalCount - limitedCount!!} more items locked",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Upgrade to PRO to view all matched items",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onUpgrade,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Upgrade to PRO")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUpgradePrompt) {
        AlertDialog(
            onDismissRequest = onDismissUpgradePrompt,
            title = { Text("Upgrade to PRO") },
            text = {
                Text("You've reached the FREE tier limit of 20 matched items. Upgrade to PRO to view all ${totalCount} matched items and unlock more features!")
            },
            confirmButton = {
                Button(onClick = {
                    onDismissUpgradePrompt()
                    onUpgrade()
                }) {
                    Text("Upgrade Now")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissUpgradePrompt) {
                    Text("Later")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchedItemCard(item: MatchedItemEntity) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = item.matchType,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                }
            }

            if (item.merchant != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                if (item.confidence < 1.0f) {
                    Text(
                        text = "Confidence: ${(item.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.confidence > 0.8f)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchedListScreenPreview() {
    UPIReconcileProTheme {
        MatchedListScreen(
            uiState = MatchedViewModel.MatchedUiState.Success(
                items = List(25) { index ->
                    MatchedItemEntity(
                        id = index.toLong(),
                        sessionId = 1,
                        reference = "INV${1000 + index}",
                        amount = 5000.0 + (index * 100),
                        date = System.currentTimeMillis() - (index * 86400000L),
                        merchant = "Merchant ${index + 1}",
                        matchType = "EXACT",
                        confidence = 0.95f
                    )
                },
                displayItems = List(20) { index ->
                    MatchedItemEntity(
                        id = index.toLong(),
                        sessionId = 1,
                        reference = "INV${1000 + index}",
                        amount = 5000.0 + (index * 100),
                        date = System.currentTimeMillis() - (index * 86400000L),
                        merchant = "Merchant ${index + 1}",
                        matchType = "EXACT",
                        confidence = 0.95f
                    )
                },
                isPro = false,
                isLimited = true,
                totalCount = 25,
                limitedCount = 20
            ),
            showUpgradePrompt = false,
            onNavigateBack = {},
            onUpgrade = {},
            onDismissUpgradePrompt = {},
            onExport = {},
            onRefresh = {}
        )
    }
}
