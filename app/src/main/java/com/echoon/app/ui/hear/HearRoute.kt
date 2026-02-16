package com.echoon.app.ui.hear

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import com.echoon.app.ui.FloatingBlob
import com.echoon.app.ui.glassBorder
import com.echoon.app.ui.theme.luminance
import com.echoon.app.services.SupabaseHistoryService
import com.echoon.app.services.TranslationService
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.util.Locale

private enum class HearState {
    Idle,
    Listening,
    Thinking,
    Finished,
}

private const val RECORD_AUDIO_PERMISSION_REQUEST = 1001

@Composable
fun HearRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    val translationService = remember { TranslationService() }
    val historyService = remember { SupabaseHistoryService() }

    val hearState = remember { mutableStateOf(HearState.Idle) }
    val recognizedText = remember { mutableStateOf("") }
    val translatedText = remember { mutableStateOf("") }
    val errorState = remember { mutableStateOf<String?>(null) }
    val targetLangState = remember { mutableStateOf("en") }

    // TTS for the Echo
    val tts = remember {
        TextToSpeech(context) { /* handle init status if needed */ }
    }

    // Android SpeechRecognizer instance
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    // Wire up recognition callbacks
    DisposableEffect(speechRecognizer) {
        val recognizer = speechRecognizer
        if (recognizer != null) {
            recognizer.setRecognitionListener(
                object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        hearState.value = HearState.Listening
                        errorState.value = null
                    }

                    override fun onBeginningOfSpeech() {
                        hearState.value = HearState.Listening
                    }

                    override fun onRmsChanged(rmsdB: Float) = Unit
                    override fun onBufferReceived(buffer: ByteArray?) = Unit
                    override fun onEndOfSpeech() {
                        hearState.value = HearState.Thinking
                    }

                    override fun onError(error: Int) {
                        hearState.value = HearState.Idle
                        errorState.value =
                            when (error) {
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                    "Microphone permission denied. Enable it in Settings and try again."
                                SpeechRecognizer.ERROR_NETWORK,
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                                ->
                                    "Network problem while listening. Check your internet and try again."
                                SpeechRecognizer.ERROR_NO_MATCH ->
                                    "I didn't catch that. Please try speaking again."
                                else ->
                                    "Couldn't hear clearly. Please try again."
                            }
                    }

                    override fun onResults(results: Bundle?) {
                        val text = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()

                        if (text.isBlank()) {
                            hearState.value = HearState.Idle
                            errorState.value = "I didn't catch that. Please try again."
                            return
                        }

                        recognizedText.value = text
                        hearState.value = HearState.Thinking
                        errorState.value = null

                        coroutineScope.launch {
                            try {
                                val translated = translationService.translate(
                                    text = text,
                                    source = "auto",
                                    target = targetLangState.value,
                                )
                                translatedText.value = translated
                                hearState.value = HearState.Finished

                                // Best-effort: log translation to Supabase history
                                historyService.logTranslation(
                                    mode = "hear",
                                    sourceLang = "auto",
                                    targetLang = targetLangState.value,
                                    sourceText = text,
                                    translatedText = translated,
                                )
                            } catch (e: Exception) {
                                hearState.value = HearState.Idle
                                errorState.value =
                                    "Couldn't translate right now. Check your internet and try again."
                            }
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) = Unit
                    override fun onEvent(eventType: Int, params: Bundle?) = Unit
                },
            )
        }

        onDispose {
            tts.stop()
            tts.shutdown()
            recognizer?.destroy()
        }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgGradient = Brush.verticalGradient(
        listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background),
    )
    val glassWhite = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)

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
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Text(
                    text = "Voice translate",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = "Let EchoOn listen, translate, and speak for you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassBorder(RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = glassWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        val statusText = when (hearState.value) {
                            HearState.Idle -> "Ready to listen"
                            HearState.Listening -> "Listening…"
                            HearState.Thinking -> "Translating…"
                            HearState.Finished -> "Translation ready"
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    if (hearState.value == HearState.Listening) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                    },
                                ),
                        ) {
                            IconButton(
                                onClick = {
                                    view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                                    if (speechRecognizer == null) {
                                        errorState.value = "Speech recognition is not available on this device."
                                        return@IconButton
                                    }
                                    val hasPermission =
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO,
                                        ) == PackageManager.PERMISSION_GRANTED
                                    if (!hasPermission) {
                                        val activity = context as? Activity
                                        if (activity != null) {
                                            ActivityCompat.requestPermissions(
                                                activity,
                                                arrayOf(Manifest.permission.RECORD_AUDIO),
                                                RECORD_AUDIO_PERMISSION_REQUEST,
                                            )
                                            errorState.value = "Please allow microphone access, then tap the mic again."
                                        } else {
                                            errorState.value = "Microphone permission is required for voice translation."
                                        }
                                        return@IconButton
                                    }
                                    if (hearState.value == HearState.Listening) {
                                        speechRecognizer.stopListening()
                                        hearState.value = HearState.Idle
                                    } else {
                                        recognizedText.value = ""
                                        translatedText.value = ""
                                        errorState.value = null
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                                        }
                                        speechRecognizer.startListening(intent)
                                    }
                                },
                            ) {
                                val icon = when (hearState.value) {
                                    HearState.Listening -> Icons.Filled.Stop
                                    HearState.Thinking -> Icons.Filled.GraphicEq
                                    else -> Icons.Filled.Mic
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Mic",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(40.dp),
                                )
                            }
                        }

                        Text(
                            text = "Speak and translate to:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        RowWithLangChips(
                            targetLangState = targetLangState.value,
                            onLangSelected = { code ->
                                targetLangState.value = code
                                if (recognizedText.value.isNotBlank()) {
                                    hearState.value = HearState.Thinking
                                    errorState.value = null
                                    coroutineScope.launch {
                                        try {
                                            val translated = translationService.translate(
                                                text = recognizedText.value,
                                                source = "auto",
                                                target = code,
                                            )
                                            translatedText.value = translated
                                            hearState.value = HearState.Finished
                                            historyService.logTranslation(
                                                mode = "hear",
                                                sourceLang = "auto",
                                                targetLang = code,
                                                sourceText = recognizedText.value,
                                                translatedText = translated,
                                            )
                                        } catch (e: Exception) {
                                            hearState.value = HearState.Idle
                                            errorState.value = "Couldn't translate right now. Check your internet and try again."
                                        }
                                    }
                                }
                            },
                        )
                    }
                }

                if (errorState.value != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorState.value ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }

                if (recognizedText.value.isNotBlank() || translatedText.value.isNotBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (recognizedText.value.isNotBlank()) {
                                Text(
                                    text = "You said",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = recognizedText.value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            if (translatedText.value.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "EchoOn will say",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = translatedText.value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                TextButton(
                                    onClick = {
                                        view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                                        tts.language = Locale.forLanguageTag(targetLangState.value)
                                        tts.speak(translatedText.value, TextToSpeech.QUEUE_FLUSH, null, "echoon-hear-tts")
                                    },
                                    modifier = Modifier.padding(top = 4.dp),
                                ) {
                                    Text("Play translation")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowWithLangChips(
    targetLangState: String,
    onLangSelected: (String) -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LangChip(
            label = "English",
            code = "en",
            selectedCode = targetLangState,
            onSelected = onLangSelected,
        )
        LangChip(
            label = "Spanish",
            code = "es",
            selectedCode = targetLangState,
            onSelected = onLangSelected,
        )
        LangChip(
            label = "French",
            code = "fr",
            selectedCode = targetLangState,
            onSelected = onLangSelected,
        )
        LangChip(
            label = "Hindi",
            code = "hi",
            selectedCode = targetLangState,
            onSelected = onLangSelected,
        )
        LangChip(
            label = "Urdu",
            code = "ur",
            selectedCode = targetLangState,
            onSelected = onLangSelected,
        )
    }
}

@Composable
private fun LangChip(
    label: String,
    code: String,
    selectedCode: String,
    onSelected: (String) -> Unit,
) {
    TextButton(
        onClick = { onSelected(code) },
    ) {
        Text(
            text = label,
            color =
                if (selectedCode == code) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}

