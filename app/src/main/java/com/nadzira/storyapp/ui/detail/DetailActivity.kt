package com.nadzira.storyapp.ui.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.nadzira.storyapp.R
import com.nadzira.storyapp.databinding.ActivityDetailBinding
import com.nadzira.storyapp.di.Injection
import com.nadzira.storyapp.remote.response.Story
import com.nadzira.storyapp.ui.Result
import com.nadzira.storyapp.ui.ViewModelFactory

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val detailViewModel by viewModels<DetailViewModel> {
        ViewModelFactory(Injection.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.overflowIcon?.setTint(ContextCompat.getColor(this, R.color.white))

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(
                ContextCompat.getDrawable(
                    this@DetailActivity,
                    R.drawable.arrow_back_24dp
                )
            )
            title = getString(R.string.detail_story)
        }

        val storyId = intent.getStringExtra(EXTRA_STORY_ID)
        if (storyId != null) {
            observeStoryDetails(storyId)
        }
    }

    private fun observeStoryDetails(eventId: String) {
        detailViewModel.getDetailStory(eventId).observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val event = result.data
                    populateEventDetails(event)
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun populateEventDetails(story: Story) {
        binding.tvDetailName.text = HtmlCompat.fromHtml(story.name, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.tvDetailDescription.text = HtmlCompat.fromHtml(story.description, HtmlCompat.FROM_HTML_MODE_LEGACY)

        Glide.with(this)
            .load(story.photoUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .fitCenter()
            )
            .into(binding.ivDetailPhoto)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_STORY_ID = "extra_story_id"
    }
}
