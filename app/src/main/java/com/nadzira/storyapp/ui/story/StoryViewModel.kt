package com.nadzira.storyapp.ui.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nadzira.storyapp.ui.Repository

class StoryViewModel(private val repository: Repository) : ViewModel() {

    private val _token = MutableLiveData<String?>()
    val token: LiveData<String?> get() = _token

    fun getStories() = repository.getStories()
}