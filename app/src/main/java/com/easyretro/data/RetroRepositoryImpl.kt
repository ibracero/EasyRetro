package com.easyretro.data

import arrow.core.Either
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.mapper.RetroDbToDomainMapper
import com.easyretro.data.remote.AuthDataStore
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.data.remote.mapper.RetroRemoteToDbMapper
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.Retro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class RetroRepositoryImpl(
    private val localDataStore: LocalDataStore,
    private val remoteDataStore: RemoteDataStore,
    private val authDataStore: AuthDataStore,
    private val retroRemoteToDbMapper: RetroRemoteToDbMapper,
    private val retroDbToDomainMapper: RetroDbToDomainMapper,
    private val dispatchers: CoroutineDispatcherProvider
) : RetroRepository {

    private val userEmail: String
        get() = authDataStore.getCurrentUserEmail()

    override suspend fun createRetro(title: String): Either<Failure, Retro> =
        withContext(dispatchers.io()) {
            remoteDataStore.createRetro(userEmail = userEmail, retroTitle = title)
                .map(retroRemoteToDbMapper::map)
                .map(retroDbToDomainMapper::map)
        }

    override suspend fun joinRetro(uuid: String): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            remoteDataStore.joinRetro(userEmail = userEmail, retroUuid = uuid)
        }

    override suspend fun getRetro(retroUuid: String): Either<Failure, Retro> =
        withContext(dispatchers.io()) {
            val retro = localDataStore.getRetro(retroUuid)
            if (retro == null) Either.left(Failure.RetroNotFoundError)
            else Either.right(retroDbToDomainMapper.map(retro))
        }

    override suspend fun getRetros(): Flow<Either<Failure, List<Retro>>> {
        return flow {
            val localRetros = localDataStore.getRetros().map(retroDbToDomainMapper::map)
            if (localRetros.isNotEmpty()) emit(Either.right(localRetros))

            val remoteEither = remoteDataStore.getUserRetros(userEmail)
                .map {
                    val retros = it.map(retroRemoteToDbMapper::map)
                    localDataStore.saveRetros(retros)
                    retros.map(retroDbToDomainMapper::map)
                }
            emit(remoteEither)
        }.flowOn(dispatchers.io())
    }
}