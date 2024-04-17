package com.stefan.universe.ui.main.data.model

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

interface User : Parcelable {
    val uid: String
    val email: String
    val displayName: String
    val university: String
    val faculty: String
    val description: String
    val photoUri: String
    val chats: List<String>
    val emailVerified: Boolean
    val gender: String
    val birthDate: Date

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(uid)
        dest.writeString(email)
        dest.writeString(displayName)
        dest.writeString(photoUri)
        dest.writeInt(if (emailVerified) 1 else 0)
        dest.writeString(university)
        dest.writeString(faculty)
        dest.writeString(description)
        dest.writeString(gender)
        dest.writeLong(birthDate.time)
    }
}

data class FirebaseUserModel(
    override val uid: String = "",
    override val email: String = "",
    override val displayName: String = "",
    override val photoUri: String = "",
    override val chats: List<String> = emptyList(),
    override val emailVerified: Boolean = false,
    override val university: String = "",
    override val faculty: String = "",
    override val description: String = "",
    override val gender: String = "",
    override val birthDate: Date = Date()
) : User {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        mutableListOf<String>().apply {
            parcel.readStringList(this)
        },
        parcel.readInt() != 0,
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        Date(parcel.readLong())
    )

    companion object CREATOR : Parcelable.Creator<FirebaseUserModel> {
        override fun createFromParcel(parcel: Parcel): FirebaseUserModel {
            return FirebaseUserModel(parcel)
        }

        override fun newArray(size: Int): Array<FirebaseUserModel?> {
            return arrayOfNulls(size)
        }
    }
}
