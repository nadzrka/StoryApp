package com.nadzira.storyapp.ui.add

import androidx.lifecycle.ViewModel
import com.nadzira.storyapp.ui.Repository

class NewViewModel(private val repository: Repository) : ViewModel() {

    fun getStories() = repository.getStories()
}