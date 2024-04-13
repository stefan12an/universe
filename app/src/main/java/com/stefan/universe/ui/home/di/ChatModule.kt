package com.stefan.universe.ui.home.di

import android.app.Application
import com.stefan.universe.ui.home.data.repository.ChatRepository
import com.stefan.universe.ui.home.data.repository.ChatRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {
    @Provides
    @Singleton
    fun provideChatRepository(
        app: Application,
    ): ChatRepository {
        return ChatRepositoryImpl(app)
    }
}