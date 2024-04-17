package com.stefan.universe.ui.settings.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.stefan.universe.common.constants.PreferencesConstants
import com.stefan.universe.ui.settings.ui.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface ThemeRepository {
    suspend fun setTheme(theme: Theme)
    fun getTheme(): Flow<Theme>
}

class ThemeRepositoryImpl @Inject constructor(private val dataStore: DataStore<Preferences>) :
    ThemeRepository {
    override suspend fun setTheme(theme: Theme) {
        dataStore.edit { settings ->
            settings[PreferencesConstants.THEME_KEY] = theme.name
        }
    }

    override fun getTheme(): Flow<Theme> {
        return dataStore.data.map { settings ->
            when (val theme = settings[PreferencesConstants.THEME_KEY]) {
                null -> Theme.SYSTEM
                else -> Theme.valueOf(theme)
            }
        }
    }
}