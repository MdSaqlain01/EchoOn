package com.echoon.app.services

import com.echoon.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class TranslationHistoryEntry(
    val id: String,
    val createdAt: String,
    val mode: String,
    val sourceLang: String,
    val targetLang: String,
    val sourceText: String,
    val translatedText: String,
)

/**
 * Best-effort logger for translation history in Supabase.
 *
 * This uses the REST API and the anon key from BuildConfig.
 * If Supabase is not configured or the network fails, calls are
 * safely ignored so they never block the main app experience.
 */
class SupabaseHistoryService(
    private val client: OkHttpClient = OkHttpClient(),
) {

    private val baseUrl: String = BuildConfig.SUPABASE_URL
    private val anonKey: String = BuildConfig.SUPABASE_ANON_KEY
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun logTranslation(
        mode: String,
        sourceLang: String,
        targetLang: String,
        sourceText: String,
        translatedText: String,
    ) {
        if (baseUrl.isBlank() || anonKey.isBlank()) {
            // Supabase not configured; skip silently
            return
        }

        withContext(Dispatchers.IO) {
            val url = baseUrl.trimEnd('/') + "/rest/v1/translations"

            val payload = JSONObject().apply {
                put("mode", mode)
                put("source_lang", sourceLang)
                put("target_lang", targetLang)
                put("source_text", sourceText)
                put("translated_text", translatedText)
            }

            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build()

            // Fire-and-forget: we don't throw even if Supabase fails
            try {
                client.newCall(request).execute().use { /* ignore body */ }
            } catch (_: Exception) {
                // Ignore logging failures
            }
        }
    }

    suspend fun getRecentTranslations(limit: Int = 10): List<TranslationHistoryEntry> {
        if (baseUrl.isBlank() || anonKey.isBlank()) {
            return emptyList()
        }

        return withContext(Dispatchers.IO) {
            val url =
                baseUrl.trimEnd('/') +
                    "/rest/v1/translations?select=*&order=created_at.desc&limit=$limit"

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext emptyList()
                    }
                    val body = response.body?.string().orEmpty()
                    if (body.isBlank()) return@withContext emptyList()

                    val arr = JSONArray(body)
                    buildList {
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            add(
                                TranslationHistoryEntry(
                                    id = obj.optString("id"),
                                    createdAt = obj.optString("created_at"),
                                    mode = obj.optString("mode"),
                                    sourceLang = obj.optString("source_lang"),
                                    targetLang = obj.optString("target_lang"),
                                    sourceText = obj.optString("source_text"),
                                    translatedText = obj.optString("translated_text"),
                                ),
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}

