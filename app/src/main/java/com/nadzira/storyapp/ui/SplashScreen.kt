package com.nadzira.storyapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nadzira.storyapp.R
import com.nadzira.storyapp.ui.story.StoryActivity
import com.nadzira.storyapp.ui.welcome.WelcomeActivity
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class SplashScreen : AppCompatActivity() {
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        userPreference = UserPreference(this)
        observeUserSession()
    }

    private fun observeUserSession() {
        lifecycleScope.launch {
            val session = userPreference.getSession()
            val token = session.token
            Log.d("SplashScreen", "Token: $token")
            Handler(Looper.getMainLooper()).postDelayed({
                if (token.isNullOrEmpty()) {
                    Log.d("SplashScreen", "Navigating to WelcomeActivity")
                    navigateToWelcomeActivity()
                } else {
                    Log.d("SplashScreen", "Navigating to StoryActivity")
                    navigateToStoryActivity()
                }
            }, 2000)
        }
    }


    private fun navigateToStoryActivity() {
        val intent = Intent(this, StoryActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToWelcomeActivity() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
