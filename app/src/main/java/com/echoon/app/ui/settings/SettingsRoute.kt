package com.echoon.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.echoon.app.preferences.AuthRepository
import com.echoon.app.ui.theme.luminance
import com.echoon.app.preferences.ThemeMode
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    authRepository: AuthRepository,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val glassWhite = if (isDark) Color(0xFF1E1E2E).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            listOf(Color(0xFF0D0D14), Color(0xFF050816)),
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFF7F9E8), Color(0xFFFFFFFF)),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(bgGradient),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(glassWhite, CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
            ) {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                )
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeModeChange(mode) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = when (mode) {
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                                ThemeMode.SYSTEM -> "System default"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        RadioButton(
                            selected = themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                        )
                    }
                }

                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { authRepository.logout() }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Log out",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
