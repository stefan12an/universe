package com.stefan.universe.common.utils

import android.view.Window
import androidx.core.view.WindowCompat

object ViewUtils {

    fun Window.setLightStatusBars(b: Boolean) {
        WindowCompat.getInsetsController(this, decorView).isAppearanceLightStatusBars = b
    }
}