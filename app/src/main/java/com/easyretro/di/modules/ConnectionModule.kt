package com.easyretro.di.modules

import android.content.Context
import android.net.ConnectivityManager
import com.easyretro.common.ConnectionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ConnectionModule {

    @Provides
    fun provideConnectionManager(@ApplicationContext applicationContext: Context): ConnectionManager =
        ConnectionManager(applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
}