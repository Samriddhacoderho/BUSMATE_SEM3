package com.example.busmate.util

import android.content.Context

object SOSPrefs {

    private const val PREF_NAME = "sos_prefs"

    private fun keyForUser(userId: String): String {
        return "last_seen_sos_time_$userId"
    }

    fun getLastSeen(context: Context, userId: String): Long {
        return context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getLong(keyForUser(userId), 0L)
    }

    fun setLastSeen(context: Context, userId: String, timestamp: Long) {
        context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(keyForUser(userId), timestamp)
            .apply()
    }

    // Optional: clear on logout
    fun clear(context: Context, userId: String) {
        context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(keyForUser(userId))
            .apply()
    }
}
