package com.nadzira.storyapp.ui.detail

import androidx.lifecycle.ViewModel
import com.nadzira.storyapp.ui.Repository

class DetailViewModel(private val repository: Repository) : ViewModel() {

    fun getDetailStory(storyId: String) = repository.getDetailStory(storyId)
}
