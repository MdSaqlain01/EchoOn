package com.echoon.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.echoon.app.preferences.AuthRepository
import com.echoon.app.preferences.ThemeMode
import com.echoon.app.ui.hear.HearRoute
import com.echoon.app.ui.history.HistoryRoute
import com.echoon.app.ui.home.HomeRoute
import com.echoon.app.ui.see.SeeRoute
import com.echoon.app.ui.settings.SettingsRoute
import com.echoon.app.ui.write.WriteRoute

@Composable
fun EchoOnApp(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    authRepository: AuthRepository,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "home",
    ) {
        composable("home") {
            HomeRoute(
                onNavigateToSee = { navController.navigate("see") },
                onNavigateToHear = { navController.navigate("hear") },
                onNavigateToWrite = { navController.navigate("write") },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToSettings = { navController.navigate("settings") },
            )
        }
        composable("see") { SeeRoute(onBack = { navController.popBackStack() }) }
        composable("hear") { HearRoute(onBack = { navController.popBackStack() }) }
        composable("write") { WriteRoute(onBack = { navController.popBackStack() }) }
        composable("history") { HistoryRoute(onBack = { navController.popBackStack() }) }
        composable("settings") {
            SettingsRoute(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                authRepository = authRepository,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
