package com.easyretro.di.modules

import android.content.Context
import com.easyretro.data.local.SessionManager
import com.easyretro.data.local.SessionSharedPrefsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ApplicationComponent::class)
object SessionModule {

    @Provides
    fun provideSessionManager(@ApplicationContext applicationContext: Context): SessionManager =
        SessionSharedPrefsManager(applicationContext)
}