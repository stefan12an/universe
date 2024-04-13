package com.stefan.universe.ui.home.ui

import androidx.lifecycle.viewModelScope
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.UiState
import com.stefan.universe.common.UserIntent
import com.stefan.universe.common.base.BaseViewModel
import com.stefan.universe.ui.home.data.repository.ChatRepository
import com.stefan.universe.ui.main.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
                startChat()
            }
        }
    }

    private fun startChat() {
        viewModelScope.launch {
            chatRepository.startChat()
            pushSideEffect(NewMessageSideEffects.StartChat)
        }
    }

    private fun getUsersList() {
        viewModelScope.launch {
            _uiState.value = uiState.value!!.copy(loading = true)
            delay(2000)
            val users = chatRepository.getUsers()
            _uiState.value = uiState.value!!.copy(loading = false, users = users.data ?: emptyList())
        }
    }
}

data class NewMessageUiState(
    val loading: Boolean = false,
    val users: List<User> = emptyList()
) : UiState

sealed class NewMessageUserIntent : UserIntent {
    data object StartChat : NewMessageUserIntent()
}

sealed class NewMessageSideEffects : SideEffect {
    data object NavigateToHome : NewMessageSideEffects()
    data object StartChat : NewMessageSideEffects()

}
