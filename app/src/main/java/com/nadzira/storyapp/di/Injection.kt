package com.nadzira.storyapp.di

import android.content.Context
import com.nadzira.storyapp.remote.retrofit.ApiConfig
import com.nadzira.storyapp.ui.UserPreference
import com.nadzira.storyapp.ui.UserRepository
import com.nadzira.storyapp.ui.dataStore

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return UserRepository.getInstance(pref, apiService)
    }
}