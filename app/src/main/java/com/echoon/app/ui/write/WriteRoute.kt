package com.echoon.app.ui.write

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echoon.app.R
import com.echoon.app.ui.FloatingBlob
import com.echoon.app.ui.PendingWriteText
import com.echoon.app.ui.glassBorder
import com.echoon.app.ui.theme.luminance
import com.echoon.app.services.SupabaseHistoryService
import com.echoon.app.services.TranslationService
import kotlinx.coroutines.launch
import java.util.Locale

private data class LocalHistoryItem(
    val id: Long,
    val sourceLang: String,
    val targetLang: String,
    val sourceText: String,
    val translatedText: String,
)

@Composable
fun WriteRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val translationService = remember { TranslationService() }
    val historyService = remember { SupabaseHistoryService() }

    val inputTextState = remember { mutableStateOf("") }
    val translatedTextState = remember { mutableStateOf("") }
    val sourceLangState = remember { mutableStateOf("auto") }
    val targetLangState = remember { mutableStateOf("en") }

    LaunchedEffect(Unit) {
        PendingWriteText.initialText?.let { inputTextState.value = it }
        PendingWriteText.sourceLang?.let { sourceLangState.value = it }
        PendingWriteText.targetLang?.let { targetLangState.value = it }
        PendingWriteText.initialText = null
        PendingWriteText.sourceLang = null
        PendingWriteText.targetLang = null
    }
    val isTranslatingState = remember { mutableStateOf(false) }
    val errorState = remember { mutableStateOf<String?>(null) }

    val localHistory = remember { mutableStateListOf<LocalHistoryItem>() }

    val tts = remember {
        TextToSpeech(context) { /* init status if needed */ }
    }
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgGradient = Brush.verticalGradient(
        listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background),
    )
    val glassWhite = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    val accentColor = MaterialTheme.colorScheme.onSurface

    fun triggerTranslate() {
        val text = inputTextState.value.trim()
        if (text.isEmpty()) {
            errorState.value = "Please enter some text first."
            return
        }
        isTranslatingState.value = true
        errorState.value = null

        scope.launch {
            try {
                val sourceCode = sourceLangState.value.ifBlank { "auto" }
                val targetCode = targetLangState.value.ifBlank { "en" }

                val translated = translationService.translate(
                    text = text,
                    source = sourceCode,
                    target = targetCode,
                )
                translatedTextState.value = translated

                historyService.logTranslation(
                    mode = "write",
                    sourceLang = sourceCode,
                    targetLang = targetCode,
                    sourceText = text,
                    translatedText = translated,
                )

                localHistory.add(
                    0,
                    LocalHistoryItem(
                        id = System.currentTimeMillis(),
                        sourceLang = sourceCode,
                        targetLang = targetCode,
                        sourceText = text,
                        translatedText = translated,
                    ),
                )
                if (localHistory.size > 10) {
                    localHistory.removeLast()
                }
            } catch (e: Exception) {
                errorState.value =
                    "Couldn’t translate right now. Check your internet and try again."
            } finally {
                isTranslatingState.value = false
            }
        }
    }

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
                    IconButton(
                        onClick = { /* profile/settings later */ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(glassWhite, CircleShape),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_see),
                            contentDescription = "Profile",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 24.dp),
            ) {
                Text(
                    "Translate AI",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    "Smart interpretation at your fingertips",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // glass main card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassBorder(RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = glassWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LanguageNode(
                                label = "From",
                                name = labelForLang(sourceLangState.value),
                                flag = flagForLang(sourceLangState.value),
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                            LanguageNode(
                                label = "To",
                                name = labelForLang(targetLangState.value),
                                flag = flagForLang(targetLangState.value),
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                        LanguageChip("English", isSelected = targetLangState.value == "en") {
                            targetLangState.value = "en"
                            if (inputTextState.value.isNotBlank()) {
                                triggerTranslate()
                            }
                        }
                        LanguageChip("Spanish", isSelected = targetLangState.value == "es") {
                            targetLangState.value = "es"
                            if (inputTextState.value.isNotBlank()) {
                                triggerTranslate()
                            }
                        }
                        LanguageChip("French", isSelected = targetLangState.value == "fr") {
                            targetLangState.value = "fr"
                            if (inputTextState.value.isNotBlank()) {
                                triggerTranslate()
                            }
                        }
                        LanguageChip("Hindi", isSelected = targetLangState.value == "hi") {
                            targetLangState.value = "hi"
                            if (inputTextState.value.isNotBlank()) {
                                triggerTranslate()
                            }
                        }
                        LanguageChip("Urdu", isSelected = targetLangState.value == "ur") {
                            targetLangState.value = "ur"
                            if (inputTextState.value.isNotBlank()) {
                                triggerTranslate()
                            }
                        }
                        }

                        TextField(
                            value = inputTextState.value,
                            onValueChange = {
                                inputTextState.value = it
                                errorState.value = null
                            },
                            placeholder = {
                                Text("Start typing...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )

                        if (errorState.value != null) {
                            Text(
                                text = errorState.value!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ToolIcon(R.drawable.ic_see)
                                ToolIcon(R.drawable.ic_hear)
                            }
                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    view.performHapticFeedback(
                                        android.view.HapticFeedbackConstants.CLOCK_TICK,
                                    )
                                    if (!isTranslatingState.value) {
                                        triggerTranslate()
                                    }
                                },
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(accentColor, CircleShape),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = "Translate",
                                    tint = Color.White,
                                )
                            }
                        }
                    }
                }

                if (translatedTextState.value.isNotBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Translation",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                IconButton(
                                    onClick = {
                                        view.performHapticFeedback(
                                            android.view.HapticFeedbackConstants.CLOCK_TICK,
                                        )
                                        val localeTag =
                                            if (targetLangState.value == "auto") {
                                                "en"
                                            } else {
                                                targetLangState.value
                                            }
                                        tts.language = Locale.forLanguageTag(localeTag)
                                        tts.speak(
                                            translatedTextState.value,
                                            TextToSpeech.QUEUE_FLUSH,
                                            null,
                                            "echoon-tts",
                                        )
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_hear),
                                        contentDescription = "Play",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Text(
                                text = translatedTextState.value,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                if (localHistory.isNotEmpty()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "History",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                        TextButton(onClick = { /* could open full history */ }) {
                            Text("See all", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(localHistory) { item ->
                            HistoryItemCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageNode(label: String, name: String, flag: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(flag, fontSize = 18.sp)
            Spacer(Modifier.size(8.dp))
            Text(name, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ToolIcon(res: Int) {
    Box(
        Modifier
            .size(40.dp)
            .background(Color(0xFFF5F5F5), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(res),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HistoryItemCard(item: LocalHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        ),
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .background(Color(0xFFE8F5E9), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    flagForLang(item.targetLang),
                    fontSize = 14.sp,
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    "${item.sourceLang} → ${item.targetLang}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    item.sourceText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    item.translatedText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LanguageChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = label,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

private fun labelForLang(code: String): String =
    when (code) {
        "auto" -> "Auto-detect"
        "en" -> "English"
        "es" -> "Spanish"
        "fr" -> "French"
        "de" -> "German"
        "hi" -> "Hindi"
        "ur" -> "Urdu"
        "zh" -> "Chinese"
        else -> code.uppercase()
    }

private fun flagForLang(code: String): String =
    when (code) {
        "en" -> "🇺🇸"
        "es" -> "🇪🇸"
        "fr" -> "🇫🇷"
        "hi" -> "🇮🇳"
        "ur" -> "🇵🇰"
        "de" -> "🇩🇪"
        "zh" -> "🇨🇳"
        else -> "🌐"
    }
