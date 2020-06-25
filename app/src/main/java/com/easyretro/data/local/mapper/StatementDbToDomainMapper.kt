package com.easyretro.data.local.mapper

import com.easyretro.common.Mapper
import com.easyretro.data.local.StatementDb
import com.easyretro.domain.model.Statement
import javax.inject.Inject

class StatementDbToDomainMapper @Inject constructor() : Mapper<StatementDb, Statement> {

    override fun map(from: StatementDb): Statement =
        Statement(
            uuid = from.uuid,
            retroUuid = from.retroUuid,
            userEmail = from.userEmail,
            description = from.description,
            type = from.type,
            timestamp = from.timestamp,
            removable = from.removable
        )
}