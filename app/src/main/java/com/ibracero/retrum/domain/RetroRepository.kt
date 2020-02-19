package com.ibracero.retrum.domain

import androidx.lifecycle.LiveData
import arrow.core.Either
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.remote.ServerError

interface RetroRepository {

    fun createRetro(title: String): LiveData<Either<ServerError, Retro>>

    fun getRetro(retroUuid: String): LiveData<Retro>

    fun getRetros(): LiveData<List<Retro>>

    fun startObservingUserRetros()

    fun stopObservingUserRetros()

    fun dispose()
}