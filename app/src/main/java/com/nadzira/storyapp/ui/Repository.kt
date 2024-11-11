package com.nadzira.storyapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.nadzira.storyapp.remote.response.FileUploadResponse
import com.nadzira.storyapp.remote.response.ListStoryItem
import com.nadzira.storyapp.remote.response.Story
import com.nadzira.storyapp.remote.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
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

    fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun clearSession() {
        userPreference.logout()
    }

    fun register(name: String, email: String, password: String): LiveData<Result<String>> = liveData {
      emit(Result.Loading)
        try {
            withContext(Dispatchers.IO) {
                apiService.register(name, email, password)
            }
            emit(Result.Success("Registration successful"))
        } catch (e: IOException) {
            emit(Result.Error("Network error: : ${e.message}"))
        } catch (e: Exception) {
           emit(Result.Error("Registration failed: ${e.message}"))
        }
    }

    fun login(user: String, password: String):  LiveData<Result<UserModel>> = liveData {
        emit(Result.Loading)
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.login(user, password)
            }
            val userModel = response.loginResult?.let {
                UserModel(user = user, token = it.token.orEmpty(), isLogin = true)
            } ?: throw Exception("Invalid login response")
            emit(Result.Success(userModel))
        } catch (e: IOException) {
            emit(Result.Error("Network error: : ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("Login failed: ${e.message}"))
        }
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
            response.story.let {
                emit(Result.Success(it))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("Error: ${e.message}"))
        }
    }

    fun uploadImage(imageFile: File, description: String) = liveData {
        emit(Result.Loading)
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            requestImageFile
        )
        try {
            val successResponse = apiService.addStory(multipartBody, requestBody)
            emit(Result.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, FileUploadResponse::class.java)
            emit(Result.Error(errorResponse.message!!))
        }

    }

    companion object {
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): Repository = Repository(userPreference, apiService)
    }
}
