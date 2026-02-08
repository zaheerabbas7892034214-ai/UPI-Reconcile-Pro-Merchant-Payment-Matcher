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
import com.zaheer.upireconcilepro.data.database.entity.ReconciliationSessionEntity
import com.zaheer.upireconcilepro.ui.components.EmptyStateView
import com.zaheer.upireconcilepro.ui.components.LoadingDialog
import com.zaheer.upireconcilepro.ui.components.ErrorDialog
import com.zaheer.upireconcilepro.ui.theme.UPIReconcileProTheme
import com.zaheer.upireconcilepro.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeViewModel.HomeUiState,
    onStartReconciliation: () -> Unit,
    onSessionClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    onRefresh: () -> Unit,
    onDeleteSession: (Long) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "UPI Reconcile Pro",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState is HomeViewModel.HomeUiState.Success && !uiState.isPro) {
                            Text(
                                text = "FREE Version",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is HomeViewModel.HomeUiState.Success && uiState.canCreateSession) {
                FloatingActionButton(
                    onClick = onStartReconciliation,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Start Reconciliation")
                }
            }
        }
    ) { padding ->
        when (uiState) {
            is HomeViewModel.HomeUiState.Loading -> {
                LoadingDialog(
                    message = "Loading sessions...",
                    onDismiss = {}
                )
            }
            
            is HomeViewModel.HomeUiState.Success -> {
                HomeContent(
                    sessions = uiState.sessions,
                    isPro = uiState.isPro,
                    canCreateSession = uiState.canCreateSession,
                    remainingSessions = uiState.remainingSessions,
                    onSessionClick = onSessionClick,
                    onDeleteSession = { showDeleteDialog = it },
                    onStartReconciliation = onStartReconciliation,
                    modifier = Modifier.padding(padding)
                )
            }
            
            is HomeViewModel.HomeUiState.Error -> {
                errorMessage = uiState.message
            }
        }
    }

    showDeleteDialog?.let { sessionId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Session") },
            text = { Text("Are you sure you want to delete this reconciliation session?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSession(sessionId)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    errorMessage?.let { message ->
        ErrorDialog(
            message = message,
            onDismiss = { errorMessage = null }
        )
    }
}

@Composable
private fun HomeContent(
    sessions: List<ReconciliationSessionEntity>,
    isPro: Boolean,
    canCreateSession: Boolean,
    remainingSessions: Int?,
    onSessionClick: (Long) -> Unit,
    onDeleteSession: (Long) -> Unit,
    onStartReconciliation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!isPro) {
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "FREE Plan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$remainingSessions sessions left",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Upgrade to PRO for unlimited sessions",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Sessions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (sessions.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Inbox,
                title = "No Reconciliation Sessions",
                message = "Start your first reconciliation by tapping the + button below",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    SessionCard(
                        session = session,
                        onClick = { onSessionClick(session.id) },
                        onDelete = { onDeleteSession(session.id) }
                    )
                }
            }
        }

        if (!canCreateSession) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "⚠️ Session limit reached. Upgrade to PRO for unlimited sessions.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionCard(
    session: ReconciliationSessionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(session.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (session.notes.isNotEmpty()) {
                    Text(
                        text = session.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    UPIReconcileProTheme {
        HomeScreen(
            uiState = HomeViewModel.HomeUiState.Success(
                sessions = listOf(
                    ReconciliationSessionEntity(
                        id = 1,
                        name = "January 2024 Reconciliation",
                        createdAt = System.currentTimeMillis(),
                        notes = "Monthly reconciliation"
                    ),
                    ReconciliationSessionEntity(
                        id = 2,
                        name = "Week 1 - January",
                        createdAt = System.currentTimeMillis() - 86400000,
                        notes = ""
                    )
                ),
                isPro = false,
                canCreateSession = true,
                remainingSessions = 3
            ),
            onStartReconciliation = {},
            onSessionClick = {},
            onSettingsClick = {},
            onRefresh = {},
            onDeleteSession = {}
        )
    }
}
