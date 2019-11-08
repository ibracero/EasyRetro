package com.ibracero.retrum.data.local

import androidx.room.TypeConverter

class RoomTypeConverters {
    @TypeConverter
    fun stringListToString(stringList: List<String>): String = stringList.joinToString(separator = ",")

    @TypeConverter
    fun stringListFromString(string: String): List<String> = string.split(",")
}