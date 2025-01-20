package com.nadzira.storyapp.ui.maps

import androidx.lifecycle.ViewModel
import com.nadzira.storyapp.ui.Repository

class MapsViewModel(private val repository: Repository) : ViewModel() {
    fun getStoriesWithLoc() = repository.getStoriesWithLoc()
}