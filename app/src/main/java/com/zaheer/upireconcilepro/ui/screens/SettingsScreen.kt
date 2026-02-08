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
import com.zaheer.upireconcilepro.ui.components.LoadingDialog
import com.zaheer.upireconcilepro.ui.components.ErrorDialog
import com.zaheer.upireconcilepro.ui.components.ProFeatureBadge
import com.zaheer.upireconcilepro.ui.theme.UPIReconcileProTheme
import com.zaheer.upireconcilepro.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsViewModel.SettingsUiState,
    onNavigateBack: () -> Unit,
    onAppLockToggle: (Boolean) -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    onDateToleranceChange: (Int) -> Unit,
    onUpgrade: () -> Unit,
    onAbout: () -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is SettingsViewModel.SettingsUiState.Loading -> {
                LoadingDialog(
                    message = "Loading settings...",
                    onDismiss = {}
                )
            }
            
            is SettingsViewModel.SettingsUiState.Success -> {
                SettingsContent(
                    isPro = uiState.isPro,
                    remainingDays = uiState.remainingDays,
                    settings = uiState.settings,
                    onAppLockToggle = onAppLockToggle,
                    onBiometricToggle = onBiometricToggle,
                    onDateToleranceChange = onDateToleranceChange,
                    onUpgrade = onUpgrade,
                    onAbout = onAbout,
                    modifier = Modifier.padding(padding)
                )
            }
            
            is SettingsViewModel.SettingsUiState.Error -> {
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
private fun SettingsContent(
    isPro: Boolean,
    remainingDays: Long?,
    settings: SettingsViewModel.AppSettings,
    onAppLockToggle: (Boolean) -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    onDateToleranceChange: (Int) -> Unit,
    onUpgrade: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ProStatusCard(
            isPro = isPro,
            remainingDays = remainingDays,
            onUpgrade = onUpgrade
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Security",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingItem(
            icon = Icons.Default.Lock,
            title = "App Lock",
            description = "Require PIN to open the app",
            trailing = {
                Switch(
                    checked = settings.appLockEnabled,
                    onCheckedChange = onAppLockToggle
                )
            }
        )

        if (settings.appLockEnabled) {
            SettingItem(
                icon = Icons.Default.Fingerprint,
                title = "Biometric Authentication",
                description = "Use fingerprint or face unlock",
                trailing = {
                    Switch(
                        checked = settings.biometricEnabled,
                        onCheckedChange = onBiometricToggle
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Reconciliation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        DateToleranceSetting(
            currentValue = settings.dateToleranceDays,
            onValueChange = onDateToleranceChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Theme",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingItem(
            icon = Icons.Default.Palette,
            title = "Theme Mode",
            description = "Current: ${settings.themeMode.name}",
            onClick = { /* TODO: Theme selector */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingItem(
            icon = Icons.Default.Info,
            title = "About UPI Reconcile Pro",
            description = "Version 1.0.0",
            onClick = onAbout
        )

        SettingItem(
            icon = Icons.Default.Policy,
            title = "Privacy Policy",
            description = "View our privacy policy",
            onClick = { /* TODO */ }
        )

        SettingItem(
            icon = Icons.Default.Description,
            title = "Terms of Service",
            description = "View terms and conditions",
            onClick = { /* TODO */ }
        )

        SettingItem(
            icon = Icons.Default.Email,
            title = "Contact Support",
            description = "Get help and support",
            onClick = { /* TODO */ }
        )
    }
}

@Composable
private fun ProStatusCard(
    isPro: Boolean,
    remainingDays: Long?,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPro) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isPro) "PRO Subscriber" else "FREE Plan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (isPro) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Stars,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (isPro) {
                        remainingDays?.let { "Renews in $it days" } ?: "Active subscription"
                    } else {
                        "Limited features"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (!isPro) {
                Button(onClick = onUpgrade) {
                    Text("Upgrade")
                }
            }
        }
    }
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun DateToleranceSetting(
    currentValue: Int,
    onValueChange: (Int) -> Unit
) {
    var sliderValue by remember(currentValue) { mutableFloatStateOf(currentValue.toFloat()) }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Date Tolerance",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Allow ±${currentValue} days for matching",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "0",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = {
                        onValueChange(sliderValue.toInt())
                    },
                    valueRange = 0f..7f,
                    steps = 6,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                )
                
                Text(
                    text = "7",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Currently set to ${currentValue} days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    UPIReconcileProTheme {
        SettingsScreen(
            uiState = SettingsViewModel.SettingsUiState.Success(
                isPro = false,
                remainingDays = null,
                settings = SettingsViewModel.AppSettings(
                    appLockEnabled = true,
                    biometricEnabled = true,
                    dateToleranceDays = 2,
                    autoBackupEnabled = false,
                    themeMode = SettingsViewModel.ThemeMode.SYSTEM
                )
            ),
            onNavigateBack = {},
            onAppLockToggle = {},
            onBiometricToggle = {},
            onDateToleranceChange = {},
            onUpgrade = {},
            onAbout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenProPreview() {
    UPIReconcileProTheme {
        SettingsScreen(
            uiState = SettingsViewModel.SettingsUiState.Success(
                isPro = true,
                remainingDays = 25,
                settings = SettingsViewModel.AppSettings(
                    appLockEnabled = false,
                    biometricEnabled = false,
                    dateToleranceDays = 3,
                    autoBackupEnabled = true,
                    themeMode = SettingsViewModel.ThemeMode.DARK
                )
            ),
            onNavigateBack = {},
            onAppLockToggle = {},
            onBiometricToggle = {},
            onDateToleranceChange = {},
            onUpgrade = {},
            onAbout = {}
        )
    }
}
