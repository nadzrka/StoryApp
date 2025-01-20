package com.nadzira.storyapp.ui

import android.content.Context

class UserPreference(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(value: UserModel) {
        val editor = preferences.edit()
        editor.putString(USER, value.user ?: "")
        editor.putString(TOKEN, value.token ?: "")
        editor.putBoolean(IS_LOGIN, value.isLogin)
        editor.apply()
    }

    fun getSession(): UserModel {
        val model = UserModel()
        model.user = preferences.getString(USER, "") ?: ""
        model.token = preferences.getString(TOKEN, "") ?: ""
        model.isLogin = preferences.getBoolean(IS_LOGIN, false)
        return model
    }

    fun logout() {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        private const val PREFS_NAME = "user_pref"
        private const val USER = "user"
        private const val TOKEN = "token"
        private const val IS_LOGIN = "is_login"
    }
}
