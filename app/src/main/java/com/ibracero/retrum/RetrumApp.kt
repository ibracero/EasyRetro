package com.ibracero.retrum

import android.app.Application
import com.ibracero.retrum.di.appModule
import com.ibracero.retrum.di.mapperModule
import com.ibracero.retrum.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class RetrumApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@RetrumApp)
            modules(listOf(appModule, viewModelModule, mapperModule))

            Timber.plant(Timber.DebugTree())
        }
    }
}