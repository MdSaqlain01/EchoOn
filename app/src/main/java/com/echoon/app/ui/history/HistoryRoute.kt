package com.echoon.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Scaffold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.echoon.app.ui.FloatingBlob
import com.echoon.app.ui.glassBorder
import com.echoon.app.ui.theme.luminance
import com.echoon.app.services.SupabaseHistoryService
import com.echoon.app.services.TranslationHistoryEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HistoryRoute(onBack: () -> Unit) {
    val service = remember { SupabaseHistoryService() }
    val scope = rememberCoroutineScope()
    val itemsState = remember { mutableStateOf<List<TranslationHistoryEntry>>(emptyList()) }
    val isLoadingState = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoadingState.value = true
        scope.launch {
            val items = service.getRecentTranslations(limit = 50)
            itemsState.value = items
            isLoadingState.value = false
        }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgGradient = Brush.verticalGradient(
        listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background),
    )
    val glassWhite = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    val glassCard = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(bgGradient),
    ) {
        FloatingBlob(
            color = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFFDCE775),
            alpha = if (isDark) 0.2f else 0.3f,
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
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
                        text = "Recent translations",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Box(modifier = Modifier.size(40.dp))
                }
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (isLoadingState.value && itemsState.value.isEmpty()) {
                    item {
                        Text(
                            text = "Loading history…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }

                if (!isLoadingState.value && itemsState.value.isEmpty()) {
                    item {
                        Text(
                            text = "No translations yet. Try using See, Hear, or Write.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }

                itemsIndexed(itemsState.value) { index, entry ->
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(index * 150L)
                        isVisible = true
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(600)) + slideInVertically(
                            initialOffsetY = { 100 },
                            animationSpec = tween(600),
                        ),
                    ) {
                        HistoryItem(entry = entry, glassCard = glassCard)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(entry: TranslationHistoryEntry, glassCard: Color) {
    val shape = RoundedCornerShape(20.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassBorder(shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = glassCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = entry.mode.uppercase() + "  •  " +
                    "${entry.sourceLang} → ${entry.targetLang}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = entry.sourceText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = entry.translatedText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

