package com.stefan.universe

import android.app.Application
import com.google.firebase.FirebaseApp
import com.stefan.universe.common.utils.RemoteConfigHelper
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        RemoteConfigHelper.initialize()
    }
}