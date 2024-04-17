package com.stefan.universe.ui.chat.data.model

import android.os.Parcel
import android.os.Parcelable

interface Message : Parcelable {
    val id: String
    val senderId: String
    val text: String
    val timestamp: Long
    val stability: Int
    val reactions: List<Reaction>

    val isBanner: Boolean
        get() = text.isEmpty() && senderId.isEmpty() && id.isEmpty()

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(senderId)
        dest.writeString(text)
        dest.writeLong(timestamp)
        dest.writeInt(stability)
        dest.writeTypedList(reactions)
    }
}

data class FirebaseMessageModel(
    override val id: String = "",
    override val senderId: String = "",
    override val text: String = "",
    override val timestamp: Long = 0,
    override val stability: Int = 0,
    override var reactions: List<FirebaseReactionModel> = emptyList(),
) : Message {

    override val isBanner: Boolean
        get() = text.isEmpty() && senderId.isEmpty() && id.isEmpty()

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readInt(),
        parcel.createTypedArrayList(FirebaseReactionModel) ?: emptyList()
    )

    companion object CREATOR : Parcelable.Creator<FirebaseMessageModel> {
        override fun createFromParcel(parcel: Parcel): FirebaseMessageModel {
            return FirebaseMessageModel(parcel)
        }

        override fun newArray(size: Int): Array<FirebaseMessageModel?> {
            return arrayOfNulls(size)
        }
    }
}
