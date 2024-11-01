package com.nadzira.storyapp.di

import android.content.Context
import com.nadzira.storyapp.remote.retrofit.ApiConfig
import com.nadzira.storyapp.ui.UserPreference
import com.nadzira.storyapp.ui.Repository
import com.nadzira.storyapp.ui.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): Repository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService = ApiConfig.getApiService(user.token)
        return Repository.getInstance(pref, apiService)
    }
}