package com.ibracero.retrum.data.mapper

import com.ibracero.retrum.common.Mapper
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.remote.firestore.RetroRemote

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