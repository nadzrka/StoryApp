package com.nadzira.storyapp

import com.nadzira.storyapp.remote.response.StoryEntity

object DataDummy {

    fun generateDummyStoryEntity(): List<StoryEntity> {
        val items: MutableList<StoryEntity> = arrayListOf()
        for (i in 0..100) {
            val story = StoryEntity(
                i.toString(),
                "name + $i",
                "description $i",
                "photoUrl $i"
            )
            items.add(story)
        }
        return items
    }
}