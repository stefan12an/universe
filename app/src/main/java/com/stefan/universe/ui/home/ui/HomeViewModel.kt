package com.stefan.universe.ui.home.ui

import androidx.lifecycle.viewModelScope
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.UiState
import com.stefan.universe.common.UserIntent
import com.stefan.universe.common.base.BaseViewModel
import com.stefan.universe.common.succeeded
import com.stefan.universe.ui.chat.data.model.ChatWrapper
import com.stefan.universe.ui.chat.data.repository.ChatRepository
import com.stefan.universe.ui.main.data.model.FirebaseUserModel
import com.stefan.universe.ui.main.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val firebaseRepository: FirebaseRepository,
) :
    BaseViewModel<HomeSideEffect, HomeUiState, HomeUserIntent>() {

    init {
        setUiState(HomeUiState())
        listenForChatListUpdates()
    }

    override fun action(intent: HomeUserIntent) {
        when (intent) {

            HomeUserIntent.NewMessage -> {
                pushSideEffect(HomeSideEffect.NavigateToNewMessage)
            }

            is HomeUserIntent.OpenChat -> {
                openChat(intent.chatId)
            }

            is HomeUserIntent.DeleteChat -> {
                deleteChat(intent.chatId)
            }
        }
    }

    private fun deleteChat(chatId: String) {
        viewModelScope.launch {
            val result = chatRepository.deleteChat(chatId)
            if (!result.succeeded) {
                pushSideEffect(
                    HomeSideEffect.Feedback(
                        result.exception?.message ?: "An error occurred"
                    )
                )
            }
        }
    }

    private fun openChat(chatId: String) {
        viewModelScope.launch {
            val chat = chatRepository.getChat(chatId)
            if (chat.succeeded) {
                pushSideEffect(HomeSideEffect.NavigateToChat(chat.data!!))
            } else {
                pushSideEffect(
                    HomeSideEffect.Feedback(
                        chat.exception?.message ?: "An error occurred"
                    )
                )
            }
        }
    }

    private fun listenForChatListUpdates() {
        viewModelScope.launch {
            chatRepository.listenForChatListUpdates()
                .collect { response ->
                    if (response.succeeded) {
                        getUserData()
                    } else {
                        pushSideEffect(
                            HomeSideEffect.Feedback(
                                response.exception?.message ?: "An error occurred"
                            )
                        )
                    }
                }
        }
    }

    private fun getUserData() {
        _uiState.value = uiState.value!!.copy(loading = true)
        viewModelScope.launch {
            val user = firebaseRepository.getUser()
            val chats = chatRepository.getUserChats()
            if (user.succeeded) {
                _uiState.value =
                    uiState.value!!.copy(user = user.data, loading = false, chats = chats.data)
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
}

data class HomeUiState(
    val loading: Boolean = false,
    val user: FirebaseUserModel? = FirebaseUserModel(),
    val chats: List<ChatWrapper>? = emptyList()
) : UiState

sealed class HomeUserIntent : UserIntent {
    data object NewMessage : HomeUserIntent()
    data class OpenChat(val chatId: String) : HomeUserIntent()
    data class DeleteChat(val chatId: String) : HomeUserIntent()
}

sealed class HomeSideEffect : SideEffect {
    data class Feedback(val msg: String) : HomeSideEffect()
    data class NavigateToChat(val chat: ChatWrapper) : HomeSideEffect()
    data object NavigateToNewMessage : HomeSideEffect()
}