package com.ibracero.retrum.data.mapper

import com.ibracero.retrum.common.Mapper
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.remote.cloudstore.RetroResponse

class RetroMapper() : Mapper<RetroResponse, Retro> {

    override fun map(from: RetroResponse): Retro =
        Retro(
            uuid = from.uuid,
            title = from.title,
            positivePoints = from.positivePoints.map { it.uuid },
            negativePoints = from.negativePoints.map { it.uuid },
            actionPoints = from.actionPoints.map { it.uuid }
        )
}
