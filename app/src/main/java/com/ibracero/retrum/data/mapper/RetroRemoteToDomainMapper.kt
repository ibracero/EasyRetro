package com.ibracero.retrum.data.mapper

import com.ibracero.retrum.common.Mapper
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.remote.cloudstore.RetroRemote

class RetroRemoteToDomainMapper : Mapper<RetroRemote, Retro> {

    override fun map(from: RetroRemote): Retro =
        Retro(
            uuid = from.uuid,
            title = from.title
        )
}