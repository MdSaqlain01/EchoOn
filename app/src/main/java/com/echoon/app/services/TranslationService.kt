package com.echoon.app.services

import com.echoon.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Translation service for the Write, Hear, and See screens.
 *
 * Priority for best accuracy:
 * 1. Google Cloud Translation API (if GOOGLE_TRANSLATE_API_KEY is set in .env) — highest quality
 * 2. LibreTranslate-compatible API (free public instance)
 * 3. MyMemory (free fallback; when source is "auto", we detect language first for better accuracy)
 */
class TranslationService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build(),
) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val googleApiKey: String? = BuildConfig.GOOGLE_TRANSLATE_API_KEY.takeIf { it.isNotBlank() }

    // LibreTranslate-compatible (public instance)
    private val libreTranslateUrl = "https://translate.cutie.dating/translate"
    private val libreDetectUrl = "https://translate.cutie.dating/detect"

    // Fallback: MyMemory (free; ~5k chars/day anonymous)
    private val myMemoryBase = "https://api.mymemory.translated.net/get"

    /**
     * Translate text from [source] to [target].
     * - source: "en", "es", "fr", "auto", etc.
     * - target: "en", "es", "fr", etc.
     */
    suspend fun translate(
        text: String,
        source: String,
        target: String,
    ): String = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext ""

        val src = source.ifBlank { "auto" }
        val tgt = target.ifBlank { "en" }

        // 1. Google (most accurate) when API key is set
        googleApiKey?.let { key ->
            try {
                return@withContext translateViaGoogle(text, src, tgt, key)
            } catch (_: Exception) { /* fall through */ }
        }

        // 2. LibreTranslate
        try {
            return@withContext translateViaLibre(text, src, tgt)
        } catch (_: Exception) { /* fall through */ }

        // 3. MyMemory fallback; detect source language when "auto" for better accuracy
        val effectiveSource = if (src == "auto") detectLanguage(text) ?: "en" else src
        translateViaMyMemory(text, effectiveSource, tgt)
    }

    /**
     * Google Cloud Translation API v2. Highest quality; requires API key.
     */
    private fun translateViaGoogle(text: String, source: String, target: String, apiKey: String): String {
        val payload = JSONObject().apply {
            put("q", listOf(text))
            put("target", target)
            if (source != "auto") put("source", source)
        }
        val url = "https://translation.googleapis.com/language/translate/v2?key=${URLEncoder.encode(apiKey, "UTF-8")}"
        val request = Request.Builder()
            .url(url)
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Google Translate: HTTP ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            if (json.has("error")) {
                throw IllegalStateException("Google Translate: ${json.optJSONObject("error")?.optString("message", "Unknown error") ?: "Unknown error"}")
            }
            val data = json.optJSONObject("data") ?: throw IllegalStateException("Google Translate: no data")
            val translations = data.optJSONArray("translations") ?: throw IllegalStateException("Google Translate: no translations")
            val first = translations.optJSONObject(0) ?: throw IllegalStateException("Google Translate: empty translations")
            return first.optString("translatedText", "").ifBlank {
                throw IllegalStateException("Google Translate: empty result")
            }
        }
    }

    private fun translateViaLibre(text: String, source: String, target: String): String {
        val payload = JSONObject().apply {
            put("q", text)
            put("source", source)
            put("target", target)
            put("format", "text")
        }
        val request = Request.Builder()
            .url(libreTranslateUrl)
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("LibreTranslate: HTTP ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            if (json.has("error")) {
                throw IllegalStateException("LibreTranslate: ${json.optString("error", "Unknown error")}")
            }
            return json.optString("translatedText", "").ifBlank {
                throw IllegalStateException("LibreTranslate: empty response")
            }
        }
    }

    /**
     * Call LibreTranslate /detect to get source language for "auto" when using MyMemory.
     */
    private fun detectLanguage(text: String): String? {
        return try {
            val payload = JSONObject().apply { put("q", text) }
            val request = Request.Builder()
                .url(libreDetectUrl)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body?.string().orEmpty()
                val arr = org.json.JSONArray(body)
                if (arr.length() == 0) return@use null
                val first = arr.optJSONObject(0) ?: return@use null
                first.optString("language", "").takeIf { it.isNotBlank() }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun translateViaMyMemory(text: String, source: String, target: String): String {
        val encoded = URLEncoder.encode(text, "UTF-8")
        val langpair = "$source|$target"
        val url = "$myMemoryBase?q=$encoded&langpair=$langpair"
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("MyMemory: HTTP ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val responseData = json.optJSONObject("responseData")
                ?: throw IllegalStateException("MyMemory: no responseData")
            val translated = responseData.optString("translatedText", "").trim()
            if (translated.isBlank()) {
                throw IllegalStateException("MyMemory: empty translation")
            }
            return translated
        }
    }
}
