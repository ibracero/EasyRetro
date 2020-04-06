package com.easyretro.data

import androidx.lifecycle.LiveData
import arrow.core.Either
import com.google.firebase.auth.FirebaseAuth
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.Retro
import com.easyretro.data.mapper.RetroRemoteToDomainMapper
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.domain.Failure
import com.easyretro.domain.RetroRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RetroRepositoryImpl(
    private val localDataStore: LocalDataStore,
    private val remoteDataStore: RemoteDataStore,
    private val retroRemoteToDomainMapper: RetroRemoteToDomainMapper,
    private val dispatchers: CoroutineDispatcherProvider
) : RetroRepository {

    private val userEmail: String
        get() = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    override suspend fun createRetro(title: String): Either<Failure, Retro> =
        withContext(dispatchers.io) {
            remoteDataStore.createRetro(userEmail = userEmail, retroTitle = title)
                .map { retroRemoteToDomainMapper.map(it) }
        }

    override fun getRetro(retroUuid: String): LiveData<Retro> = localDataStore.getRetroInfo(retroUuid)

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
        }.flowOn(dispatchers.io)
    }

    override fun dispose() {
        scope.cancel()
        remoteDataStore.stopObservingAll()
    }
}