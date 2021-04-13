package com.easyretro.di.modules

import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.common.DefaultCoroutineDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    fun provideDispatcherProvider(): CoroutineDispatcherProvider = DefaultCoroutineDispatcher()
}