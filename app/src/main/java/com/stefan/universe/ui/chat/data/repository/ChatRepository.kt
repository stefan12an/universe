package com.stefan.universe.ui.chat.data.repository

import android.app.Application
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.stefan.universe.common.Resource
import com.stefan.universe.common.Status
import com.stefan.universe.ui.chat.data.model.ChatWrapper
import com.stefan.universe.ui.chat.data.model.FirebaseChatModel
import com.stefan.universe.ui.chat.data.model.FirebaseChatModelWrapper
import com.stefan.universe.ui.chat.data.model.FirebaseMessageModel
import com.stefan.universe.ui.chat.data.model.FirebaseReactionModel
import com.stefan.universe.ui.main.data.model.FirebaseUserModel
import com.stefan.universe.ui.main.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface ChatRepository {
    suspend fun getUsers(): Resource<List<User>>
    suspend fun getUserChats(): Resource<List<ChatWrapper>>
    suspend fun updateUserChatReferenceList(uid: String, chatUid: String): Resource<Boolean>
    suspend fun startChat(user: User): Resource<ChatWrapper>
    suspend fun getChat(chatId: String): Resource<ChatWrapper>
    suspend fun sendMessage(message: String, chatId: String): Resource<ChatWrapper>
    suspend fun deleteMessage(messageId: String, chatId: String): Resource<ChatWrapper>
    suspend fun reactToMessage(
        chatId: String,
        messageId: String,
        reaction: String
    ): Resource<ChatWrapper>

    suspend fun deleteChat(chatId: String): Resource<ChatWrapper>
    fun listenForChatUpdates(chatUid: String): Flow<ChatWrapper>
    fun listenForChatListUpdates(): Flow<Resource<Boolean>>
}

class ChatRepositoryImpl @Inject constructor(private val app: Application) : ChatRepository {


    override suspend fun getUsers(): Resource<List<User>> {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        return try {
            val snapshot =
                db.collection("Users").whereNotEqualTo(FieldPath.documentId(), currentUser?.uid)
                    .get().await()
            val userList = snapshot.map { it.toObject(FirebaseUserModel::class.java) }
            Resource.success(userList)
        } catch (e: Exception) {
            Resource(Status.ERROR, emptyList(), errorCode = 500, exception = e)
        }
    }

    override suspend fun getUserChats(): Resource<List<ChatWrapper>> {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser = auth.currentUser ?: return Resource.error(errorCode = 500)
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        return try {
            val snapshot =
                db.collection("Chats").whereArrayContains("participants", currentUser.uid).get()
                    .await()
            val chatList = snapshot.map { it.toObject(FirebaseChatModel::class.java) }
            Resource.success(chatList.map { FirebaseChatModelWrapper(it) }
                .sortedByDescending { it.chat.latestMessage.timestamp })
        } catch (e: Exception) {
            Resource(Status.ERROR, emptyList(), errorCode = 500, exception = e)
        }
    }

    override suspend fun updateUserChatReferenceList(
        uid: String, chatUid: String
    ): Resource<Boolean> {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        return try {
            val currentChats = db.collection("Users").document(uid).get().await()
                .toObject(FirebaseUserModel::class.java)?.chats?.toMutableList() ?: mutableListOf()
            if (!currentChats.contains(chatUid)) {
                currentChats.add(chatUid)
                db.collection("Users").document(uid).update("chats", currentChats).await()
            }
            Resource.success(true)
        } catch (e: Exception) {
            Resource.error(errorCode = 500, exception = e)
        }
    }


    override suspend fun startChat(user: User): Resource<ChatWrapper> {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser = auth.currentUser ?: return Resource.error(errorCode = 500)
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        val chatName = "${currentUser.displayName}-${user.displayName}"
        val chatId = if (currentUser.uid < user.uid) {
            "${currentUser.uid}${user.uid}"
        } else {
            "${user.uid}${currentUser.uid}"
        }

        return try {
            val reference = db.collection("Chats")
            val document = reference.document(chatId).get().await()

            if (document.exists()) {
                Resource.success(FirebaseChatModelWrapper(document.toObject(FirebaseChatModel::class.java)!!))
            } else {
                val newChat = FirebaseChatModel(
                    name = chatName, id = chatId, participants = listOf(
                        currentUser.uid, user.uid
                    )
                )
                reference.document(chatId).set(newChat).await()
                val senderUpdate = updateUserChatReferenceList(currentUser.uid, chatId)
                val receiverUpdate = updateUserChatReferenceList(user.uid, chatId)

                if (senderUpdate.status == Status.ERROR || receiverUpdate.status == Status.ERROR) {
                    db.collection("Chats").document(chatId).delete().await()
                    Resource.error<FirebaseChatModel>(errorCode = 500)
                }

                return Resource.success(FirebaseChatModelWrapper(newChat))
            }
        } catch (e: Exception) {
            return Resource.error(errorCode = 500, exception = e)
        }
    }

    override suspend fun getChat(chatId: String): Resource<ChatWrapper> {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        return try {
            val document = db.collection("Chats").document(chatId).get().await()
            Resource.success(FirebaseChatModelWrapper(document.toObject(FirebaseChatModel::class.java)!!))
        } catch (e: Exception) {
            Resource.error(errorCode = 500, exception = e)
        }
    }

    override suspend fun sendMessage(message: String, chatId: String): Resource<ChatWrapper> {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser = auth.currentUser ?: return Resource.error(errorCode = 500)
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val reference = db.collection("Chats")
        val document = reference.document(chatId)
        return try {
            val chat = document.get().await().toObject(FirebaseChatModel::class.java)!!
            val messages = chat.messages.toMutableList()
            val newMessage = FirebaseMessageModel(
                id = "${System.currentTimeMillis()}${currentUser.uid}",
                senderId = currentUser.uid,
                text = message,
                timestamp = System.currentTimeMillis()
            )
            messages.add(newMessage)
            reference.document(chatId)
                .update(mapOf("messages" to messages, "latestMessage" to newMessage)).await()

            Resource.success(
                FirebaseChatModelWrapper(
                    document.get().await().toObject(FirebaseChatModel::class.java)!!
                )
            )
        } catch (e: Exception) {
            Resource.error(errorCode = 500, exception = e)
        }
    }

    override suspend fun deleteMessage(messageId: String, chatId: String): Resource<ChatWrapper> {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val reference = db.collection("Chats")
        val document = reference.document(chatId)
        return try {
            val chat = document.get().await().toObject(FirebaseChatModel::class.java)!!
            val messages = chat.messages.toMutableList()
            val message = messages.find { it.id == messageId }
            messages.remove(message)

            reference.document(chatId)
                .update(
                    mapOf(
                        "messages" to messages,
                        "latestMessage" to (messages.lastOrNull() ?: FirebaseMessageModel())
                    )
                ).await()

            Resource.success(
                FirebaseChatModelWrapper(
                    document.get().await().toObject(FirebaseChatModel::class.java)!!
                )
            )
        } catch (e: Exception) {
            Resource.error(errorCode = 500, exception = e)
        }
    }

    override suspend fun reactToMessage(
        chatId: String,
        messageId: String,
        reaction: String
    ): Resource<ChatWrapper> {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val reference = db.collection("Chats")
        val document = reference.document(chatId)
        return try {
            val chat = document.get().await().toObject(FirebaseChatModel::class.java)!!
            val messages = chat.messages.toMutableList()
            val message = messages.find { it.id == messageId }
            val reactions = message?.reactions?.toMutableList() ?: mutableListOf()
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val existingReaction = reactions.find { it.senderId == currentUserUid }

            if (existingReaction != null) {
                // User has already reacted, update the reaction
                if (existingReaction.data == reaction) {
                    // User is removing the reaction
                    reactions.remove(existingReaction)
                } else {
                    // User is changing the reaction
                    existingReaction.data = reaction
                    existingReaction.timestamp = System.currentTimeMillis()
                }
            } else {
                // User has not reacted yet, add a new reaction
                val reactionModel = FirebaseReactionModel(
                    "${System.currentTimeMillis()}${currentUserUid}",
                    messageId,
                    currentUserUid,
                    System.currentTimeMillis(),
                    reaction,
                )
                reactions.add(reactionModel)
            }

            message?.reactions = reactions
            reference.document(chatId)
                .update(
                    mapOf(
                        "messages" to messages,
                        "latestMessage" to (messages.lastOrNull() ?: FirebaseMessageModel())
                    )
                ).await()

            Resource.success(
                FirebaseChatModelWrapper(
                    document.get().await().toObject(FirebaseChatModel::class.java)!!
                )
            )
        } catch (e: Exception) {
            Resource.error(errorCode = 500, exception = e)
        }
    }

    override suspend fun deleteChat(chatId: String): Resource<ChatWrapper> {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val reference = db.collection("Chats")
        return try {
            val chat =
                reference.document(chatId).get().await().toObject(FirebaseChatModel::class.java)!!
            val participants = chat.participants
            participants.forEach {
                if (it != null) {
                    val userChats = db.collection("Users").document(it).get().await()
                        .toObject(FirebaseUserModel::class.java)?.chats?.toMutableList()
                        ?: mutableListOf()
                    userChats.remove(chatId)
                    db.collection("Users").document(it).update("chats", userChats).await()
                }
            }
            reference.document(chatId).delete().await()
            Resource.success(FirebaseChatModelWrapper())
        } catch (e: Exception) {
            Resource.error(errorCode = 500, exception = e)
        }
    }

    override fun listenForChatUpdates(chatUid: String): Flow<ChatWrapper> = callbackFlow {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val reference = db.collection("Chats")
        val docRef = reference.document(chatUid)
        val listener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("TAG", "Listen failed.", e)
                trySend(FirebaseChatModelWrapper())
                return@addSnapshotListener
            }

            val updatedChat =
                snapshot?.toObject(FirebaseChatModel::class.java) ?: FirebaseChatModel()

            trySend(FirebaseChatModelWrapper(updatedChat))
        }

        awaitClose {
            listener.remove()
        }
    }

    override fun listenForChatListUpdates(): Flow<Resource<Boolean>> {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser =
            auth.currentUser ?: return callbackFlow { trySend(Resource.error(500)) }
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val reference =
            db.collection("Chats").whereArrayContains("participants", currentUser.uid)
        return callbackFlow {
            val listener = reference.addSnapshotListener { _, e ->
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e)
                    trySend(Resource.error(500))
                    return@addSnapshotListener
                }

                trySend(Resource.success(true))
            }

            awaitClose {
                listener.remove()
            }
        }
    }
}