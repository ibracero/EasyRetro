package com.easyretro.data.local.mapper

import com.easyretro.common.Mapper
import com.easyretro.data.local.RetroDb
import com.easyretro.domain.model.Retro
import javax.inject.Inject

class RetroDbToDomainMapper @Inject constructor(
    private val userDbToDomainMapper: UserDbToDomainMapper
) : Mapper<RetroDb, Retro> {

    override fun map(from: RetroDb): Retro =
        Retro(
            uuid = from.uuid,
            title = from.title,
            timestamp = from.timestamp,
            deepLink = from.deepLink,
            protected = from.isProtected,
            users = from.users.map(userDbToDomainMapper::map),
            lockingAllowed = false
        )

    fun map(from: RetroDb, currentUserEmail: String) =
        map(from).copy(lockingAllowed = from.ownerEmail == currentUserEmail)
}