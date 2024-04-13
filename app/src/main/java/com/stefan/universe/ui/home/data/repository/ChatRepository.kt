package com.stefan.universe.ui.home.data.repository

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.stefan.universe.common.Resource
import com.stefan.universe.common.Status
import com.stefan.universe.ui.home.data.model.Chat
import com.stefan.universe.ui.main.data.model.FirebaseUserModel
import com.stefan.universe.ui.main.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface ChatRepository {
    suspend fun getUsers(): Resource<List<User>>
    suspend fun getUserChats(): Resource<List<Chat>>
    suspend fun startChat(): Resource<Boolean>
}

class ChatRepositoryImpl @Inject constructor(private val app: Application) : ChatRepository {
    override suspend fun getUsers(): Resource<List<User>> {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        return try {
            val snapshot = db.collection("Users").get().await()
            val userList = snapshot.filter { it.id != currentUser?.uid }
                .map { it.toObject(FirebaseUserModel::class.java) }
            Resource.success(userList)
        } catch (e: Exception) {
            Resource(Status.ERROR, emptyList(), errorCode = 500, exception = e)
        }
    }

    override suspend fun getUserChats(): Resource<List<Chat>> {
        val db = FirebaseFirestore.getInstance()
        return Resource(status = Status.SUCCESS, data = listOf())
    }

    override suspend fun startChat(): Resource<Boolean> {
        return Resource(status = Status.SUCCESS, data = true)
    }


}