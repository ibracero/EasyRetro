package com.easyretro.domain

import androidx.lifecycle.LiveData
import arrow.core.Either
import com.easyretro.data.local.Retro
import kotlinx.coroutines.flow.Flow

interface RetroRepository {

    suspend fun createRetro(title: String): Either<Failure, Retro>

    fun getRetro(retroUuid: String): LiveData<Retro>

    suspend fun getRetros(): Flow<Either<Failure, List<Retro>>>

    fun dispose()
}