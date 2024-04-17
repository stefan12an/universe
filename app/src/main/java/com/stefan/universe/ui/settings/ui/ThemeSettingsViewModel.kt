package com.stefan.universe.ui.settings.ui

import androidx.lifecycle.viewModelScope
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.UiState
import com.stefan.universe.common.UserIntent
import com.stefan.universe.common.base.BaseViewModel
import com.stefan.universe.ui.settings.data.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(private val themeRepository: ThemeRepository) :
    BaseViewModel<ThemeSettingsSideEffect, ThemeSettingsState, ThemeSettingsIntent>() {

    init {
        setUiState(ThemeSettingsState())
        getTheme()
    }

    override fun action(intent: ThemeSettingsIntent) {
        when (intent) {
            is ThemeSettingsIntent.SelectTheme -> {
                onThemeSelected(intent.theme)
            }

            is ThemeSettingsIntent.GoBack -> {
                pushSideEffect(ThemeSettingsSideEffect.GoBack)
            }
        }
    }

    private fun getTheme() {
        viewModelScope.launch {
            themeRepository.getTheme().collect {
                _uiState.value = _uiState.value?.copy(theme = it)
            }
        }
    }

    private fun onThemeSelected(theme: Theme) {
        viewModelScope.launch {
            themeRepository.setTheme(theme)
        }
    }
}

sealed class ThemeSettingsIntent : UserIntent {
    data class SelectTheme(val theme: Theme) : ThemeSettingsIntent()
    data object GoBack : ThemeSettingsIntent()
}

data class ThemeSettingsState(
    val loading: Boolean = false,
    val theme: Theme? = null
) : UiState

sealed class ThemeSettingsSideEffect : SideEffect {
    data class Feedback(val message: String) : ThemeSettingsSideEffect()
    data object GoBack : ThemeSettingsSideEffect()
}

enum class Theme {
    LIGHT, DARK, SYSTEM
}