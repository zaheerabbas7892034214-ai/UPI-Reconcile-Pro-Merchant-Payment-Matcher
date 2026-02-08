package com.zaheer.upireconcilepro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

/**
 * Navigation routes for the app
 */
object NavRoutes {
    const val HOME = "home"
    const val FILE_UPLOAD = "file_upload"
    const val COLUMN_MAPPING = "column_mapping/{sessionId}"
    const val RECONCILIATION_PROGRESS = "reconciliation_progress/{sessionId}"
    const val RESULTS_DASHBOARD = "results_dashboard/{sessionId}"
    const val MATCHED_ITEMS = "matched_items/{sessionId}"
    const val UNMATCHED_ITEMS = "unmatched_items/{sessionId}"
    const val DISCREPANCY_DETAILS = "discrepancy_details/{sessionId}/{itemId}/{itemType}"
    const val EXPORT_OPTIONS = "export_options/{sessionId}"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    
    // Helper functions to create routes with arguments
    fun columnMapping(sessionId: Long) = "column_mapping/$sessionId"
    fun reconciliationProgress(sessionId: Long) = "reconciliation_progress/$sessionId"
    fun resultsDashboard(sessionId: Long) = "results_dashboard/$sessionId"
    fun matchedItems(sessionId: Long) = "matched_items/$sessionId"
    fun unmatchedItems(sessionId: Long) = "unmatched_items/$sessionId"
    fun discrepancyDetails(sessionId: Long, itemId: Long, itemType: String) = 
        "discrepancy_details/$sessionId/$itemId/$itemType"
    fun exportOptions(sessionId: Long) = "export_options/$sessionId"
}

/**
 * Deep link configuration
 */
object DeepLinks {
    const val SCHEME = "upireconcilepro"
    const val HOST = "app"
    
    fun columnMapping(sessionId: Long) = "$SCHEME://$HOST/column_mapping/$sessionId"
    fun reconciliationProgress(sessionId: Long) = "$SCHEME://$HOST/reconciliation_progress/$sessionId"
    fun resultsDashboard(sessionId: Long) = "$SCHEME://$HOST/results_dashboard/$sessionId"
    fun matchedItems(sessionId: Long) = "$SCHEME://$HOST/matched_items/$sessionId"
    fun unmatchedItems(sessionId: Long) = "$SCHEME://$HOST/unmatched_items/$sessionId"
    fun discrepancyDetails(sessionId: Long, itemId: Long, itemType: String) = 
        "$SCHEME://$HOST/discrepancy_details/$sessionId/$itemId/$itemType"
    fun exportOptions(sessionId: Long) = "$SCHEME://$HOST/export_options/$sessionId"
}

/**
 * Main navigation graph for UPI Reconcile Pro
 * 
 * Screens:
 * 1. Home - Dashboard with recent sessions and quick actions
 * 2. File Upload - Upload bank and merchant CSV files
 * 3. Column Mapping - Map CSV columns to required fields
 * 4. Reconciliation Progress - Real-time progress of reconciliation
 * 5. Results Dashboard - Overview of reconciliation results
 * 6. Matched Items - List of successfully matched transactions
 * 7. Unmatched Items - List of unmatched transactions requiring attention
 * 8. Discrepancy Details - Detailed view of a specific discrepancy
 * 9. Export Options - Export results in various formats (CSV, Excel, PDF)
 * 10. History - View past reconciliation sessions
 * 11. Settings - App settings including pro features
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavRoutes.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 1. Home Screen
        composable(
            route = NavRoutes.HOME,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${DeepLinks.SCHEME}://${DeepLinks.HOST}/home" }
            )
        ) {
            // HomeScreen implementation
            HomeScreenPlaceholder(
                onNavigateToFileUpload = { navController.navigate(NavRoutes.FILE_UPLOAD) },
                onNavigateToHistory = { navController.navigate(NavRoutes.HISTORY) },
                onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) },
                onNavigateToSession = { sessionId ->
                    navController.navigate(NavRoutes.resultsDashboard(sessionId))
                }
            )
        }
        
        // 2. File Upload Screen
        composable(
            route = NavRoutes.FILE_UPLOAD,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${DeepLinks.SCHEME}://${DeepLinks.HOST}/file_upload" }
            )
        ) {
            // FileUploadScreen implementation
            FileUploadScreenPlaceholder(
                onNavigateBack = { navController.popBackStack() },
                onFilesUploaded = { sessionId ->
                    navController.navigate(NavRoutes.columnMapping(sessionId)) {
                        popUpTo(NavRoutes.FILE_UPLOAD) { inclusive = true }
                    }
                }
            )
        }
        
        // 3. Column Mapping Screen
        composable(
            route = NavRoutes.COLUMN_MAPPING,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType }
            ),
            deepLinks = listOf(
                navDeepLink { 
                    uriPattern = "${DeepLinks.SCHEME}://${DeepLinks.HOST}/column_mapping/{sessionId}"
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            
            // ColumnMappingScreen implementation
            ColumnMappingScreenPlaceholder(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() },
                onMappingComplete = {
                    navController.navigate(NavRoutes.reconciliationProgress(sessionId)) {
                        popUpTo(NavRoutes.COLUMN_MAPPING) { inclusive = true }
                    }
                }
            )
        }
        
        // 4. Reconciliation Progress Screen
        composable(
            route = NavRoutes.RECONCILIATION_PROGRESS,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType }
            ),
            deepLinks = listOf(
                navDeepLink { 
                    uriPattern = "${DeepLinks.SCHEME}://${DeepLinks.HOST}/reconciliation_progress/{sessionId}"
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            
            // ReconciliationProgressScreen implementation
            ReconciliationProgressScreenPlaceholder(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() },
                onReconciliationComplete = {
                    navController.navigate(NavRoutes.resultsDashboard(sessionId)) {
                        popUpTo(NavRoutes.RECONCILIATION_PROGRESS) { inclusive = true }
                    }
                }
            )
        }
        
        // 5. Results Dashboard Screen
        composable(
            route = NavRoutes.RESULTS_DASHBOARD,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType }
            ),
            deepLinks = listOf(
                navDeepLink { 
                    uriPattern = "${DeepLinks.SCHEME}://${DeepLinks.HOST}/results_dashboard/{sessionId}"
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            
            // ResultsDashboardScreen implementation
            ResultsDashboardScreenPlaceholder(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMatched = { navController.navigate(NavRoutes.matchedItems(sessionId)) },
                onNavigateToUnmatched = { navController.navigate(NavRoutes.unmatchedItems(sessionId)) },
                onNavigateToExport = { navController.navigate(NavRoutes.exportOptions(sessionId)) }
            )
        }
        
        // 6. Matched Items Screen
        composable(
            route = NavRoutes.MATCHED_ITEMS,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType }
            ),
            deepLinks = listOf(
                navDeepLink { 
                    uriPattern = "${DeepLinks.SCHEME}://${DeepLinks.HOST}/matched_items/{sessionId}"
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            
            // MatchedItemsScreen implementation
            MatchedItemsScreenPlaceholder(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() },
                onItemClick = { itemId ->
                    navController.navigate(
                        NavRoutes.discrepancyDetails(sessionId, itemId, "matched")
                    )
                }
            )
        }
        
        // 7. Unmatched Items Screen
        composable(
            route = NavRoutes.UNMATCHED_ITEMS,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType }
            ),
            deepLinks = listOf(
                navDeepLink { 
                    uriPattern = "${DeepLinks.SCHEME}://${DeepLinks.HOST}/unmatched_items/{sessionId}"
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            
            // UnmatchedItemsScreen implementation
            UnmatchedItemsScreenPlaceholder(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() },
                onItemClick = { itemId ->
                    navController.navigate(
                        NavRoutes.discrepancyDetails(sessionId, itemId, "unmatched")
                    )
                }
            )
        }
        
        // 8. Discrepancy Details Screen
        composable(
            route = NavRoutes.DISCREPANCY_DETAILS,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType },
                navArgument("itemId") { type = NavType.LongType },
                navArgument("itemType") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { 
                    uriPattern = "${DeepLinks.SCHEME}://${DeepLinks.HOST}/discrepancy_details/{sessionId}/{itemId}/{itemType}"
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
            val itemType = backStackEntry.arguments?.getString("itemType") ?: "matched"
            
            // DiscrepancyDetailsScreen implementation
            DiscrepancyDetailsScreenPlaceholder(
                sessionId = sessionId,
                itemId = itemId,
                itemType = itemType,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // 9. Export Options Screen
        composable(
            route = NavRoutes.EXPORT_OPTIONS,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType }
            ),
            deepLinks = listOf(
                navDeepLink { 
                    uriPattern = "${DeepLinks.SCHEME}://${DeepLinks.HOST}/export_options/{sessionId}"
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            
            // ExportOptionsScreen implementation
            ExportOptionsScreenPlaceholder(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() },
                onExportComplete = { navController.popBackStack() }
            )
        }
        
        // 10. History Screen
        composable(
            route = NavRoutes.HISTORY,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${DeepLinks.SCHEME}://${DeepLinks.HOST}/history" }
            )
        ) {
            // HistoryScreen implementation
            HistoryScreenPlaceholder(
                onNavigateBack = { navController.popBackStack() },
                onSessionClick = { sessionId ->
                    navController.navigate(NavRoutes.resultsDashboard(sessionId))
                }
            )
        }
        
        // 11. Settings Screen
        composable(
            route = NavRoutes.SETTINGS,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${DeepLinks.SCHEME}://${DeepLinks.HOST}/settings" }
            )
        ) {
            // SettingsScreen implementation
            SettingsScreenPlaceholder(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// Placeholder composables - to be replaced with actual screen implementations
@Composable
private fun HomeScreenPlaceholder(
    onNavigateToFileUpload: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSession: (Long) -> Unit
) {
    androidx.compose.material3.Text("Home Screen - To be implemented")
}

@Composable
private fun FileUploadScreenPlaceholder(
    onNavigateBack: () -> Unit,
    onFilesUploaded: (Long) -> Unit
) {
    androidx.compose.material3.Text("File Upload Screen - To be implemented")
}

@Composable
private fun ColumnMappingScreenPlaceholder(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    onMappingComplete: () -> Unit
) {
    androidx.compose.material3.Text("Column Mapping Screen - To be implemented")
}

@Composable
private fun ReconciliationProgressScreenPlaceholder(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    onReconciliationComplete: () -> Unit
) {
    androidx.compose.material3.Text("Reconciliation Progress Screen - To be implemented")
}

@Composable
private fun ResultsDashboardScreenPlaceholder(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToMatched: () -> Unit,
    onNavigateToUnmatched: () -> Unit,
    onNavigateToExport: () -> Unit
) {
    androidx.compose.material3.Text("Results Dashboard Screen - To be implemented")
}

@Composable
private fun MatchedItemsScreenPlaceholder(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    androidx.compose.material3.Text("Matched Items Screen - To be implemented")
}

@Composable
private fun UnmatchedItemsScreenPlaceholder(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    androidx.compose.material3.Text("Unmatched Items Screen - To be implemented")
}

@Composable
private fun DiscrepancyDetailsScreenPlaceholder(
    sessionId: Long,
    itemId: Long,
    itemType: String,
    onNavigateBack: () -> Unit
) {
    androidx.compose.material3.Text("Discrepancy Details Screen - To be implemented")
}

@Composable
private fun ExportOptionsScreenPlaceholder(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    onExportComplete: () -> Unit
) {
    androidx.compose.material3.Text("Export Options Screen - To be implemented")
}

@Composable
private fun HistoryScreenPlaceholder(
    onNavigateBack: () -> Unit,
    onSessionClick: (Long) -> Unit
) {
    androidx.compose.material3.Text("History Screen - To be implemented")
}

@Composable
private fun SettingsScreenPlaceholder(
    onNavigateBack: () -> Unit
) {
    androidx.compose.material3.Text("Settings Screen - To be implemented")
}
