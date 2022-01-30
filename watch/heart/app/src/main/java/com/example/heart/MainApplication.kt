package com.example.heart

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class, needed to enable dependency injection with Hilt.
 */
@HiltAndroidApp
class MainApplication : Application()

const val TAG = "Measuring Data Sample"
