package com.stefan.universe.common.utils

import android.content.Context
import android.content.res.Configuration

object ThemeUtils {

    fun isNightMode(context: Context): Boolean {
        val currentNightMode =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}