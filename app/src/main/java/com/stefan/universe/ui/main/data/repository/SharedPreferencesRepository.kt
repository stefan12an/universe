package com.stefan.universe.ui.main.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface SharedPreferencesRepository {

}

class SharedPreferencesRepositoryImpl @Inject constructor(@ApplicationContext val context: Context) :
    SharedPreferencesRepository {

        init {
            Log.e("TAG", ": (\"SharedPreferencesRepositoryImpl created\")", )
        }
}