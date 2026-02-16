package com.echoon.app.ui.see

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.echoon.app.ui.FloatingBlob
import com.echoon.app.ui.glassBorder
import com.echoon.app.ui.theme.luminance
import com.echoon.app.services.SupabaseHistoryService
import com.echoon.app.services.TranslationService
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.util.Locale

private const val CAMERA_PERMISSION_REQUEST = 2001

@Composable
fun SeeRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    val translationService = remember { TranslationService() }
    val historyService = remember { SupabaseHistoryService() }
    val textRecognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    val capturedBitmapState = remember { mutableStateOf<Bitmap?>(null) }
    val recognizedTextState = remember { mutableStateOf("") }
    val translatedTextState = remember { mutableStateOf("") }
    val errorState = remember { mutableStateOf<String?>(null) }
    val isProcessingState = remember { mutableStateOf(false) }
    val targetLangState = remember { mutableStateOf("en") }

    val takePictureLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap == null) {
                errorState.value = "No image captured. Try again."
                return@rememberLauncherForActivityResult
            }
            capturedBitmapState.value = bitmap
            recognizedTextState.value = ""
            translatedTextState.value = ""
            errorState.value = null

            coroutineScope.launch {
                try {
                    isProcessingState.value = true
                    val image = InputImage.fromBitmap(bitmap, 0)
                    val result = textRecognizer.process(image).await()
                    val recognized = result.text.trim()
                    if (recognized.isBlank()) {
                        errorState.value = "No readable text found. Try a clearer photo."
                        isProcessingState.value = false
                        return@launch
                    }
                    recognizedTextState.value = recognized

                    val translated = translationService.translate(
                        text = recognized,
                        source = "auto",
                        target = targetLangState.value,
                    )
                    translatedTextState.value = translated

                    // Best-effort: log translation to Supabase history
                    historyService.logTranslation(
                        mode = "see",
                        sourceLang = "auto",
                        targetLang = targetLangState.value,
                        sourceText = recognized,
                        translatedText = translated,
                    )
                } catch (e: Exception) {
                    errorState.value = "Couldn't read or translate this image. Try again."
                } finally {
                    isProcessingState.value = false
                }
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
                    text = "Camera translate",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = "Point, capture, and read. We never save or upload your photo.",
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
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                val bmp = capturedBitmapState.value
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "Captured text",
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    Text(
                                        text = "Your captured text will appear here",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp),
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Translate to:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            LangChip("English", "en", targetLangState.value) { code ->
                                targetLangState.value = code
                                if (recognizedTextState.value.isNotBlank()) {
                                    coroutineScope.launch {
                                        try {
                                            val translated = translationService.translate(
                                                text = recognizedTextState.value,
                                                source = "auto",
                                                target = code,
                                            )
                                            translatedTextState.value = translated
                                            historyService.logTranslation(
                                                mode = "see",
                                                sourceLang = "auto",
                                                targetLang = code,
                                                sourceText = recognizedTextState.value,
                                                translatedText = translated,
                                            )
                                        } catch (e: Exception) {
                                            errorState.value = "Couldn't read or translate this image. Try again."
                                        }
                                    }
                                }
                            }
                            LangChip("Spanish", "es", targetLangState.value) { code ->
                                targetLangState.value = code
                                if (recognizedTextState.value.isNotBlank()) {
                                    coroutineScope.launch {
                                        try {
                                            val translated = translationService.translate(
                                                text = recognizedTextState.value,
                                                source = "auto",
                                                target = code,
                                            )
                                            translatedTextState.value = translated
                                            historyService.logTranslation(
                                                mode = "see",
                                                sourceLang = "auto",
                                                targetLang = code,
                                                sourceText = recognizedTextState.value,
                                                translatedText = translated,
                                            )
                                        } catch (e: Exception) {
                                            errorState.value = "Couldn't read or translate this image. Try again."
                                        }
                                    }
                                }
                            }
                            LangChip("French", "fr", targetLangState.value) { code ->
                                targetLangState.value = code
                                if (recognizedTextState.value.isNotBlank()) {
                                    coroutineScope.launch {
                                        try {
                                            val translated = translationService.translate(
                                                text = recognizedTextState.value,
                                                source = "auto",
                                                target = code,
                                            )
                                            translatedTextState.value = translated
                                            historyService.logTranslation(
                                                mode = "see",
                                                sourceLang = "auto",
                                                targetLang = code,
                                                sourceText = recognizedTextState.value,
                                                translatedText = translated,
                                            )
                                        } catch (e: Exception) {
                                            errorState.value = "Couldn't read or translate this image. Try again."
                                        }
                                    }
                                }
                            }
                            LangChip("Hindi", "hi", targetLangState.value) { code ->
                                targetLangState.value = code
                                if (recognizedTextState.value.isNotBlank()) {
                                    coroutineScope.launch {
                                        try {
                                            val translated = translationService.translate(
                                                text = recognizedTextState.value,
                                                source = "auto",
                                                target = code,
                                            )
                                            translatedTextState.value = translated
                                            historyService.logTranslation(
                                                mode = "see",
                                                sourceLang = "auto",
                                                targetLang = code,
                                                sourceText = recognizedTextState.value,
                                                translatedText = translated,
                                            )
                                        } catch (e: Exception) {
                                            errorState.value = "Couldn't read or translate this image. Try again."
                                        }
                                    }
                                }
                            }
                            LangChip("Urdu", "ur", targetLangState.value) { code ->
                                targetLangState.value = code
                                if (recognizedTextState.value.isNotBlank()) {
                                    coroutineScope.launch {
                                        try {
                                            val translated = translationService.translate(
                                                text = recognizedTextState.value,
                                                source = "auto",
                                                target = code,
                                            )
                                            translatedTextState.value = translated
                                            historyService.logTranslation(
                                                mode = "see",
                                                sourceLang = "auto",
                                                targetLang = code,
                                                sourceText = recognizedTextState.value,
                                                translatedText = translated,
                                            )
                                        } catch (e: Exception) {
                                            errorState.value = "Couldn't read or translate this image. Try again."
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                                val hasPermission =
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA,
                                    ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    val activity = context as? Activity
                                    if (activity != null) {
                                        ActivityCompat.requestPermissions(
                                            activity,
                                            arrayOf(Manifest.permission.CAMERA),
                                            CAMERA_PERMISSION_REQUEST,
                                        )
                                        errorState.value = "Please allow camera access, then tap Scan again."
                                    } else {
                                        errorState.value = "Camera permission is required to scan text."
                                    }
                                    return@Button
                                }
                                errorState.value = null
                                takePictureLauncher.launch(null)
                            },
                            enabled = !isProcessingState.value,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhotoCamera,
                                contentDescription = "Scan text",
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (isProcessingState.value) "Scanning..." else "Scan text")
                        }
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

                if (recognizedTextState.value.isNotBlank() || translatedTextState.value.isNotBlank()) {
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
                            if (recognizedTextState.value.isNotBlank()) {
                                Text(
                                    text = "Detected text",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = recognizedTextState.value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            if (translatedTextState.value.isNotBlank()) {
                                Text(
                                    text = "Translation",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = translatedTextState.value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
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
        shape = RoundedCornerShape(50),
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

// Small extension to use ML Kit Task with coroutines
private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addOnCompleteListener {
            if (it.exception != null) {
                cont.cancel(it.exception!!)
            } else {
                cont.resume(it.result, onCancellation = {})
            }
        }
    }