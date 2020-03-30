package com.easyretro.data.mapper

import com.easyretro.common.Mapper
import com.easyretro.data.local.Retro
import com.easyretro.data.remote.firestore.RetroRemote

class RetroRemoteToDomainMapper(
    private val userRemoteToDomainMapper: UserRemoteToDomainMapper
) : Mapper<RetroRemote, Retro> {

    override fun map(from: RetroRemote): Retro =
        Retro(
            uuid = from.uuid,
            title = from.title,
            timestamp = from.timestamp ?: 0,
            users = from.users?.map(userRemoteToDomainMapper::map) ?: emptyList()
        )
}