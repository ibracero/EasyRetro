package com.ibracero.retrum.data.local

import androidx.room.TypeConverter
import com.ibracero.retrum.domain.StatementType

class RoomTypeConverters {
    @TypeConverter
    fun stringListToString(stringList: List<String>): String = stringList.joinToString(separator = ",")

    @TypeConverter
    fun stringListFromString(string: String): List<String> = string.split(",")

    @TypeConverter
    fun statementTypeToString(statementType: StatementType): String = statementType.toString()

    @TypeConverter
    fun stringToStatementType(statementTypeString: String): StatementType = StatementType.valueOf(statementTypeString)
}