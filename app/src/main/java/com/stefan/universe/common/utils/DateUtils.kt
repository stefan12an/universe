package com.stefan.universe.common.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


object DateUtils {

    fun getTime(milliSeconds: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds

        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    fun isUserAtLeast18(birthDate: Date): Boolean {
        val cal = Calendar.getInstance()
        cal.time = Date() // set current date
        cal.add(Calendar.YEAR, -18) // subtract 18 years
        val eighteenYearsAgo = cal.time
        return !birthDate.after(eighteenYearsAgo) // if birthDate is after eighteenYearsAgo, user is not 18 yet
    }

    fun getDate(milliSeconds: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    fun getDayOrDate(milliSeconds: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds

        val currentDate = Calendar.getInstance()

        // Create a Calendar instance for yesterday
        val yesterdayDate = Calendar.getInstance()
        yesterdayDate.add(Calendar.DAY_OF_YEAR, -1)

        return when {
            // If the timestamp is for today
            calendar[Calendar.YEAR] == currentDate[Calendar.YEAR] &&
                    calendar[Calendar.DAY_OF_YEAR] == currentDate[Calendar.DAY_OF_YEAR] -> {
                "Today"
            }
            // If the timestamp is for yesterday
            calendar[Calendar.YEAR] == yesterdayDate[Calendar.YEAR] &&
                    calendar[Calendar.DAY_OF_YEAR] == yesterdayDate[Calendar.DAY_OF_YEAR] -> {
                "Yesterday"
            }
            // If the timestamp is in the current week
            calendar[Calendar.YEAR] == currentDate[Calendar.YEAR] &&
                    calendar[Calendar.WEEK_OF_YEAR] == currentDate[Calendar.WEEK_OF_YEAR] -> {
                val formatter = SimpleDateFormat("EEEE", Locale.getDefault())
                formatter.format(calendar.time)
            }
            // If the timestamp is older than the current week but in the same year
            calendar[Calendar.YEAR] == currentDate[Calendar.YEAR] -> {
                val formatter = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
                formatter.format(calendar.time)
            }
            // If the timestamp is from a previous year
            else -> {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formatter.format(calendar.time)
            }
        }
    }

    fun isNewDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }

        return cal1[Calendar.YEAR] != cal2[Calendar.YEAR] ||
                cal1[Calendar.DAY_OF_YEAR] != cal2[Calendar.DAY_OF_YEAR]
    }
}