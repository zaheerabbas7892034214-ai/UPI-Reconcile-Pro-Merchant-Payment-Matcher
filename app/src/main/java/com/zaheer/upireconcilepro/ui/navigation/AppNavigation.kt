package com.zaheer.upireconcilepro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zaheer.upireconcilepro.ui.screens.*
import com.zaheer.upireconcilepro.viewmodel.*

/**
 * Navigation routes for the app
 */
object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val UPLOAD = "upload"
    const val PROCESSING = "processing/{sessionId}"
    const val DASHBOARD = "dashboard/{sessionId}"
    const val MATCHED = "matched/{sessionId}"
    const val UNMATCHED = "unmatched/{sessionId}"
    const val EXPORT = "export/{sessionId}"
    const val PAYWALL = "paywall"
    const val SETTINGS = "settings"
    const val APP_LOCK = "app_lock"
    
    fun processing(sessionId: Long) = "processing/$sessionId"
    fun dashboard(sessionId: Long) = "dashboard/$sessionId"
    fun matched(sessionId: Long) = "matched/$sessionId"
    fun unmatched(sessionId: Long) = "unmatched/$sessionId"
    fun export(sessionId: Long) = "export/$sessionId"
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        // Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToAppLock = {
                    navController.navigate(Routes.APP_LOCK) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        
        // App Lock Screen
        composable(Routes.APP_LOCK) {
            AppLockScreen(
                onUnlocked = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.APP_LOCK) { inclusive = true }
                    }
                }
            )
        }
        
        // Home Screen
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel()
            HomeScreen(
                viewModel = viewModel,
                onStartReconciliation = {
                    navController.navigate(Routes.UPLOAD)
                },
                onNavigateToSession = { sessionId ->
                    navController.navigate(Routes.dashboard(sessionId))
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }
        
        // Upload Screen
        composable(Routes.UPLOAD) {
            val viewModel: UploadViewModel = viewModel()
            UploadScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProceed = { sessionId ->
                    navController.navigate(Routes.processing(sessionId)) {
                        popUpTo(Routes.UPLOAD) { inclusive = true }
                    }
                },
                onUpgradeClick = {
                    navController.navigate(Routes.PAYWALL)
                }
            )
        }
        
        // Processing Screen
        composable(
            route = Routes.PROCESSING,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            ProcessingScreen(
                sessionId = sessionId,
                onComplete = {
                    navController.navigate(Routes.dashboard(sessionId)) {
                        popUpTo(Routes.PROCESSING) { inclusive = true }
                    }
                }
            )
        }
        
        // Dashboard Screen
        composable(
            route = Routes.DASHBOARD,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            val viewModel: DashboardViewModel = viewModel()
            DashboardScreen(
                viewModel = viewModel,
                sessionId = sessionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onViewMatched = {
                    navController.navigate(Routes.matched(sessionId))
                },
                onViewUnmatched = {
                    navController.navigate(Routes.unmatched(sessionId))
                },
                onExport = {
                    navController.navigate(Routes.export(sessionId))
                },
                onUpgradeClick = {
                    navController.navigate(Routes.PAYWALL)
                }
            )
        }
        
        // Matched List Screen
        composable(
            route = Routes.MATCHED,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            val viewModel: MatchedViewModel = viewModel()
            MatchedListScreen(
                viewModel = viewModel,
                sessionId = sessionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onUpgradeClick = {
                    navController.navigate(Routes.PAYWALL)
                },
                onExport = {
                    navController.navigate(Routes.export(sessionId))
                }
            )
        }
        
        // Unmatched List Screen
        composable(
            route = Routes.UNMATCHED,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            val viewModel: UnmatchedViewModel = viewModel()
            UnmatchedListScreen(
                viewModel = viewModel,
                sessionId = sessionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onUpgradeClick = {
                    navController.navigate(Routes.PAYWALL)
                },
                onExport = {
                    navController.navigate(Routes.export(sessionId))
                }
            )
        }
        
        // Export Screen
        composable(
            route = Routes.EXPORT,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            ExportScreen(
                sessionId = sessionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onUpgradeClick = {
                    navController.navigate(Routes.PAYWALL)
                }
            )
        }
        
        // Paywall Screen
        composable(Routes.PAYWALL) {
            val viewModel: PaywallViewModel = viewModel()
            PaywallScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSubscribeSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        // Settings Screen
        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onManageSubscription = {
                    navController.navigate(Routes.PAYWALL)
                }
            )
        }
    }
}
