package com.stefan.universe.ui.main.di

import android.app.Application
import com.stefan.universe.common.utils.FirebaseAuthenticator
import com.stefan.universe.common.utils.FirebaseValidator
import com.stefan.universe.ui.main.data.repository.FirebaseRepository
import com.stefan.universe.ui.main.data.repository.FirebaseRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseRepository(
        app: Application,
        authenticator: FirebaseAuthenticator,
        validator: FirebaseValidator
    ): FirebaseRepository {
        return FirebaseRepositoryImpl(app, authenticator, validator)
    }
}