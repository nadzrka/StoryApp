package com.nadzira.storyapp.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
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

    val session: LiveData<UserModel> = userPreference.getSession().asLiveData()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _listStory = MutableLiveData<List<ListStoryItem>>()
    val listStory: LiveData<List<ListStoryItem>> = _listStory

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    suspend fun register(name: String, email: String, password: String) {
        showLoading(true)
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.register(name, email, password)
            }
            showLoading(false)
            if (response != null) {
                Log.d("Repository", "Registration successful: $response")
            } else {
                Log.e("Repository", "Registration failed: Response is null")
            }
        } catch (e: IOException) {
            showLoading(false)
            Log.e("Repository", "Registration failed due to network error: ${e.message}")
            throw e
        } catch (e: Exception) {
            showLoading(false)
            Log.e("Repository", "Registration failed: ${e.message}")
            throw e
        }
    }

    suspend fun login(email: String, password: String): UserModel {
        showLoading(true)
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.login(email, password)
            }
            showLoading(false)

            response.loginResult?.let {
                Log.d("Repository", "Login successful: $response")
                UserModel(
                    email = email,
                    token = it.token ?: "",
                    isLogin = true
                )
            } ?: throw Exception("Login response is null or invalid")
        } catch (e: IOException) {
            showLoading(false)
            Log.e("Repository", "Login failed due to network error: ${e.message}")
            throw e
        } catch (e: Exception) {
            showLoading(false)
            Log.e("Repository", "Login failed: ${e.message}")
            throw e
        }
    }

    private fun showLoading(isLoading: Boolean) {
        _isLoading.postValue(isLoading)
    }

    fun getStories(): LiveData<Result<List<ListStoryItem>>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)
        try {
            val response = apiService.getStories()
            val storyDetail = response.listStory

            if (response.error) {
                emit(Result.Error(response.message))
            } else if (storyDetail.isEmpty()) {
                emit(Result.Error("No stories found"))
            } else {
                emit(Result.Success(storyDetail))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }


    fun getDetailStory(storyId: String): LiveData<Result<Story>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)
        try {
            val response = apiService.getDetailStory(storyId)
            val storyDetail = response.story
            if (storyDetail != null) {
                emit(Result.Success(storyDetail))
            } else {
                emit(Result.Error("Story details not found"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }

    companion object {
        @Volatile
        private var instance: Repository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): Repository =
            instance ?: synchronized(this) {
                instance ?: Repository(userPreference, apiService)
            }.also { instance = it }
    }
}
