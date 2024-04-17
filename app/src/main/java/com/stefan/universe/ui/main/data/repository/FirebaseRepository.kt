package com.stefan.universe.ui.main.data.repository

import android.app.Application
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.stefan.universe.common.Resource
import com.stefan.universe.common.utils.Authenticator
import com.stefan.universe.common.utils.Validator
import com.stefan.universe.ui.main.data.model.FirebaseUserModel
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

interface FirebaseRepository {
    suspend fun authenticate(email: String, password: String): Resource<FirebaseUserModel>
    suspend fun register(email: String, password: String): Resource<Boolean>
    suspend fun logout(): Resource<Boolean>
    suspend fun editProfile(
        displayName: String? = null,
        uri: Uri? = null,
        university: String?,
        faculty: String? = null,
        gender: String? = null,
        birthDate: Date? = null,
    ): Resource<Boolean>

    suspend fun sendEmailVerification(): Resource<Boolean>
    suspend fun checkEmailVerification(): Resource<FirebaseUserModel>
    suspend fun getAuthUser(): Resource<FirebaseUserModel>
    suspend fun getUser(): Resource<FirebaseUserModel>
    suspend fun getUserById(id: String?): Resource<FirebaseUserModel>
}

class FirebaseRepositoryImpl @Inject constructor(
    private val context: Application,
    private val authenticator: Authenticator,
    private val validator: Validator
) :
    FirebaseRepository {

    override suspend fun authenticate(
        email: String,
        password: String
    ): Resource<FirebaseUserModel> {
        return authenticator.authenticate(email, password) as Resource<FirebaseUserModel>
    }

    override suspend fun register(email: String, password: String): Resource<Boolean> {
        return authenticator.register(email, password)
    }

    override suspend fun logout(): Resource<Boolean> {
        return authenticator.logout()
    }

    override suspend fun editProfile(
        displayName: String?,
        uri: Uri?,
        university: String?,
        faculty: String?,
        gender: String?,
        birthDate: Date?
    ): Resource<Boolean> {
        return authenticator.editProfile(displayName, uri, university, faculty, gender, birthDate)
    }

    override suspend fun sendEmailVerification(): Resource<Boolean> {
        return validator.sendEmailVerification()
    }

    override suspend fun checkEmailVerification(): Resource<FirebaseUserModel> {
        return validator.checkEmailVerification() as Resource<FirebaseUserModel>
    }

    override suspend fun getAuthUser(): Resource<FirebaseUserModel> {
        return authenticator.getAuthUser() as Resource<FirebaseUserModel>
    }

    override suspend fun getUser(): Resource<FirebaseUserModel> {
        return authenticator.getUser() as Resource<FirebaseUserModel>
    }

    override suspend fun getUserById(id: String?): Resource<FirebaseUserModel> {
        val db = FirebaseFirestore.getInstance()
        return try {
            val user = id?.let { db.collection("Users").document(it).get().await() }
            Resource.success(user?.toObject(FirebaseUserModel::class.java)!!)
        } catch (e: Exception) {
            Resource.error(500, e)
        }
    }
}