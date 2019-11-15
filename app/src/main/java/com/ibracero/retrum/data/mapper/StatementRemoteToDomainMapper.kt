package com.ibracero.retrum.data.mapper

import com.ibracero.retrum.common.Mapper
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.data.remote.firestore.StatementRemote
import com.ibracero.retrum.domain.StatementType
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