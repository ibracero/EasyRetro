package com.easyretro.domain

import arrow.core.Either
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.Retro
import kotlinx.coroutines.flow.Flow

interface RetroRepository {

    suspend fun createRetro(title: String): Either<Failure, Retro>

    suspend fun joinRetro(uuid: String): Either<Failure, Unit>

    suspend fun observeRetro(retroUuid: String): Flow<Either<Failure, Retro>>

    suspend fun getRetros(): Flow<Either<Failure, List<Retro>>>

    suspend fun protectRetro(retroUuid: String): Either<Failure, Unit>

    suspend fun unprotectRetro(retroUuid: String): Either<Failure, Unit>

    suspend fun startObservingRetroDetails(retroUuid: String): Flow<Either<Failure, Unit>>
}