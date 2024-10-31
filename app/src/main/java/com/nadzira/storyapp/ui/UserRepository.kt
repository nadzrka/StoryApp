package com.nadzira.storyapp.ui

import android.util.Log
import com.nadzira.storyapp.remote.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.IOException

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun register(name: String, email: String, password: String) {
        showLoading(true)
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.register(name, email, password)
            }
            showLoading(false)
            if (response != null) {
                Log.d("UserRepository", "Registration successful: $response")
            } else {
                Log.e("UserRepository", "Registration failed: Response is null")
            }
        } catch (e: IOException) {
            showLoading(false)
            Log.e("UserRepository", "Registration failed due to network error: ${e.message}")
            throw e // Re-throw to handle in the calling method
        } catch (e: Exception) {
            showLoading(false)
            Log.e("UserRepository", "Registration failed: ${e.message}")
            throw e // Re-throw to handle in the calling method
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
                Log.d("UserRepository", "Login successful: $response")
                UserModel(
                    email = email,
                    token = it.token ?: "",
                    isLogin = true
                )
            } ?: throw Exception("Login response is null or invalid")
        } catch (e: IOException) {
            showLoading(false)
            Log.e("UserRepository", "Login failed due to network error: ${e.message}")
            throw e // Re-throw to handle in the calling method
        } catch (e: Exception) {
            showLoading(false)
            Log.e("UserRepository", "Login failed: ${e.message}")
            throw e // Re-throw to handle in the calling method
        }
    }

    private fun showLoading(isLoading: Boolean) {
        // Implement loading indicator logic here
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService)
            }.also { instance = it }
    }
}
