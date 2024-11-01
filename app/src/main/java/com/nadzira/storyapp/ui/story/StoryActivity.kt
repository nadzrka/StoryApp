package com.nadzira.storyapp.ui.story

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nadzira.storyapp.databinding.ActivityStoryBinding
import com.nadzira.storyapp.remote.response.ListStoryItem
import com.nadzira.storyapp.ui.Result
import com.nadzira.storyapp.ui.UserPreference
import com.nadzira.storyapp.ui.ViewModelFactory
import com.nadzira.storyapp.ui.dataStore
import com.nadzira.storyapp.ui.detail.DetailActivity
import kotlinx.coroutines.launch
import com.nadzira.storyapp.R
import com.nadzira.storyapp.ui.add.NewActivity

class StoryActivity : AppCompatActivity() {
    private var _binding: ActivityStoryBinding? = null
    private val binding get() = _binding
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var userPreference: UserPreference
    private val storyViewModel by viewModels<StoryViewModel> {
        ViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        userPreference = UserPreference.getInstance(dataStore)

        setupRecyclerView()
        observeUserSession()
    }

    private fun observeUserSession() {
        lifecycleScope.launch {
            userPreference.getSession().collect { user ->
                val userToken = user.token
                observeStories(userToken)
            }
        }
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter { navigateToDetailEvent(it) }
        binding?.rvEvent?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = storyAdapter
        }
        val fab: View = findViewById(R.id.addButton)
        fab.setOnClickListener { view ->
            val intent = Intent(this, NewActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeStories(token: String) {
        if (token.isNotEmpty()) {
            storyViewModel.getStories().observe(this) { result ->
                Log.d("StoryActivity", "API result: $result")
                handleResult(result)
            }
        } else {
            showError("User is not logged in.")
            finish()
        }
    }

    private fun handleResult(result: Result<List<ListStoryItem>>) {
        when (result) {
            is Result.Loading -> showLoading(true)
            is Result.Success -> {
                updateEventList(result.data)
                showLoading(false)
            }
            is Result.Error -> {
                showLoading(false)
                showError("Failed to load stories")
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding?.tvNoEvent?.visibility = View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateEventList(stories: List<ListStoryItem>) {
        if (stories.isEmpty()) {
            binding?.tvNoEvent?.visibility = View.VISIBLE
            binding?.rvEvent?.visibility = View.GONE
        } else {
            binding?.tvNoEvent?.visibility = View.GONE
            binding?.rvEvent?.visibility = View.VISIBLE
            storyAdapter.submitList(stories)
        }
    }

    private fun navigateToDetailEvent(story: ListStoryItem) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_STORY_ID, story.id.toString())
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        storyViewModel.getStories().observe(this) { result ->
            if (result is Result.Success) {
                storyAdapter.submitList(result.data)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
