package com.easyretro.data.local.mapper

import com.easyretro.common.Mapper
import com.easyretro.data.local.RetroDb
import com.easyretro.domain.model.Retro

class RetroDbToDomainMapper(
    private val userDbToDomainMapper: UserDbToDomainMapper
) : Mapper<RetroDb, Retro> {

    override fun map(from: RetroDb): Retro =
        Retro(
            uuid = from.uuid,
            title = from.title,
            timestamp = from.timestamp,
            ownerEmail = from.ownerEmail,
            locked = from.locked,
            users = from.users.map(userDbToDomainMapper::map)
        )
}