package com.stefan.universe.ui.chat.ui

import androidx.lifecycle.viewModelScope
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.UiState
import com.stefan.universe.common.UserIntent
import com.stefan.universe.common.base.BaseViewModel
import com.stefan.universe.common.succeeded
import com.stefan.universe.ui.chat.data.model.ChatWrapper
import com.stefan.universe.ui.chat.data.model.FirebaseChatModelWrapper
import com.stefan.universe.ui.chat.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(private val chatRepository: ChatRepository) :
    BaseViewModel<ChatSideEffects, ChatUiState, ChatUserIntent>() {

    init {
        setUiState(ChatUiState())
    }

    private lateinit var chatWrapper: ChatWrapper

    fun setChat(chatWrapper: ChatWrapper) {
        this.chatWrapper = chatWrapper
        setUiState(ChatUiState(chatWrapper = chatWrapper))
        listenForChatUpdates(chatWrapper.chat.id)
    }

    override fun action(intent: ChatUserIntent) {
        when (intent) {
            is ChatUserIntent.SendMessage -> {
                sendMessage(intent.message)
            }

            ChatUserIntent.GoBack -> {
                pushSideEffect(ChatSideEffects.GoBack)
            }

            is ChatUserIntent.DeleteMessage -> {
                deleteMessage(intent.messageId)
            }

            is ChatUserIntent.ReactToMessage -> {
                reactToMessage(intent.messageId, intent.reaction)
            }
        }
    }

    private fun reactToMessage(messageId: String, reaction: String) {
        viewModelScope.launch {
            val response = chatRepository.reactToMessage(
                uiState.value?.chatWrapper?.chat?.id ?: "",
                messageId,
                reaction
            )
            if (response.succeeded) {
                _uiState.value = uiState.value!!.copy(chatWrapper = response.data!!)
            } else {
                pushSideEffect(ChatSideEffects.Feedback("Failed to react to message"))
            }
        }
    }

    private fun listenForChatUpdates(chatId: String) {
        viewModelScope.launch {
            chatRepository.listenForChatUpdates(chatId)
                .collect { chat ->
                    _uiState.value = uiState.value!!.copy(chatWrapper = chat)
                }
        }
    }

    private fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            val response = chatRepository.deleteMessage(messageId, chatWrapper.chat.id)
            if (response.succeeded) {
                pushSideEffect(ChatSideEffects.MessageDeleted(response.data!!))
            } else {
                pushSideEffect(ChatSideEffects.Feedback("Failed to delete message"))
            }
        }
    }

    private fun sendMessage(message: String) {
        _uiState.value = uiState.value!!.copy(loading = true)
        val messageText = message.trim { it == '\n' || it <= ' ' }
        if (messageText.isNotEmpty()) {
            viewModelScope.launch {
                val response =
                    chatRepository.sendMessage(
                        messageText,
                        uiState.value?.chatWrapper?.chat?.id ?: ""
                    )
                if (response.succeeded) {
                    _uiState.value =
                        uiState.value!!.copy(loading = false, chatWrapper = response.data!!)
                    pushSideEffect(ChatSideEffects.MessageSent(response.data))
                } else {
                    pushSideEffect(ChatSideEffects.Feedback("Failed to send message"))
                }
            }
        } else {
            // Notify the user to enter a message
            pushSideEffect(ChatSideEffects.Feedback("Please enter a message"))
        }
        _uiState.value = uiState.value!!.copy(loading = false)
    }
}

data class ChatUiState(
    val loading: Boolean = false,
    val chatWrapper: ChatWrapper = FirebaseChatModelWrapper(),
) : UiState

sealed class ChatUserIntent : UserIntent {
    data class SendMessage(val message: String) : ChatUserIntent()
    data class DeleteMessage(val messageId: String) : ChatUserIntent()
    data class ReactToMessage(val messageId: String, val reaction: String) :
        ChatUserIntent()

    data object GoBack : ChatUserIntent()
}

sealed class ChatSideEffects : SideEffect {
    data class Feedback(val msg: String) : ChatSideEffects()

    data class MessageSent(val chat: ChatWrapper) : ChatSideEffects()
    data class MessageDeleted(val chat: ChatWrapper) : ChatSideEffects()
    data object GoBack : ChatSideEffects()
}