package com.easyretro.di.modules

import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.common.DefaultCoroutineDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

@Module
@InstallIn(ApplicationComponent::class)
object DispatcherModule {

    @Provides
    fun provideDispatcherProvider(): CoroutineDispatcherProvider = DefaultCoroutineDispatcher()
}