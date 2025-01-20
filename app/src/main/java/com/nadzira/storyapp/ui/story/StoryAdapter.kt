package com.nadzira.storyapp.ui.story

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nadzira.storyapp.R
import com.nadzira.storyapp.databinding.ItemRowStoryBinding
import com.nadzira.storyapp.remote.response.StoryEntity

class StoryAdapter(
    private val onItemClick: (StoryEntity) -> Unit
) : PagingDataAdapter<StoryEntity, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemRowStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story)
        }
    }

    inner class StoryViewHolder(private val binding: ItemRowStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: StoryEntity) {
            binding.tvItemName.text = story.name

            if (story.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(story.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(binding.ivItemPhoto)
            } else {
                binding.ivItemPhoto.setImageResource(R.drawable.placeholder)
            }

            itemView.setOnClickListener {
                onItemClick(story)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryEntity>() {
            override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
