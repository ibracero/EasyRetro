package com.easyretro.data.remote.mapper

import com.easyretro.common.Mapper
import com.easyretro.data.local.StatementDb
import com.easyretro.data.remote.firestore.StatementRemote
import com.easyretro.domain.model.StatementType
import java.util.*
import javax.inject.Inject

class StatementRemoteToDbMapper @Inject constructor() : Mapper<StatementRemote, StatementDb> {

    override fun map(from: StatementRemote): StatementDb =
        StatementDb(
            uuid = from.uuid.orEmpty(),
            retroUuid = from.retroUuid.orEmpty(),
            userEmail = from.userEmail,
            description = from.description,
            type = StatementType.valueOf(from.statementType.toUpperCase(Locale.getDefault())),
            timestamp = from.timestamp ?: 0,
            removable = from.isRemovable ?: false
        )
}