package com.echoon.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// EchoOn brand: soft blue primary, pastel backgrounds
private val Primary = Color(0xFF2F6BFF)
private val PrimaryDark = Color(0xFFBB86FC)  // Purple accent for dark mode (Capi-style)
private val OnPrimary = Color.White
private val SurfaceLight = Color(0xFFF7F9FF)
private val SurfaceDark = Color(0xFF121212)
private val OnSurfaceLight = Color(0xFF000000)       // Pure black for maximum readability
private val OnSurfaceVariantLight = Color(0xFF1A1A1A) // Very dark gray for secondary text
private val OnSurfaceDark = Color(0xFFF5F5F7)       // Light text on dark surfaces
private val OnSurfaceVariantDark = Color(0xFFCACACE) // Slightly muted light gray for secondary

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    surface = SurfaceLight,
    background = SurfaceLight,
    surfaceVariant = Color(0xFFE4ECFF),
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimary,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Color(0xFF1E1E2E),
    onSurfaceVariant = OnSurfaceVariantDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
)

@Composable
fun EchoOnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val ctx = view.context
            if (ctx is Activity) {
                val window = ctx.window
                window.statusBarColor = colorScheme.surface.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
