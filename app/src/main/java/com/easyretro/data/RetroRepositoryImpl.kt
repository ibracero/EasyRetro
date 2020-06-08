package com.easyretro.data

import arrow.core.Either
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.common.UuidProvider
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.mapper.RetroDbToDomainMapper
import com.easyretro.data.remote.AuthDataStore
import com.easyretro.data.remote.DeepLinkDataStore
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.data.remote.mapper.RetroRemoteToDbMapper
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.Retro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

@Suppress("EXPERIMENTAL_API_USAGE")
class RetroRepositoryImpl(
    private val localDataStore: LocalDataStore,
    private val remoteDataStore: RemoteDataStore,
    private val authDataStore: AuthDataStore,
    private val deepLinkDataStore: DeepLinkDataStore,
    private val uuidProvider: UuidProvider,
    private val retroRemoteToDbMapper: RetroRemoteToDbMapper,
    private val retroDbToDomainMapper: RetroDbToDomainMapper,
    private val dispatchers: CoroutineDispatcherProvider
) : RetroRepository {

    companion object {
        private const val DEEPLINK_FORMAT = "https://easyretro.page.link/join/"
    }

    private val userEmail: String
        get() = authDataStore.getCurrentUserEmail()

    override suspend fun createRetro(title: String): Either<Failure, Retro> =
        withContext(dispatchers.io()) {
            val retroUuid = uuidProvider.generateUuid()
            when (val deepLinkEither =
                deepLinkDataStore.generateDeepLink(DEEPLINK_FORMAT + retroUuid)) {
                is Either.Left -> deepLinkEither
                is Either.Right -> {
                    remoteDataStore.createRetro(
                        retroUuid = retroUuid,
                        userEmail = userEmail,
                        retroTitle = title,
                        retroDeepLink = deepLinkEither.b
                    )
                        .map(retroRemoteToDbMapper::map)
                        .map { dbRetro ->
                            localDataStore.saveRetros(listOf(dbRetro))
                            dbRetro
                        }
                        .map { retroDbToDomainMapper.map(it, userEmail) }
                }
            }
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
        localDataStore.observeRetro(retroUuid)
            .map { localRetro ->
                if (localRetro != null) Either.right(
                    retroDbToDomainMapper.map(
                        localRetro,
                        userEmail
                    )
                )
                else Either.left(Failure.RetroNotFoundError)
            }
            .flowOn(dispatchers.io())

    override suspend fun protectRetro(retroUuid: String): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            remoteDataStore.updateRetroProtection(retroUuid = retroUuid, protected = true)
        }

    override suspend fun unprotectRetro(retroUuid: String): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            remoteDataStore.updateRetroProtection(retroUuid = retroUuid, protected = false)
        }

    override suspend fun startObservingRetroDetails(retroUuid: String): Flow<Either<Failure, Unit>> {
        Timber.d("Start observing remote retro $retroUuid")
        return remoteDataStore.observeRetro(retroUuid)
            .map {
                it.map { remoteRetro ->
                    val dbRetro = retroRemoteToDbMapper.map(remoteRetro)
                    localDataStore.updateRetro(dbRetro)
                }
            }.flowOn(dispatchers.io())
    }
}