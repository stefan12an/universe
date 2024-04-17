package com.stefan.universe.ui.chat.data.model

import android.os.Parcel
import android.os.Parcelable

interface Chat : Parcelable {
    val id: String
    val participants: List<String?>
    val name: String
    val latestMessage: Message
    val messages: List<Message>

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeStringList(participants)
        dest.writeString(name)
        dest.writeParcelable(latestMessage, flags)
        dest.writeTypedList(messages)
    }

    fun copyWith(
        id: String = this.id,
        participants: List<String?> = this.participants,
        name: String = this.name,
        latestMessage: Message = this.latestMessage,
        messages: List<Message> = this.messages,
    ): Chat
}

class FirebaseChatModel(
    override val id: String = "",
    override val participants: List<String?> = emptyList(),
    override val name: String = "",
    override val latestMessage: FirebaseMessageModel = FirebaseMessageModel(),
    override val messages: List<FirebaseMessageModel> = emptyList(),
) : Chat {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        mutableListOf<String>().apply {
            parcel.readStringList(this)
        },
        parcel.readString() ?: "",
        parcel.readParcelable(FirebaseMessageModel::class.java.classLoader)
            ?: FirebaseMessageModel(),
        mutableListOf<FirebaseMessageModel>().apply {
            parcel.readTypedList(this, FirebaseMessageModel)
        }
    )

    companion object CREATOR : Parcelable.Creator<FirebaseChatModel> {
        override fun createFromParcel(parcel: Parcel): FirebaseChatModel {
            return FirebaseChatModel(parcel)
        }

        override fun newArray(size: Int): Array<FirebaseChatModel?> {
            return arrayOfNulls(size)
        }
    }

    override fun copyWith(
        id: String,
        participants: List<String?>,
        name: String,
        latestMessage: Message,
        messages: List<Message>
    ): Chat {
        return FirebaseChatModel(
            id,
            participants,
            name,
            latestMessage as FirebaseMessageModel,
            messages as List<FirebaseMessageModel>
        )
    }
}