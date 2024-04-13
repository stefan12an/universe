package com.stefan.universe.ui.auth.ui

import androidx.lifecycle.viewModelScope
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.UiState
import com.stefan.universe.common.UserIntent
import com.stefan.universe.common.base.BaseViewModel
import com.stefan.universe.common.succeeded
import com.stefan.universe.ui.main.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthType {
    LOGIN, REGISTER
}

@HiltViewModel
class AuthViewModel @Inject constructor(private val firebaseRepository: FirebaseRepository) :
    BaseViewModel<AuthSideEffect, AuthUiState, AuthUserIntent>() {

    init {
        setUiState(AuthUiState())
    }

    override fun action(intent: AuthUserIntent) {
        when (intent) {
            is AuthUserIntent.Login -> {
                login(intent.email, intent.password)
            }

            is AuthUserIntent.Register -> {
                register(intent.email, intent.password)
            }

            AuthUserIntent.ChangeToLogin -> {
                _uiState.value = uiState.value!!.copy(type = AuthType.LOGIN)
                pushSideEffect(AuthSideEffect.ChangeToLogin)
            }

            AuthUserIntent.ChangeToRegister -> {
                _uiState.value = uiState.value!!.copy(type = AuthType.REGISTER)
                pushSideEffect(AuthSideEffect.ChangeToRegister)
            }
        }
    }

    private fun login(email: String?, password: String?) {
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            pushSideEffect(AuthSideEffect.Feedback("Please enter email and password"))
            return
        }

        _uiState.value = uiState.value!!.copy(loading = true)

        viewModelScope.launch {
            val result = firebaseRepository.authenticate(email, password)
            _uiState.value = uiState.value!!.copy(loading = false)

            val sideEffect = when {
                result.succeeded && result.data?.emailVerified == true -> AuthSideEffect.NavigateToMain
                result.succeeded -> AuthSideEffect.NavigateToValidateEmail
                else -> AuthSideEffect.Feedback(result.exception?.message ?: "An error occurred")
            }

            pushSideEffect(sideEffect)
        }
    }

    private fun register(email: String?, password: String?) {
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            pushSideEffect(AuthSideEffect.Feedback("Please enter email and password"))
            return
        }

        _uiState.value = uiState.value!!.copy(loading = true)

        viewModelScope.launch {
            val result = firebaseRepository.register(email, password)
            _uiState.value = uiState.value!!.copy(loading = false)

            val sideEffect = if (result.succeeded) AuthSideEffect.NavigateToValidateEmail
            else AuthSideEffect.Feedback(result.exception?.message ?: "An error occurred")

            pushSideEffect(sideEffect)
        }
    }
}

data class AuthUiState(
    val loading: Boolean = false,
    val type: AuthType = AuthType.LOGIN
) : UiState

sealed class AuthUserIntent : UserIntent {
    class Login(val email: String?, val password: String?) : AuthUserIntent()
    data object ChangeToLogin : AuthUserIntent()
    class Register(val email: String?, val password: String?) : AuthUserIntent()
    data object ChangeToRegister : AuthUserIntent()

}

sealed class AuthSideEffect : SideEffect {
    data class Feedback(val msg: String) : AuthSideEffect()
    data object NavigateToMain : AuthSideEffect()
    data object NavigateToValidateEmail : AuthSideEffect()
    data object ChangeToLogin : AuthSideEffect()
    data object ChangeToRegister : AuthSideEffect()
}