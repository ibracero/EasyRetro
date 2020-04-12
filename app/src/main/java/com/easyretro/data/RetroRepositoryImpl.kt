package com.easyretro.data

import arrow.core.Either
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.Retro
import com.easyretro.data.mapper.RetroRemoteToDomainMapper
import com.easyretro.data.remote.AuthDataStore
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.domain.Failure
import com.easyretro.domain.RetroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class RetroRepositoryImpl(
    private val localDataStore: LocalDataStore,
    private val remoteDataStore: RemoteDataStore,
    private val authDataStore: AuthDataStore,
    private val retroRemoteToDomainMapper: RetroRemoteToDomainMapper,
    private val dispatchers: CoroutineDispatcherProvider
) : RetroRepository {

    private val userEmail: String
        get() = authDataStore.getCurrentUserEmail()

    override suspend fun createRetro(title: String): Either<Failure, Retro> =
        withContext(dispatchers.io()) {
            remoteDataStore.createRetro(userEmail = userEmail, retroTitle = title)
                .map { retroRemoteToDomainMapper.map(it) }
        }

    override suspend fun joinRetro(uuid: String): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            remoteDataStore.joinRetro(userEmail = userEmail, retroUuid = uuid)
        }

    override suspend fun getRetro(retroUuid: String): Either<Failure, Retro> =
        withContext(dispatchers.io()) {
            val retro = localDataStore.getRetro(retroUuid)
            if (retro == null) Either.left(Failure.RetroNotFoundError)
            else Either.right(retro)
        }

    override suspend fun getRetros(): Flow<Either<Failure, List<Retro>>> {
        return flow {
            val localRetros = localDataStore.getRetros()
            if (localRetros.isNotEmpty()) emit(Either.right(localRetros))

            val remoteEither = remoteDataStore.getUserRetros(userEmail)
                .map {
                    val retros = it.map(retroRemoteToDomainMapper::map)
                    localDataStore.saveRetros(retros)
                    retros
                }
            emit(remoteEither)
        }.flowOn(dispatchers.io())
    }
}