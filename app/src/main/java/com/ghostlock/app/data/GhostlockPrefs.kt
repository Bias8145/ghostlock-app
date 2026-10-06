package com.ghostlock.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore(name = "ghostlock_prefs")

object GhostlockPrefs {
    private val THEME_MODE = intPreferencesKey("theme_mode")
    private val SAFE_MODE = booleanPreferencesKey("safe_mode")
    private val CPU_PAIR_INDEX = intPreferencesKey("cpu_pair_index")
    private val CUSTOM_CPU_PAIR = stringPreferencesKey("custom_cpu_pair")
    private val LANGUAGE = stringPreferencesKey("language")
    private val TEXT_SCALE = floatPreferencesKey("text_scale")

    // Theme mode: 0 = System, 1 = Light, 2 = Dark
    suspend fun Context.getThemeMode(): Int = dataStore.data.first()[THEME_MODE] ?: 0

    suspend fun Context.setThemeMode(mode: Int) = withContext(Dispatchers.IO) {
        dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun Context.isSafeModeEnabled(): Boolean =
        dataStore.data.first()[SAFE_MODE] ?: false

    suspend fun Context.setSafeMode(enabled: Boolean) = withContext(Dispatchers.IO) {
        dataStore.edit { it[SAFE_MODE] = enabled }
    }

    suspend fun Context.getCpuPairIndex(): Int =
        dataStore.data.first()[CPU_PAIR_INDEX] ?: 0

    suspend fun Context.setCpuPairIndex(index: Int) = withContext(Dispatchers.IO) {
        dataStore.edit { it[CPU_PAIR_INDEX] = index }
    }

    suspend fun Context.getCustomCpuPair(): String? =
        dataStore.data.first()[CUSTOM_CPU_PAIR]

    suspend fun Context.setCustomCpuPair(pair: String?) = withContext(Dispatchers.IO) {
        dataStore.edit {
            if (pair == null) it.remove(CUSTOM_CPU_PAIR) else it[CUSTOM_CPU_PAIR] = pair
        }
    }

    suspend fun Context.getLanguage(): String =
        dataStore.data.first()[LANGUAGE] ?: "id"

    suspend fun Context.setLanguage(lang: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[LANGUAGE] = lang }
    }

    suspend fun Context.getTextScale(): Float =
        dataStore.data.first()[TEXT_SCALE] ?: 1.0f

    suspend fun Context.setTextScale(scale: Float) = withContext(Dispatchers.IO) {
        dataStore.edit { it[TEXT_SCALE] = scale }
    }
}
