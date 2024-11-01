package com.nadzira.storyapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadzira.storyapp.ui.UserModel
import com.nadzira.storyapp.ui.Repository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: Repository) : ViewModel() {

    private val _loginResult = MutableLiveData<UserModel?>()
    val loginResult: LiveData<UserModel?> = _loginResult

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = repository.login(email, password)
                _loginResult.value = user
            } catch (e: Exception) {
                _loginResult.value = null
            }
        }
    }
}