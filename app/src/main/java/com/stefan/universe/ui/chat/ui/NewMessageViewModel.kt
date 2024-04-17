package com.stefan.universe.ui.chat.ui

import androidx.lifecycle.viewModelScope
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.UiState
import com.stefan.universe.common.UserIntent
import com.stefan.universe.common.base.BaseViewModel
import com.stefan.universe.common.succeeded
import com.stefan.universe.ui.chat.data.model.ChatWrapper
import com.stefan.universe.ui.chat.data.repository.ChatRepository
import com.stefan.universe.ui.main.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewMessageViewModel @Inject constructor(private val chatRepository: ChatRepository) :
    BaseViewModel<NewMessageSideEffects, NewMessageUiState, NewMessageUserIntent>() {

    init {
        setUiState(NewMessageUiState())

        getUsersList()
    }

    override fun action(intent: NewMessageUserIntent) {
        when (intent) {
            is NewMessageUserIntent.StartChat -> {
                startChat(intent.user)
            }

            NewMessageUserIntent.NavigateHome -> {
                pushSideEffect(NewMessageSideEffects.NavigateToHome)
            }
        }
    }

    private fun startChat(user: User) {
        viewModelScope.launch {
            val response = chatRepository.startChat(user)
            if (response.succeeded) {
                pushSideEffect(NewMessageSideEffects.NavigateToChat(response.data!!))
            } else {
                pushSideEffect(NewMessageSideEffects.Feedback("Failed to start chat"))
            }
        }
    }

    private fun getUsersList() {
        viewModelScope.launch {
            _uiState.value = uiState.value!!.copy(loading = true)
            val users = chatRepository.getUsers()
            _uiState.value =
                uiState.value!!.copy(loading = false, users = users.data ?: emptyList())
        }
    }
}

data class NewMessageUiState(
    val loading: Boolean = false,
    val users: List<User> = emptyList()
) : UiState

sealed class NewMessageUserIntent : UserIntent {
    data class StartChat(val user: User) : NewMessageUserIntent()
    data object NavigateHome : NewMessageUserIntent()
}

sealed class NewMessageSideEffects : SideEffect {
    data class Feedback(val msg: String) : NewMessageSideEffects()

    data object NavigateToHome : NewMessageSideEffects()
    data class NavigateToChat(val chat: ChatWrapper) : NewMessageSideEffects()

}
