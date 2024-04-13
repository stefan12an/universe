package com.stefan.universe.ui.main.di

import android.app.Application
import com.stefan.universe.common.utils.FirebaseAuthenticator
import com.stefan.universe.ui.main.data.repository.FirebaseRepository
import com.stefan.universe.ui.main.data.repository.FirebaseRepositoryImpl
import com.stefan.universe.ui.main.data.repository.SharedPreferencesRepository
import com.stefan.universe.ui.main.data.repository.SharedPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SharedPreferencesModule {
    @Binds
    abstract fun provideSharedPreferencesRepository(impl: SharedPreferencesRepositoryImpl): SharedPreferencesRepository

}