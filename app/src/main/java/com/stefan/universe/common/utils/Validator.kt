package com.stefan.universe.common.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.stefan.universe.common.Resource
import com.stefan.universe.common.Status
import com.stefan.universe.ui.main.data.model.FirebaseUserModel
import com.stefan.universe.ui.main.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface Validator {
    suspend fun sendEmailVerification(): Resource<Boolean>
    suspend fun checkEmailVerification(): Resource<User>
    suspend fun updateVerifiedUserInDb(user: User): Resource<Boolean>
}

class FirebaseValidator @Inject constructor() : Validator {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override suspend fun sendEmailVerification() = runCatching {
        auth.currentUser?.sendEmailVerification()?.await()
        Resource.success(true)
    }.getOrElse { Resource.error(500, NullPointerException()) }

    override suspend fun checkEmailVerification() = runCatching {
        auth.currentUser?.reload()?.await()
        val user = auth.currentUser
        val updatedUser = FirebaseUserModel(
            user?.uid.orEmpty(),
            user?.email.orEmpty(),
            user?.displayName.orEmpty(),
            user?.photoUrl?.toString() ?: "",
            user?.isEmailVerified ?: false
        )
        updateVerifiedUserInDb(updatedUser)
        Resource(
            status = if (user != null) Status.SUCCESS else Status.ERROR,
            data = updatedUser
        )
    }.getOrElse { Resource.error(500, NullPointerException()) }

    override suspend fun updateVerifiedUserInDb(user: User) = runCatching {
        if (user.uid.isNotEmpty()) {
            db.collection("Users").document(user.uid).update("emailVerified", user.emailVerified).await()
            Resource.success(true)
        } else {
            Resource.error(500, Exception("User uid is empty"))
        }
    }.getOrElse { Resource.error(500, NullPointerException()) }
}