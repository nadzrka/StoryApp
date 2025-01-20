package com.nadzira.storyapp.ui.maps

import android.content.ContentValues.TAG
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.nadzira.storyapp.R
import com.nadzira.storyapp.databinding.ActivityMapsBinding
import com.nadzira.storyapp.di.Injection
import com.nadzira.storyapp.remote.response.ListStoryItem
import com.nadzira.storyapp.ui.Result
import com.nadzira.storyapp.ui.UserPreference
import com.nadzira.storyapp.ui.ViewModelFactory
import kotlinx.coroutines.launch
import kotlin.getValue

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val mapsViewModel by viewModels<MapsViewModel> {
        ViewModelFactory(Injection.provideRepository(this))
    }
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreference = UserPreference(this)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeUserSession()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.style_json
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
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
    private fun observeStories(token: String) {
        if (token.isNotEmpty()) {
            mapsViewModel.getStoriesWithLoc().observe(this) { result ->
                Log.d("StoryActivity", "API result: $result")
                handleResult(result)
            }
        } else {
            showError(getString(R.string.user_is_not_logged_in))
        }
    }

    private fun handleResult(result: Result<List<ListStoryItem>>) {
        when (result) {
            is Result.Loading -> showLoading(true)
            is Result.Success -> {
                result.data.forEach { data ->
                    val latLng = LatLng(data.lat, data.lon)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(data.name)
                            .snippet(data.description)
                    )
                    Log.d("MapUpdate", "Adding story: ${data.name} at $latLng")
                }
                showLoading(false)
            }
            is Result.Error -> {
                showLoading(false)
                Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}