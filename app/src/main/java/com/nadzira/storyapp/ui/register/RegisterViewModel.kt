package com.nadzira.storyapp.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nadzira.storyapp.ui.Repository
import com.nadzira.storyapp.ui.Result

class RegisterViewModel(private val repository: Repository): ViewModel() {

    private val _registrationResult = MutableLiveData<Result<String>>()
    val registrationResult: LiveData<Result<String>> = _registrationResult

    fun register(name: String, email: String, password: String) {
        repository.register(name, email, password).observeForever { result ->
            _registrationResult.value = result!!
        }
    }
}