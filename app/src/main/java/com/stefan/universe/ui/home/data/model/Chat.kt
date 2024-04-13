package com.stefan.universe.ui.home.data.model

import android.net.Uri

interface Chat {
    val id: String
    val name: String
    val latestMessage: String
    val photoUri: Uri
}

class FirebaseChatModel(
    override val id: String = "",
    override val name: String = "",
    override val latestMessage: String = "",
    override val photoUri: Uri = Uri.EMPTY
) : Chat