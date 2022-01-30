 package com.example.heart

 import android.os.Bundle
 import android.util.Log
 import androidx.activity.result.ActivityResultLauncher
 import androidx.activity.result.contract.ActivityResultContracts
 import androidx.activity.viewModels
 import androidx.appcompat.app.AppCompatActivity
 import androidx.core.view.isVisible
 import androidx.lifecycle.lifecycleScope
 import com.example.measuredata.R
 import com.example.measuredata.databinding.ActivityMainBinding
 import dagger.hilt.android.AndroidEntryPoint
 import kotlinx.coroutines.flow.collect

 /**
  * Activity displaying the app UI. Notably, this binds data from [MainViewModel] to views on screen,
  * and performs the permission check when enabling measure data.
  */
 @AndroidEntryPoint
 class MainActivity : AppCompatActivity() {

     private lateinit var binding: ActivityMainBinding
     private lateinit var permissionLauncher: ActivityResultLauncher<String>

     private val viewModel: MainViewModel by viewModels()

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)

         binding = ActivityMainBinding.inflate(layoutInflater)
         setContentView(binding.root)

         permissionLauncher =
             registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                 when (result) {
                     true -> {
                         Log.i(TAG, "Body sensors permission granted")
                         // Only measure while the activity is at least in STARTED state.
                         // MeasureClient provides frequent updates, which requires increasing the
                         // sampling rate of device sensors, so we must be careful not to remain
                         // registered any longer than necessary.
                         lifecycleScope.launchWhenStarted {
                             viewModel.measureHeartRate()
                         }
                     }
                     false -> Log.i(TAG, "Body sensors permission not granted")
                 }
             }

         // Bind viewmodel state to the UI.
         lifecycleScope.launchWhenStarted {
             viewModel.uiState.collect {
                 updateViewVisiblity(it)
             }
         }
         lifecycleScope.launchWhenStarted {
             viewModel.heartRateBpm.collect {
                 binding.lastMeasuredValue.text = String.format("%.1f", it)
             }
         }
     }

     override fun onStart() {
         super.onStart()
         permissionLauncher.launch(android.Manifest.permission.BODY_SENSORS)
     }

     private fun updateViewVisiblity(uiState: UiState) {
         // These views are visible when heart rate capability is not available.
         (uiState is UiState.HeartRateNotAvailable).let {
             binding.brokenHeart.isVisible = it
             binding.notAvailable.isVisible = it
         }
         // These views are visible when the capability is available.
         (uiState is UiState.HeartRateAvailable).let {

             binding.lastMeasuredLabel.isVisible = it
             binding.lastMeasuredValue.isVisible = it

         }
     }
 }
