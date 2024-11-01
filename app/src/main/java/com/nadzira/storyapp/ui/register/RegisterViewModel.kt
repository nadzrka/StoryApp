package com.nadzira.storyapp.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadzira.storyapp.ui.Repository
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: Repository): ViewModel() {

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            repository.register(name, email, password)
        }
    }
}