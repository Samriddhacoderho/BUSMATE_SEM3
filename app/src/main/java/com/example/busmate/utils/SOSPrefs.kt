package com.example.busmate.util

import android.content.Context

object SOSPrefs {

    private const val PREF_NAME = "sos_prefs"
    private fun activeKey(userId: String) = "active_sos_$userId"


    private fun keyForUser(userId: String): String {
        return "last_seen_sos_time_$userId"
    }

    /* ────────────────
       KEYS
       ──────────────── */

    private fun lastSeenKey(userId: String) = "last_seen_sos_time_$userId"
    private fun sosTitleKey(userId: String) = "sos_title_$userId"
    private fun sosMessageKey(userId: String) = "sos_message_$userId"

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
    fun isSOSActive(context: Context, userId: String): Boolean {
        return context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(activeKey(userId), false)
    }
    fun setSOSActive(context: Context, userId: String, active: Boolean) {
        context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(activeKey(userId), active)
            .apply()
    }
    fun saveSOSContent(
        context: Context,
        userId: String,
        title: String,
        message: String
    ) {
        context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(sosTitleKey(userId), title)
            .putString(sosMessageKey(userId), message)
            .apply()
    }

    fun getSOSTitle(context: Context, userId: String): String {
        return context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(sosTitleKey(userId), "SOS ALERT")
            ?: "SOS ALERT"
    }

    fun getSOSMessage(context: Context, userId: String): String {
        return context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(
                sosMessageKey(userId),
                "An emergency has been reported."
            )
            ?: "An emergency has been reported."
    }



}
//testing sosprefs and sos alert bug
