package com.stefan.universe.ui.home.ui

import androidx.lifecycle.viewModelScope
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.UiState
import com.stefan.universe.common.UserIntent
import com.stefan.universe.common.base.BaseViewModel
import com.stefan.universe.common.succeeded
import com.stefan.universe.ui.home.data.model.Chat
import com.stefan.universe.ui.home.data.repository.ChatRepository
import com.stefan.universe.ui.main.data.model.FirebaseUserModel
import com.stefan.universe.ui.main.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val firebaseRepository: FirebaseRepository
) :
    BaseViewModel<HomeSideEffect, HomeUiState, HomeUserIntent>() {

    init {
        setUiState(HomeUiState())
        getUser()
        getChatList()
    }

    override fun action(intent: HomeUserIntent) {
        when (intent) {
            HomeUserIntent.Logout -> {
                logout()
            }

            HomeUserIntent.NewMessage -> {
                pushSideEffect(HomeSideEffect.NavigateToNewMessage)
            }
        }
    }


    private fun getChatList() {
        _uiState.value = uiState.value!!.copy(loading = true)
        viewModelScope.launch {
            _uiState.value =
                uiState.value!!.copy(loading = false, chats = chatRepository.getUserChats().data)
        }
    }

    private fun getUser() {
        _uiState.value = uiState.value!!.copy(loading = true)
        viewModelScope.launch {
            val user = firebaseRepository.getUser()
            if (user.succeeded) {
                _uiState.value = uiState.value!!.copy(user = user.data, loading = false)
            } else {
                _uiState.value = uiState.value!!.copy(loading = false)
                pushSideEffect(
                    HomeSideEffect.Feedback(
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
                pushSideEffect(HomeSideEffect.NavigateToAuth)
            } else {
                pushSideEffect(
                    HomeSideEffect.Feedback(
                        result.exception?.message ?: "An error occurred"
                    )
                )
            }
            _uiState.value = uiState.value!!.copy(loading = false)
        }
    }
}

data class HomeUiState(
    val loading: Boolean = false,
    val user: FirebaseUserModel? = FirebaseUserModel(),
    val chats: List<Chat>? = emptyList()
) : UiState

sealed class HomeUserIntent : UserIntent {
    data object Logout : HomeUserIntent()
    data object NewMessage : HomeUserIntent()
}

sealed class HomeSideEffect : SideEffect {
    data class Feedback(val msg: String) : HomeSideEffect()
    data object NavigateToAuth : HomeSideEffect()
    data object NavigateToNewMessage : HomeSideEffect()
}