package com.ibracero.retrum.data.mapper

import com.ibracero.retrum.common.Mapper
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.data.remote.cloudstore.StatementResponse

class StatementMapper : Mapper<StatementResponse, Statement> {

    override fun map(from: StatementResponse): Statement =
        Statement(
            uuid = from.uuid,
            userEmail = from.userEmail,
            description = from.description
        )
}