package com.ghostlock.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.edit.put
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "ghostlock_prefs")

object GhostlockPrefs {
    private val Context.THEME_MODE = intPreferencesKey("theme_mode")
    private val Context.SAFE_MODE = booleanPreferencesKey("safe_mode")
    private val Context.CPU_PAIR_INDEX = intPreferencesKey("cpu_pair_index")
    private val Context.CUSTOM_CPU_PAIR = stringPreferencesKey("custom_cpu_pair")
    private val Context.LANGUAGE = stringPreferencesKey("language")
    private val Context.TEXT_SCALE = floatPreferencesKey("text_scale")

    // Tema mode: 0 = System, 1 = Light, 2 = Dark
    fun Context.getThemeMode(): Int = dataStore.data.first().getInt(THEME_MODE, 0)
    suspend fun Context.setThemeMode(mode: Int) = withContext(Dispatchers.IO) {
        dataStore.edit { it[THEME_MODE] = mode }
    }

    fun Context.isSafeModeEnabled(): Boolean = dataStore.data.first().getBoolean(SAFE_MODE, false)
    suspend fun Context.setSafeMode(enabled: Boolean) = withContext(Dispatchers.IO) {
        dataStore.edit { it[SAFE_MODE] = enabled }
    }

    fun Context.getCpuPairIndex(): Int = dataStore.data.first().getInt(CPU_PAIR_INDEX, 0)
    suspend fun Context.setCpuPairIndex(index: Int) = withContext(Dispatchers.IO) {
        dataStore.edit { it[CPU_PAIR_INDEX] = index }
    }

    fun Context.getCustomCpuPair(): String? = dataStore.data.first()[CUSTOM_CPU_PAIR]
    suspend fun Context.setCustomCpuPair(pair: String?) = withContext(Dispatchers.IO) {
        dataStore.edit { it[CUSTOM_CPU_PAIR] = pair }
    }

    fun Context.getLanguage(): String = dataStore.data.first().getString(LANGUAGE, "id")
    suspend fun Context.setLanguage(lang: String) = withContext(Dispatchers.IO) {
        dataStore.edit { it[LANGUAGE] = lang }
    }

    fun Context.getTextScale(): Float = dataStore.data.first().getFloat(TEXT_SCALE, 1.0f)
    suspend fun Context.setTextScale(scale: Float) = withContext(Dispatchers.IO) {
        dataStore.edit { it[TEXT_SCALE] = scale }
    }
}