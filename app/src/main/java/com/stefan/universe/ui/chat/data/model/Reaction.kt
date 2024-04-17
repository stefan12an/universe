package com.stefan.universe.ui.chat.data.model

import android.os.Parcel
import android.os.Parcelable

interface Reaction : Parcelable {
    val id: String
    val messageId: String
    val senderId: String
    val timestamp: Long
    val data: String
    val stability: Int

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(messageId)
        dest.writeString(senderId)
        dest.writeString(data)
        dest.writeLong(timestamp)
        dest.writeInt(stability)
    }
}

class FirebaseReactionModel(
    override val id: String = "",
    override val messageId: String = "",
    override val senderId: String = "",
    override var timestamp: Long = 0,
    override var data: String = "",
    override val stability: Int = 0,
) : Reaction {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readInt()
    )


    companion object CREATOR : Parcelable.Creator<FirebaseReactionModel> {
        override fun createFromParcel(parcel: Parcel): FirebaseReactionModel {
            return FirebaseReactionModel(parcel)
        }

        override fun newArray(size: Int): Array<FirebaseReactionModel?> {
            return arrayOfNulls(size)
        }
    }
}