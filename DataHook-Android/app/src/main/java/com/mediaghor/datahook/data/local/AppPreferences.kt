package com.mediaghor.datahook.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// A separate DataStore file from TokenManager's "datahook_prefs" so appearance
// settings never touch (or get cleared with) session storage.
private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

/**
 * Persisted appearance preferences: theme mode + Material You dynamic color.
 *
 * Defaults preserve the behavior the app had before this setting existed:
 * follow the system dark-mode setting, and use dynamic color wherever the
 * platform supports it (Android 12+; older devices ignore the flag entirely).
 */
class AppPreferences(private val context: Context) {

    private object Keys {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }

    /** One of [MODE_SYSTEM] / [MODE_LIGHT] / [MODE_DARK]. */
    val themeMode: Flow<Int> = context.appDataStore.data.map { it[Keys.THEME_MODE] ?: MODE_SYSTEM }

    /**
     * Material You wallpaper colors. Only honored on Android 12+ — the theme
     * gates on the SDK level and Settings hides the switch before then, so the
     * stored value is simply unused on older devices.
     */
    val dynamicColor: Flow<Boolean> =
        context.appDataStore.data.map { it[Keys.DYNAMIC_COLOR] ?: false }

    suspend fun setThemeMode(mode: Int) {
        context.appDataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.appDataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    companion object {
        const val MODE_SYSTEM = 0
        const val MODE_LIGHT = 1
        const val MODE_DARK = 2
    }
}
