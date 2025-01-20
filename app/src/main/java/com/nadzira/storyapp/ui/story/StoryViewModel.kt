package com.nadzira.storyapp.ui.story

import androidx.lifecycle.ViewModel
import com.nadzira.storyapp.ui.Repository

class StoryViewModel(private val repository: Repository) : ViewModel() {

    fun getStories() = repository.getStories()
}