package com.easyretro.data.local

import androidx.room.TypeConverter
import com.easyretro.domain.model.StatementType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class RoomTypeConverters {

    private val gson = Gson()

    @TypeConverter
    fun stringListToString(stringList: List<String>): String = stringList.joinToString(separator = ",")

    @TypeConverter
    fun stringListFromString(string: String): List<String> = string.split(",")

    @TypeConverter
    fun statementTypeToString(statementType: StatementType): String = statementType.toString()

    @TypeConverter
    fun stringToStatementType(statementTypeString: String): StatementType = StatementType.valueOf(statementTypeString)

    @TypeConverter
    fun userListToString(userList: List<UserDb>): String = gson.toJson(userList)

    @TypeConverter
    fun stringToUserList(userListString: String): List<UserDb> {
        return if (userListString.isEmpty()) emptyList()
        else {
            val listType: Type = object : TypeToken<List<UserDb?>?>() {}.type
            gson.fromJson(userListString, listType)
        }
    }
}