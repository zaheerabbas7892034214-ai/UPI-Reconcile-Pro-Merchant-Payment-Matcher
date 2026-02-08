package com.zaheer.upireconcilepro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaheer.upireconcilepro.ui.theme.UPIReconcileProTheme

@Composable
fun EmptyStateView(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onActionClick,
                modifier = Modifier.height(48.dp)
            ) {
                Text(text = actionText)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewPreview() {
    UPIReconcileProTheme {
        Surface {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                EmptyStateView(
                    icon = Icons.Default.Inbox,
                    message = "No reconciliation sessions found.\nStart a new session to begin.",
                    actionText = "Start New Session",
                    onActionClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateViewNoActionPreview() {
    UPIReconcileProTheme {
        Surface {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                EmptyStateView(
                    icon = Icons.Default.Inbox,
                    message = "No data available"
                )
            }
        }
    }
}
