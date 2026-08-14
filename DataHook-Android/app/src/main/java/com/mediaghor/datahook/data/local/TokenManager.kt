package com.mediaghor.datahook.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "datahook_prefs")

/**
 * Persists JWT access/refresh tokens (and a couple of convenience values) in
 * DataStore. All reads are Flows; the OkHttp interceptor/authenticator read the
 * latest value synchronously off the network thread.
 */
class TokenManager(private val context: Context) {

    private object Keys {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
        val EMAIL = stringPreferencesKey("user_email")
        val FCM = stringPreferencesKey("fcm_token")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[Keys.ACCESS] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[Keys.REFRESH] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[Keys.EMAIL] }
    val fcmToken: Flow<String?> = context.dataStore.data.map { it[Keys.FCM] }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map {
        !it[Keys.ACCESS].isNullOrEmpty()
    }

    suspend fun saveTokens(access: String, refresh: String) {
        context.dataStore.edit {
            it[Keys.ACCESS] = access
            it[Keys.REFRESH] = refresh
        }
    }

    suspend fun updateAccessToken(access: String) {
        context.dataStore.edit { it[Keys.ACCESS] = access }
    }

    suspend fun saveEmail(email: String) {
        context.dataStore.edit { it[Keys.EMAIL] = email }
    }

    suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { it[Keys.FCM] = token }
    }

    /** Clears session (access/refresh/email) but keeps the FCM token for re-registration. */
    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(Keys.ACCESS)
            it.remove(Keys.REFRESH)
            it.remove(Keys.EMAIL)
        }
    }
}
