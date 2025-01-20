package com.nadzira.storyapp.ui.story

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.nadzira.storyapp.ui.maps.MapsActivity
import com.nadzira.storyapp.databinding.ActivityStoryBinding
import com.nadzira.storyapp.ui.Result
import com.nadzira.storyapp.ui.UserPreference
import com.nadzira.storyapp.ui.ViewModelFactory
import com.nadzira.storyapp.ui.detail.DetailActivity
import kotlinx.coroutines.launch
import com.nadzira.storyapp.R
import com.nadzira.storyapp.di.Injection
import com.nadzira.storyapp.remote.response.StoryEntity
import com.nadzira.storyapp.ui.add.AddActivity
import com.nadzira.storyapp.ui.logout.LogoutActivity

class StoryActivity : AppCompatActivity() {
    private var _binding: ActivityStoryBinding? = null
    private val binding get() = _binding
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var userPreference: UserPreference
    private val storyViewModel by viewModels<StoryViewModel> {
        ViewModelFactory(Injection.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        userPreference = UserPreference(this)
        setSupportActionBar(binding?.toolbar)
        supportActionBar?.title = getString(R.string.story)

        binding?.mapButton?.setOnClickListener{
            startActivity(Intent(this, MapsActivity::class.java))
        }

        setupRecyclerView()
        observeUserSession()
    }

    private fun observeUserSession() {
        lifecycleScope.launch {
            val session = userPreference.getSession()
            val token = session.token
            if (token != null) {
                observeStories(token)
            } else {
                showError(getString(R.string.user_session_not_found))
            }
        }
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter { navigateToDetailEvent(it) }
        binding?.rvStory?.apply {
            layoutManager = LinearLayoutManager(context)
            binding?.rvStory?.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = storyAdapter.withLoadStateFooter(
                    footer = LoadingStateAdapter { storyAdapter.retry() }
                )
            }
        }
        val fab: View = findViewById(R.id.addButton)
        fab.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.setting_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, LogoutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun observeStories(token: String) {
        if (token.isNotEmpty()) {
            storyViewModel.getStories().observe(this) { result ->
                Log.d("StoryActivity", "API result: $result")
                handleResult(result)
            }
        } else {
            showError(getString(R.string.user_is_not_logged_in))
        }
    }

    private fun handleResult(result: Result<PagingData<StoryEntity>>?) {
        when (result) {
            is Result.Loading -> showLoading(true)
            is Result.Success -> {
                storyAdapter.submitData(lifecycle, result.data)
                showLoading(false)
            }
            is Result.Error -> {
                showLoading(false)
                Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
            }

            null -> TODO()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding?.tvNoItem?.visibility = View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToDetailEvent(story: StoryEntity) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_STORY_ID, story.id)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        storyViewModel.getStories().observe(this) { result ->
            if (result is Result.Success) {
                storyAdapter.submitData(lifecycle, result.data)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
