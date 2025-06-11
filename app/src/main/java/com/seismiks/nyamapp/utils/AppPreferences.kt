package com.seismiks.nyamapp.utils

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    const val PREF_NAME = "UserPrefs"
    const val KEY_USER_ID = "firebase_user_id"

    fun saveUserIdToPreferences(context: Context, userId: String?) {
        if (userId == null) {
            getSharedPreferences(context).edit().remove(AppPreferences.KEY_USER_ID).apply()
            return
        }
        val editor: SharedPreferences.Editor = getSharedPreferences(context).edit()
        editor.putString(AppPreferences.KEY_USER_ID, userId)
        editor.apply()
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(AppPreferences.PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getUserIdFromPreferences(context: Context): String? {
        return getSharedPreferences(context).getString(AppPreferences.KEY_USER_ID, null)
    }

    fun clearUserIdFromPreferences(context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.remove(AppPreferences.KEY_USER_ID)
        editor.apply()
    }
}