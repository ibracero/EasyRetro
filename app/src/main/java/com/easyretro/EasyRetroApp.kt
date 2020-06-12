package com.easyretro

import android.app.Application
import com.easyretro.analytics.AnalyticsManager
import com.easyretro.di.accountModule
import com.easyretro.di.appModule
import com.easyretro.di.mapperModule
import com.easyretro.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class EasyRetroApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@EasyRetroApp)
            modules(listOf(appModule, viewModelModule, mapperModule, accountModule))
        }

        Timber.plant(Timber.DebugTree())
        AnalyticsManager.init(this, BuildConfig.ENABLE_ANALYTICS)
    }
}