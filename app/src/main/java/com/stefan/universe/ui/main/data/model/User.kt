package com.stefan.universe.ui.main.data.model

interface User {
    val uid: String
    val email: String
    val displayName: String
    val photoUri: String
    val emailVerified: Boolean
}

data class FirebaseUserModel(
    override val uid: String = "",
    override val email: String = "",
    override val displayName: String = "",
    override val photoUri: String = "",
    override val emailVerified: Boolean = false
) : User