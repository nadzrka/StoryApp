package com.nadzira.storyapp.ui.add

import androidx.lifecycle.ViewModel
import com.nadzira.storyapp.ui.Repository
import java.io.File

class AddViewModel(private val repository: Repository) : ViewModel() {
  fun uploadImage(file: File, description: String, lat: Float?, lon: Float?) = repository.uploadImage(file, description, lat, lon)
}