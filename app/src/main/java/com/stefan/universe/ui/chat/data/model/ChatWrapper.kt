package com.stefan.universe.ui.chat.data.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface ChatWrapper : Parcelable {
    val chat: Chat

    fun isSenderCurrentUser(message: Message): Boolean
    suspend fun senderPhotoUri(message: Message): String?
    suspend fun senderDisplayName(message: Message): String?
    suspend fun recieverPhotoUri(): String?
    suspend fun recieverDisplayName(): String?

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(chat, flags)
    }

    fun copyWith(chat: Chat): ChatWrapper
}

class FirebaseChatModelWrapper(override val chat: FirebaseChatModel = FirebaseChatModel()) :
    ChatWrapper {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(FirebaseChatModel::class.java.classLoader)
            ?: FirebaseChatModel(parcel),
    )


    override fun isSenderCurrentUser(message: Message): Boolean {
        return FirebaseAuth.getInstance().currentUser?.uid == message.senderId
    }

    override suspend fun senderDisplayName(message: Message): String? {
        return FirebaseFirestore.getInstance().collection("Users").document(message.senderId)
            .get().await().getString("displayName")
    }

    override suspend fun senderPhotoUri(message: Message): String? {
        return FirebaseFirestore.getInstance().collection("Users").document(message.senderId)
            .get().await().getString("photoUri")
    }


    override suspend fun recieverPhotoUri(): String? {
        return chat.participants.first { recieverId -> recieverId != FirebaseAuth.getInstance().currentUser?.uid }
            ?.let { recieverId ->
                FirebaseFirestore.getInstance().collection("Users").document(recieverId)
                    .get().await().getString("photoUri")
            }
    }

    override suspend fun recieverDisplayName(): String? {
        return chat.participants.first { recieverId -> recieverId != FirebaseAuth.getInstance().currentUser?.uid }
            ?.let { recieverId ->
                FirebaseFirestore.getInstance().collection("Users").document(recieverId)
                    .get().await().getString("displayName")
            }
    }

    override fun equals(other: Any?): Boolean {
        return chat == (other as FirebaseChatModelWrapper).chat
    }

    override fun hashCode(): Int {
        return chat.hashCode()
    }

    override fun copyWith(chat: Chat): ChatWrapper {
        return FirebaseChatModelWrapper((chat as FirebaseChatModel))
    }

    companion object CREATOR : Parcelable.Creator<FirebaseChatModelWrapper> {
        override fun createFromParcel(parcel: Parcel): FirebaseChatModelWrapper {
            return FirebaseChatModelWrapper(parcel)
        }

        override fun newArray(size: Int): Array<FirebaseChatModelWrapper?> {
            return arrayOfNulls(size)
        }
    }
}
