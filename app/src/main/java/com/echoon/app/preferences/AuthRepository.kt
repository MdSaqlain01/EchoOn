package com.echoon.app.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

private val ACCOUNT_USERNAME = stringPreferencesKey("account_username")
private val ACCOUNT_PASSWORD_HASH = stringPreferencesKey("account_password_hash")
private val SESSION_USERNAME = stringPreferencesKey("session_username")

private fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}

class AuthRepository(private val context: Context) {

    val sessionState: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[SESSION_USERNAME]
    }

    suspend fun signUp(username: String, password: String): Result<Unit> {
        val u = username.trim()
        val p = password
        if (u.isBlank()) return Result.failure(IllegalArgumentException("Username cannot be empty"))
        if (p.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        context.authDataStore.edit { prefs ->
            prefs[ACCOUNT_USERNAME] = u
            prefs[ACCOUNT_PASSWORD_HASH] = hashPassword(p)
            prefs[SESSION_USERNAME] = u
        }
        return Result.success(Unit)
    }

    suspend fun login(username: String, password: String): Result<Unit> {
        val u = username.trim()
        val p = password
        if (u.isBlank() || p.isBlank()) return Result.failure(IllegalArgumentException("Username and password required"))
        val prefs = context.authDataStore.data.first()
        val storedUser = prefs[ACCOUNT_USERNAME] ?: return Result.failure(IllegalArgumentException("No account found. Create one first."))
        val storedHash = prefs[ACCOUNT_PASSWORD_HASH] ?: return Result.failure(IllegalArgumentException("No account found. Create one first."))
        if (storedUser != u) return Result.failure(IllegalArgumentException("Username or password incorrect"))
        if (storedHash != hashPassword(p)) return Result.failure(IllegalArgumentException("Username or password incorrect"))
        context.authDataStore.edit { it[SESSION_USERNAME] = u }
        return Result.success(Unit)
    }

    suspend fun logout() {
        context.authDataStore.edit { it.remove(SESSION_USERNAME) }
    }
}
