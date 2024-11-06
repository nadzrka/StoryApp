package com.nadzira.storyapp.di

import android.content.Context
import com.nadzira.storyapp.remote.retrofit.ApiConfig
import com.nadzira.storyapp.ui.UserPreference
import com.nadzira.storyapp.ui.Repository
object Injection {
    fun provideRepository(context: Context): Repository {
        val pref = UserPreference(context)
        val user = pref.getSession()
        val apiService = ApiConfig.getApiService(user.token!!)
        return Repository.getInstance(pref, apiService)
    }
}