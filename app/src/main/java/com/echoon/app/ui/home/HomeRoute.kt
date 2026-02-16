package com.echoon.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.echoon.app.ui.PendingWriteText

@Composable
fun HomeRoute(
    onNavigateToSee: () -> Unit,
    onNavigateToHear: () -> Unit,
    onNavigateToWrite: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
) {
    val view = LocalView.current
    var sourceLang by remember { mutableStateOf("auto") }
    var targetLang by remember { mutableStateOf("en") }
    var inputText by remember { mutableStateOf("") }

    fun swapLanguages() {
        sourceLang = targetLang.also { targetLang = sourceLang }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            // Header: title centered, menu right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(modifier = Modifier.size(48.dp))
                Text(
                    text = "EchoOn",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(
                    onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                        onNavigateToSettings()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Language selection bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val sourceOptions = listOf("auto", "en", "es", "fr", "hi", "ur")
                val targetOptions = listOf("en", "es", "fr", "hi", "ur")
                LanguageChip(
                    label = labelForCode(sourceLang),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                            sourceLang = sourceOptions[(sourceOptions.indexOf(sourceLang) + 1) % sourceOptions.size]
                        }),
                )
                IconButton(
                    onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                        swapLanguages()
                    },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                LanguageChip(
                    label = labelForCode(targetLang),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                            targetLang = targetOptions[(targetOptions.indexOf(targetLang).let { if (it < 0) 0 else it } + 1) % targetOptions.size]
                        }),
                )
            }

            // Large text input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp),
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { inner ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Enter the text to translate...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                )
                            }
                            inner()
                        }
                    },
                )
            }

            // Translate button (full width, purple)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                            PendingWriteText.initialText = inputText.ifBlank { null }
                            PendingWriteText.sourceLang = sourceLang
                            PendingWriteText.targetLang = targetLang
                            onNavigateToWrite()
                        }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Translate",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            // 2x2 grid: Voice, Conversation, Image, Camera
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ModeGridButton(
                    icon = Icons.Default.Mic,
                    label = "Voice",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                        onNavigateToHear()
                    },
                )
                ModeGridButton(
                    icon = Icons.Default.Chat,
                    label = "Conversation",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                        PendingWriteText.initialText = inputText.ifBlank { null }
                        PendingWriteText.sourceLang = sourceLang
                        PendingWriteText.targetLang = targetLang
                        onNavigateToWrite()
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ModeGridButton(
                    icon = Icons.Default.Image,
                    label = "Image",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                        onNavigateToSee()
                    },
                )
                ModeGridButton(
                    icon = Icons.Default.CameraAlt,
                    label = "Camera",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                        onNavigateToSee()
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                TextButton(
                    onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                        onNavigateToHistory()
                    },
                ) {
                    Text(
                        text = "Recent translations",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

private fun labelForCode(code: String): String = when (code) {
    "auto" -> "Auto"
    "en" -> "English"
    "es" -> "Spanish"
    "fr" -> "French"
    "hi" -> "Hindi"
    "ur" -> "Urdu"
    else -> code.uppercase()
}

@Composable
private fun LanguageChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ModeGridButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
