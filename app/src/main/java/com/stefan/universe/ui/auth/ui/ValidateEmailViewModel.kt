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

@HiltViewModel
class ValidateEmailViewModel @Inject constructor(private val firebaseRepository: FirebaseRepository) :
    BaseViewModel<ValidateEmailSideEffect, ValidateEmailUiState, ValidateEmailUserIntent>() {

    init {
        setUiState(ValidateEmailUiState())
    }

    override fun action(intent: ValidateEmailUserIntent) {
        when (intent) {
            is ValidateEmailUserIntent.SendEmailVerification -> sendEmailVerification()
            is ValidateEmailUserIntent.CheckEmailVerification -> checkEmailVerification()
            is ValidateEmailUserIntent.GiveUpEmailVerification -> giveUpEmailVerification()
        }
    }

    private fun giveUpEmailVerification() {
        viewModelScope.launch {
            val result = firebaseRepository.logout()
            val sideEffect = when {
                result.succeeded -> ValidateEmailSideEffect.NavigateToAuth
                else -> ValidateEmailSideEffect.Feedback("Failed to log out")
            }
            pushSideEffect(sideEffect)
        }
    }

    private fun checkEmailVerification() {
        viewModelScope.launch {
            val result = firebaseRepository.checkEmailVerification()
            val sideEffect = when {
                result.succeeded && result.data?.emailVerified == true -> {
                    firebaseRepository
                    ValidateEmailSideEffect.NavigateToAdditionalDetails
                }
                else -> ValidateEmailSideEffect.Feedback("Email not yet verified")
            }
            pushSideEffect(sideEffect)
        }
    }

    private fun sendEmailVerification() {
        viewModelScope.launch {
            val result = firebaseRepository.sendEmailVerification()
            val sideEffect = when {
                result.succeeded -> ValidateEmailSideEffect.Feedback("Verification email sent")
                else -> ValidateEmailSideEffect.Feedback("Failed to send email verification")
            }
            pushSideEffect(sideEffect)
        }
    }
}

data class ValidateEmailUiState(
    val loading: Boolean = false,
) : UiState

sealed class ValidateEmailUserIntent : UserIntent {
    data object SendEmailVerification : ValidateEmailUserIntent()
    data object CheckEmailVerification : ValidateEmailUserIntent()
    data object GiveUpEmailVerification : ValidateEmailUserIntent()
}

sealed class ValidateEmailSideEffect : SideEffect {
    data class Feedback(val msg: String) : ValidateEmailSideEffect()
    data object NavigateToAdditionalDetails : ValidateEmailSideEffect()
    data object NavigateToAuth : ValidateEmailSideEffect()
}