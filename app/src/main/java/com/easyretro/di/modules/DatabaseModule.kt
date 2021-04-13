package com.easyretro.di.modules

import android.content.Context
import androidx.room.Room
import com.easyretro.data.local.EasyRetroDatabase
import com.easyretro.data.local.RetroDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object DatabaseModule {

    private const val DB_NAME = "easyretro_database"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext applicationContext: Context): EasyRetroDatabase =
        Room.databaseBuilder(applicationContext, EasyRetroDatabase::class.java, DB_NAME).build()

    @Provides
    fun provideRetroDao(retroDatabase: EasyRetroDatabase): RetroDao =
        retroDatabase.retroDao()
}