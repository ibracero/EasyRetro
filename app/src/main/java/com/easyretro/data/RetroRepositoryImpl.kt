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

    override suspend fun getRetros(): Either<Failure, List<Retro>> =
        withContext(dispatchers.io) {
            remoteDataStore.getUserRetros(userEmail)
                .map { localDataStore.saveRetros(it.map(retroRemoteToDomainMapper::map)) }
            Either.right(localDataStore.getRetros())
        }

    override fun startObservingUserRetros() {
        remoteDataStore.observeUserRetros(userEmail) {
            scope.launch {
                it.map { retros -> localDataStore.saveRetros(retros.map(retroRemoteToDomainMapper::map)) }
            }
        }
    }

    override fun stopObservingUserRetros() {
        remoteDataStore.stopObservingUserRetros()
    }

    override fun dispose() {
        scope.cancel()
        remoteDataStore.stopObservingAll()
    }
}