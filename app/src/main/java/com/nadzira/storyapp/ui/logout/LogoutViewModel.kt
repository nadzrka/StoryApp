package com.nadzira.storyapp.ui.logout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadzira.storyapp.ui.Repository
import kotlinx.coroutines.launch

class LogoutViewModel(private val repository: Repository) : ViewModel() {

    fun logout() {
        viewModelScope.launch {
            repository.clearSession()
        }
    }
}