package com.easyretro.data.mapper

import com.easyretro.common.Mapper
import com.easyretro.data.local.Statement
import com.easyretro.data.remote.firestore.StatementRemote
import com.easyretro.domain.StatementType
import java.util.*

class StatementRemoteToDomainMapper : Mapper<StatementRemote, Statement> {

    override fun map(from: StatementRemote): Statement =
        Statement(
            uuid = from.uuid.orEmpty(),
            retroUuid = from.retroUuid.orEmpty(),
            userEmail = from.userEmail,
            description = from.description,
            type = StatementType.valueOf(from.statementType.toUpperCase(Locale.getDefault())),
            timestamp = from.timestamp ?: 0,
            removable = from.isRemovable ?: false
        )
}