package com.nadzira.storyapp.remote.response

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story")
data class StoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val imageUrl: String
)
