package com.easyretro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
@Database(entities = [User::class, Retro::class, Statement::class], version = 1)
@TypeConverters(RoomTypeConverters::class)
abstract class EasyRetroDatabase : RoomDatabase() {

    abstract fun retroDao(): RetroDao

    companion object {

        @Volatile
        private var INSTANCE: EasyRetroDatabase? = null

        fun getDatabase(context: Context): EasyRetroDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EasyRetroDatabase::class.java,
                    "easyretro_database"
                ).build()

                INSTANCE = instance

                return instance
            }
        }
    }
}