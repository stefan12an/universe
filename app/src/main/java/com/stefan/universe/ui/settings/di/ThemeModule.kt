package com.stefan.universe.ui.settings.di

import com.stefan.universe.ui.settings.data.repository.ThemeRepository
import com.stefan.universe.ui.settings.data.repository.ThemeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ThemeModule {
    @Binds
    abstract fun bindThemeRepository(themeRepositoryImpl: ThemeRepositoryImpl): ThemeRepository
}