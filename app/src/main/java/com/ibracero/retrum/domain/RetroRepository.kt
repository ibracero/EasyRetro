package com.ibracero.retrum.domain

import androidx.lifecycle.LiveData
import arrow.core.Either
import com.ibracero.retrum.data.local.Retro

interface RetroRepository {

    suspend fun createRetro(title: String): Either<Failure, Retro>

    fun getRetro(retroUuid: String): LiveData<Retro>

    suspend fun getRetros(): Either<Failure, List<Retro>>

    fun startObservingUserRetros()

    fun stopObservingUserRetros()

    fun dispose()
}