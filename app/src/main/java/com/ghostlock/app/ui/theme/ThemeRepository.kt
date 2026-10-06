package com.ghostlock.app.ui.theme

import android.content.Context
import androidx.compose.runtime.isSystemInDarkTheme
import androidx.lifecycle.viewModel
import androidx.lifecycle.viewmodelScope
import com.ghostlock.app.data.GhostlockPrefs
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class UiMode { SYSTEM, LIGHT, DARK }

class ThemeRepository @ViewModelInject constructor(private val context: Context) {
    private val _uiMode = MutableStateFlow(UiMode.SYSTEM)
    val uiMode: StateFlow<UiMode> = _uiMode.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = context.getThemeMode()
            _uiMode.value = when (saved) {
                0 -> UiMode.SYSTEM
                1 -> UiMode.LIGHT
                2 -> UiMode.DARK
                else -> UiMode.SYSTEM
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(5_000)
                if (currentUiMode() == UiMode.SYSTEM) {
                    val systemDark = isSystemInDarkTheme()
                    val newMode = if (systemDark) UiMode.DARK else UiMode.LIGHT
                    if (_uiMode.value != newMode) {
                        _uiMode.value = newMode
                    }
                }
            }
        }
    }

    fun setUserChoice(mode: UiMode) {
        viewModelScope.launch {
            val modeInt = when (mode) {
                UiMode.SYSTEM -> 0
                UiMode.LIGHT -> 1
                UiMode.DARK -> 2
            }
            context.setThemeMode(modeInt)
            _uiMode.value = mode
        }
    }

    fun getActualIsDark(): Boolean {
        return when (currentUiMode()) {
            UiMode.SYSTEM -> isSystemInDarkTheme()
            UiMode.LIGHT -> false
            UiMode.DARK -> true
        }
    }

    private fun currentUiMode(): UiMode = _uiMode.value
}