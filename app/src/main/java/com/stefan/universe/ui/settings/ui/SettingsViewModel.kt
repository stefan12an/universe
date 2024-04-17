package com.stefan.universe.ui.settings.ui

import androidx.lifecycle.viewModelScope
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.UiState
import com.stefan.universe.common.UserIntent
import com.stefan.universe.common.base.BaseViewModel
import com.stefan.universe.common.succeeded
import com.stefan.universe.ui.main.data.model.FirebaseUserModel
import com.stefan.universe.ui.main.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepository) :
    BaseViewModel<SettingsSideEffects, SettingsUiState, SettingsUserIntent>() {

    init {
        setUiState(SettingsUiState())
        getUserData()
    }

    override fun action(intent: SettingsUserIntent) {
        when (intent) {
            SettingsUserIntent.Logout -> {
                logout()
            }

            SettingsUserIntent.ChangeTheme -> {
                pushSideEffect(SettingsSideEffects.NavigateToChangeTheme)
            }
        }
    }


    private fun getUserData() {
        _uiState.value = uiState.value!!.copy(loading = true)
        viewModelScope.launch {
            val user = firebaseRepository.getUser()
            if (user.succeeded) {
                _uiState.value =
                    uiState.value!!.copy(user = user.data, loading = false)
            } else {
                _uiState.value = uiState.value!!.copy(loading = false)
                pushSideEffect(
                    SettingsSideEffects.Feedback(
                        user.exception?.message ?: "An error occurred"
                    )
                )
            }
        }
    }

    private fun logout() {
        _uiState.value = uiState.value!!.copy(loading = true)
        viewModelScope.launch {
            val result = firebaseRepository.logout()
            if (result.succeeded) {
                _uiState.value = uiState.value!!.copy(loading = false)
                pushSideEffect(SettingsSideEffects.NavigateToAuth)
            } else {
                pushSideEffect(
                    SettingsSideEffects.Feedback(
                        result.exception?.message ?: "An error occurred"
                    )
                )
            }
            _uiState.value = uiState.value!!.copy(loading = false)
        }
    }
}

data class SettingsUiState(
    val loading: Boolean = false,
    val user: FirebaseUserModel? = FirebaseUserModel()
) : UiState

sealed class SettingsUserIntent : UserIntent {
    data object Logout : SettingsUserIntent()
    data object ChangeTheme : SettingsUserIntent()
}

sealed class SettingsSideEffects : SideEffect {
    data object NavigateToAuth : SettingsSideEffects()
    data object NavigateToChangeTheme : SettingsSideEffects()
    data class Feedback(val message: String) : SettingsSideEffects()
}