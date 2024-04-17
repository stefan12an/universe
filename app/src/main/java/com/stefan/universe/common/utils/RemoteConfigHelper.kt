package com.stefan.universe.common.utils

import android.util.Log
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.stefan.universe.R
import com.stefan.universe.ui.auth.data.model.University

object RemoteConfigHelper {
    const val KEY_SUPPORTED_UNIVERSITIES = "supported_universities"

    fun initialize() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(10)
            .build()
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseRemoteConfig.apply {
            setDefaultsAsync(R.xml.remote_config_defaults)
            setConfigSettingsAsync(configSettings)
            fetchAndActivate().addOnCompleteListener { task ->
                if (!task.isSuccessful) Log.e("TAG", "initialize: ${task.exception}")
            }
            addOnConfigUpdateListener(object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    activate().addOnCompleteListener {
                        if (!it.isSuccessful) {
                            Log.e("TAG", "onUpdate: Remote config update failed")
                            return@addOnCompleteListener
                        }
                    }
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    Log.w("TAG", "Config update error with code: " + error.code, error)
                }
            })
        }
    }

    fun getBoolean(key: String): Boolean {
        return FirebaseRemoteConfig.getInstance().getBoolean(key)
    }

    fun getString(key: String): String {
        return FirebaseRemoteConfig.getInstance().getString(key)
    }

    fun getLong(key: String): Long {
        return FirebaseRemoteConfig.getInstance().getLong(key)
    }

    fun getDouble(key: String): Double {
        return FirebaseRemoteConfig.getInstance().getDouble(key)
    }

    private fun getJson(key: String): String {
        return FirebaseRemoteConfig.getInstance().getString(key)
    }

    fun getUniversitiesList(): List<University> {
        val jsonString = getJson(KEY_SUPPORTED_UNIVERSITIES)
        val universityListType = object : TypeToken<List<University>>() {}.type
        return Gson().fromJson(jsonString, universityListType)
    }
}