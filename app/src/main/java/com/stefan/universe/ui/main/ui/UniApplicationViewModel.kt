package com.stefan.universe.ui.main.ui

import androidx.lifecycle.viewModelScope
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.UiState
import com.stefan.universe.common.UserIntent
import com.stefan.universe.common.base.BaseViewModel
import com.stefan.universe.ui.main.data.repository.FirebaseRepository
import com.stefan.universe.ui.main.data.repository.SharedPreferencesRepository
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UniApplicationViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    sharedPreferencesRepository: Lazy<SharedPreferencesRepository>
) :
    BaseViewModel<UniApplicationSideEffects, UniApplicationUiState, UniApplicationUserIntent>() {

    override fun action(intent: UniApplicationUserIntent) {
        when (intent) {
            is UniApplicationUserIntent.NavigateToChats -> {
                pushSideEffect(UniApplicationSideEffects.NavigateToMain)
            }
            is UniApplicationUserIntent.NavigateToNotifications -> {
                pushSideEffect(UniApplicationSideEffects.NavigateToNotifications)
            }
            is UniApplicationUserIntent.NavigateToSettings -> {
                pushSideEffect(UniApplicationSideEffects.NavigateToSettings)
            }
        }
    }
    fun isLoggedIn() {
        viewModelScope.launch {
            val user = firebaseRepository.getAuthUser()
            val sideEffect = when {
                user.data?.email?.isEmpty() == true -> UniApplicationSideEffects.NavigateToAuth
                user.data?.emailVerified == true -> UniApplicationSideEffects.NavigateToMain
                else -> UniApplicationSideEffects.NavigateToValidateEmail
            }
            pushSideEffect(sideEffect)
        }
    }

}

data class UniApplicationUiState(
    val loading: Boolean = false
) : UiState

sealed class UniApplicationUserIntent : UserIntent {
    data object NavigateToChats : UniApplicationUserIntent()
    data object NavigateToNotifications : UniApplicationUserIntent()
    data object NavigateToSettings : UniApplicationUserIntent()
}


sealed class UniApplicationSideEffects : SideEffect {
    class Feedback(val msg: String) : UniApplicationSideEffects()
    data object NavigateToAuth : UniApplicationSideEffects()
    data object NavigateToValidateEmail : UniApplicationSideEffects()
    data object NavigateToMain : UniApplicationSideEffects()
    data object NavigateToNotifications : UniApplicationSideEffects()
    data object NavigateToSettings : UniApplicationSideEffects()
}