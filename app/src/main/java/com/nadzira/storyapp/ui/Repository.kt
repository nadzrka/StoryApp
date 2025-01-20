package com.nadzira.storyapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.google.gson.Gson
import com.nadzira.storyapp.data.StoryRemoteMediator
import com.nadzira.storyapp.database.StoryDatabase
import com.nadzira.storyapp.remote.response.FileUploadResponse
import com.nadzira.storyapp.remote.response.ListStoryItem
import com.nadzira.storyapp.remote.response.Story
import com.nadzira.storyapp.remote.response.StoryEntity
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
    private val storyDatabase: StoryDatabase,
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {
    private val _detailStory = MutableLiveData<Story>()
    val story: LiveData<Story> get() = _detailStory

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
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string()?.let {
                Gson().fromJson(it, FileUploadResponse::class.java).message
            } ?: "Registration failed"
            emit(Result.Error(errorMessage))
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message ?: "Unable to connect"}"))
        } catch (e: Exception) {
            emit(Result.Error("Registration failed: ${e.message ?: "Unknown error"}"))
        }
    }


    fun login(user: String, password: String): LiveData<Result<UserModel>> = liveData {
        emit(Result.Loading)
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.login(user, password)
            }
            val userModel = response.loginResult?.let {
                UserModel(user = user, token = it.token.orEmpty(), isLogin = true)
            } ?: throw Exception("Invalid login response")
            emit(Result.Success(userModel))
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string()?.let {
                Gson().fromJson(it, FileUploadResponse::class.java).message
            } ?: "Login failed"
            emit(Result.Error(errorMessage))
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message ?: "Unable to connect"}"))
        } catch (e: Exception) {
            emit(Result.Error("Login failed: ${e.message ?: "Unknown error"}"))
        }
    }

    fun getStories(): LiveData<Result<PagingData<StoryEntity>>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)
        try {
            @OptIn(ExperimentalPagingApi::class)
            val pager = Pager(
                config = PagingConfig(
                    pageSize = 5
                ),
                remoteMediator = StoryRemoteMediator(storyDatabase, apiService),
                pagingSourceFactory = {
                    storyDatabase.storyDao().getAllStory()
                }
            )
            emitSource(pager.liveData.map { pagingData ->
                Result.Success(pagingData)
            })
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message ?: "Unable to connect"}"))
        } catch (e: Exception) {
            emit(Result.Error("Error: ${e.message ?: "Unknown error"}"))
        }
    }

    fun getStoriesWithLoc(): LiveData<Result<List<ListStoryItem>>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)
        try {
            val response = apiService.getStoriesWithLocation()
            if (response.error || response.listStory.isEmpty()) {
                emit(Result.Error("No stories found or error occurred"))
            } else {
                emit(Result.Success(response.listStory))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message ?: "Unable to connect"}"))
        } catch (e: Exception) {
            emit(Result.Error("Error: ${e.message ?: "Unknown error"}"))
        }
    }

    fun getDetailStory(storyId: String): LiveData<Result<Story>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)
        try {
            val response = apiService.getDetailStory(storyId)
            emit(Result.Success(response.story))
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message ?: "Unable to connect"}"))
        } catch (e: Exception) {
            emit(Result.Error("Error: ${e.message ?: "Unknown error"}"))
        }
    }

    fun uploadImage(imageFile: File, description: String, lat: Float?, lon: Float?) = liveData(Dispatchers.IO) {
        emit(Result.Loading)
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            requestImageFile
        )
        val latRequestBody = lat?.toString()?.toRequestBody("text/plain".toMediaType())
        val lonRequestBody = lon?.toString()?.toRequestBody("text/plain".toMediaType())
        try {
            val successResponse = apiService.addStory(multipartBody, requestBody,
                latRequestBody, lonRequestBody)
            emit(Result.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let {
                Gson().fromJson(it, FileUploadResponse::class.java)
            }
            emit(Result.Error(errorResponse?.message ?: "Failed to upload image"))
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message ?: "Unable to connect"}"))
        } catch (e: Exception) {
            emit(Result.Error("Error: ${e.message ?: "Unknown error"}"))
        }
    }

    companion object {
        fun getInstance(
            storyDatabase: StoryDatabase,
            userPreference: UserPreference,
            apiService: ApiService
        ): Repository = Repository(storyDatabase, userPreference, apiService)
    }
}
