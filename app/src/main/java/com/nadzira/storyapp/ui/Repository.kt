package com.nadzira.storyapp.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.nadzira.storyapp.remote.response.ListStoryItem
import com.nadzira.storyapp.remote.response.Story
import com.nadzira.storyapp.remote.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class Repository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {
    private val _detailStory = MutableLiveData<Story>()
    val story: LiveData<Story> get() = _detailStory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _listStory = MutableLiveData<List<ListStoryItem>>()
    val listStory: LiveData<List<ListStoryItem>> get() = _listStory

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    suspend fun clearSession() {
        userPreference.logout()
    }

    suspend fun register(name: String, email: String, password: String) {
        showLoading(true)
        try {
            withContext(Dispatchers.IO) {
                apiService.register(name, email, password)
            }.also {
                Log.d("Repository", "Registration successful")
            }
        } catch (e: IOException) {
            Log.e("Repository", "Network error during registration: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e("Repository", "Registration failed: ${e.message}")
            throw e
        } finally {
            showLoading(false)
        }
    }

    suspend fun login(user: String, password: String): UserModel {
        showLoading(true)
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.login(user, password)
            }
            response.loginResult?.let {
                Log.d("Repository", "Login successful")
                UserModel(user = user, token = it.token.orEmpty(), isLogin = true)
            } ?: throw Exception("Invalid login response")
        } catch (e: IOException) {
            Log.e("Repository", "Network error during login: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e("Repository", "Login failed: ${e.message}")
            throw e
        } finally {
            showLoading(false)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        _isLoading.postValue(isLoading)
    }

    fun getStories(): LiveData<Result<List<ListStoryItem>>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)
        try {
            val response = apiService.getStories()
            if (response.error || response.listStory.isEmpty()) {
                emit(Result.Error("No stories found or error occurred"))
            } else {
                emit(Result.Success(response.listStory))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("Error: ${e.message}"))
        }
    }

    fun getDetailStory(storyId: String): LiveData<Result<Story>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)
        try {
            val response = apiService.getDetailStory(storyId)
            response.story?.let {
                emit(Result.Success(it))
            } ?: emit(Result.Error("Story details not found"))
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("Error: ${e.message}"))
        }
    }

    companion object {
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): Repository = Repository(userPreference, apiService)
    }
}
