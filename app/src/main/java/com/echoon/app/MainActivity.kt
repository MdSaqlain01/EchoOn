package com.echoon.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import com.echoon.app.preferences.AuthRepository
import com.echoon.app.preferences.ThemeMode
import com.echoon.app.preferences.ThemePreferences
import com.echoon.app.ui.EchoOnApp
import com.echoon.app.ui.login.LoginRoute
import com.echoon.app.ui.theme.EchoOnTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themePreferences = ThemePreferences(applicationContext)
        val authRepository = AuthRepository(applicationContext)
        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
            var sessionUsername by remember { mutableStateOf<String?>(null) }
            var authChecked by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(themePreferences) {
                themePreferences.themeModeFlow
                    .catch { _ -> themeMode = ThemeMode.SYSTEM }
                    .collect { themeMode = it }
            }
            LaunchedEffect(authRepository) {
                authRepository.sessionState
                    .catch { _ -> /* show login on error */ }
                    .collect { sessionUsername = it; authChecked = true }
            }
            // Fallback: if session flow never emits, show login after 2s so app doesn't stay on spinner
            LaunchedEffect(Unit) {
                delay(2000)
                authChecked = true
            }
            val onThemeModeChange: (ThemeMode) -> Unit = { mode ->
                themeMode = mode
                scope.launch { themePreferences.setThemeMode(mode) }
            }
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            EchoOnTheme(darkTheme = darkTheme) {
                if (!authChecked) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                    )
                } else if (sessionUsername == null) {
                    LoginRoute(
                        authRepository = authRepository,
                        onLoginSuccess = { /* sessionState flow will update */ },
                    )
                } else {
                    EchoOnApp(
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                        authRepository = authRepository,
                    )
                }
            }
        }
    }
}
