package com.zaheer.upireconcilepro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaheer.upireconcilepro.ui.theme.UPIReconcileProTheme

@Composable
fun ProFeatureBadge(
    modifier: Modifier = Modifier,
    text: String = "PRO",
    showIcon: Boolean = true
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFFA500)  // Orange
        )
    )
    
    Row(
        modifier = modifier
            .background(
                brush = gradient,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showIcon) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProFeatureBadgePreview() {
    UPIReconcileProTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProFeatureBadge()
                ProFeatureBadge(showIcon = false)
                ProFeatureBadge(text = "PREMIUM", showIcon = true)
                
                // Example in context
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Advanced Analytics")
                        ProFeatureBadge()
                    }
                }
            }
        }
    }
}
