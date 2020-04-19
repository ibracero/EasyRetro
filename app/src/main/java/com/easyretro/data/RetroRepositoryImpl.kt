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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber

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

    override suspend fun observeRetro(retroUuid: String): Flow<Either<Failure, Retro>> =
        observeLocalRetro(retroUuid)
            .onCompletion {
                emitAll(observeRemoteRetro(retroUuid))
            }
            .flowOn(dispatchers.io())

    override suspend fun lockRetro(retroUuid: String): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            remoteDataStore.updateRetroLock(retroUuid = retroUuid, locked = true)
        }

    override suspend fun unlockRetro(retroUuid: String): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            remoteDataStore.updateRetroLock(retroUuid = retroUuid, locked = false)
        }

    private suspend fun observeLocalRetro(retroUuid: String): Flow<Either<Failure, Retro>> {
        return flow {
            Timber.d("Observing local retro $retroUuid")
            val localRetro = localDataStore.getRetro(retroUuid)
            if (localRetro != null) emit(Either.right(retroDbToDomainMapper.map(localRetro)))
            else Either.left(Failure.RetroNotFoundError)
        }
    }

    private suspend fun observeRemoteRetro(retroUuid: String): Flow<Either<Failure, Retro>> {
        Timber.d("Observing remote retro $retroUuid")
        return remoteDataStore.observeRetro(retroUuid)
            .map {
                it.map { remoteRetro ->
                    val dbRetro = retroRemoteToDbMapper.map(remoteRetro)
                    localDataStore.updateRetro(dbRetro)
                    retroDbToDomainMapper.map(dbRetro, userEmail)
                }
            }
    }
}