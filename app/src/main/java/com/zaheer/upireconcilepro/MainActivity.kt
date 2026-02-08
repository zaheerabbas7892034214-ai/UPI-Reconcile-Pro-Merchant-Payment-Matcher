package com.zaheer.upireconcilepro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.zaheer.upireconcilepro.ui.navigation.AppNavigation
import com.zaheer.upireconcilepro.ui.theme.UPIReconcileProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UPIReconcileProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UPIReconcileApp()
                }
            }
        }
    }
}

@Composable
fun UPIReconcileApp() {
    val navController = rememberNavController()
    AppNavigation(navController = navController)
}
