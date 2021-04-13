package com.easyretro

import android.app.Application
import com.easyretro.analytics.AnalyticsManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class EasyRetroApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        AnalyticsManager.init(this, BuildConfig.ENABLE_ANALYTICS)
    }
}