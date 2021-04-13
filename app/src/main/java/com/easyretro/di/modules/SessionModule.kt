package com.easyretro.di.modules

import android.content.Context
import com.easyretro.data.local.SessionManager
import com.easyretro.data.local.SessionSharedPrefsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides
    fun provideSessionManager(@ApplicationContext applicationContext: Context): SessionManager =
        SessionSharedPrefsManager(applicationContext)
}