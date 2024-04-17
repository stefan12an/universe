package com.stefan.universe.common.utils

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.stefan.universe.common.Resource
import com.stefan.universe.ui.main.data.model.FirebaseUserModel
import com.stefan.universe.ui.main.data.model.User
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

interface Authenticator {
    suspend fun authenticate(username: String, password: String): Resource<User>
    suspend fun register(username: String, password: String): Resource<Boolean>
    suspend fun logout(): Resource<Boolean>
    suspend fun editProfile(
        displayName: String?,
        uri: Uri?,
        university: String?,
        faculty: String?,
        gender: String?,
        birthDate: Date?
    ): Resource<Boolean>

    suspend fun getAuthUser(): Resource<User>
    suspend fun getUser(): Resource<User>
    suspend fun updateAdditionalUserDetailsInDb(user: User): Resource<Boolean>
}

class FirebaseAuthenticator @Inject constructor() : Authenticator {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    override suspend fun authenticate(username: String, password: String) = runCatching {
        val result = auth.signInWithEmailAndPassword(username, password).await()
        val user = result.user
        Resource.success(
            FirebaseUserModel(
                user?.uid.orEmpty(),
                user?.email.orEmpty(),
                user?.displayName.orEmpty(),
                user?.photoUrl?.toString() ?: "",
                emptyList(),
                user?.isEmailVerified ?: false
            )
        )
    }.getOrElse { Resource.error(500, NullPointerException()) }

    override suspend fun register(username: String, password: String) = runCatching {
        val result = auth.createUserWithEmailAndPassword(username, password).await()
        result.user?.sendEmailVerification()?.await()
        val user = FirebaseUserModel(
            result.user?.uid.orEmpty(),
            result.user?.email.orEmpty(),
        )
        db.collection("Users").document(user.uid).set(user).await()
        Resource.success(true)
    }.getOrElse { Resource.error(500, NullPointerException()) }

    override suspend fun logout() = runCatching {
        auth.signOut()
        Resource.success(true)
    }.getOrElse { Resource.error(500, NullPointerException()) }

    override suspend fun editProfile(
        displayName: String?,
        uri: Uri?,
        university: String?,
        faculty: String?,
        gender: String?,
        birthDate: Date?
    ): Resource<Boolean> = runCatching {
        val user = auth.currentUser
        var photoUri: Uri? = null
        if (uri != null) {
            val fileLocation = "users/${user?.uid}/profile.jpg"
            val fileRef = storage.getReference(fileLocation)
            val uploadResponse = fileRef.putFile(uri).await()
            if (uploadResponse.task.isSuccessful) {
                photoUri = fileRef.downloadUrl.await()
            }
        }
        user?.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .setPhotoUri(photoUri)
                .build()
        )?.await()
        updateAdditionalUserDetailsInDb(
            FirebaseUserModel(
                uid = user?.uid.orEmpty(),
                displayName = displayName.orEmpty(),
                photoUri = photoUri?.toString().orEmpty(),
                university = university.orEmpty(),
                faculty = faculty.orEmpty(),
                gender = gender.orEmpty(),
                birthDate = birthDate ?: Date()
            )
        )
        Resource.success(true)
    }.getOrElse { Resource.error(500, NullPointerException()) }

    override suspend fun getAuthUser() = runCatching {
        val user = auth.currentUser
        Resource.success(
            FirebaseUserModel(
                user?.uid.orEmpty(),
                user?.email.orEmpty(),
                user?.displayName.orEmpty(),
                user?.photoUrl?.toString() ?: "",
                emptyList(),
                user?.isEmailVerified ?: false
            )
        )
    }.getOrElse { Resource.error(500, NullPointerException()) }

    override suspend fun getUser(): Resource<User> {
        val user = db.collection("Users").document(auth.currentUser?.uid.orEmpty()).get().await()
        return if (user.exists()) {
            Resource.success(user.toObject(FirebaseUserModel::class.java))
        } else {
            Resource.error(500, NullPointerException())
        }
    }

    override suspend fun updateAdditionalUserDetailsInDb(user: User) = runCatching {
        if (user.uid.isNotEmpty()) {
            db.collection("Users").document(user.uid)
                .update(
                    mapOf(
                        "displayName" to user.displayName,
                        "photoUri" to user.photoUri,
                        "university" to user.university,
                        "faculty" to user.faculty,
                        "gender" to user.gender,
                        "birthDate" to user.birthDate
                    )
                )
                .await()
            Resource.success(true)
        } else {
            Resource.error(500, Exception("User uid is empty"))
        }
    }.getOrElse { Resource.error(500, NullPointerException()) }
}