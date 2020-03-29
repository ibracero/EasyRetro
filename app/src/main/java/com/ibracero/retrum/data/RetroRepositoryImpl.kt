package com.ibracero.retrum.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import arrow.core.Either
import com.google.firebase.auth.FirebaseAuth
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.data.local.LocalDataStore
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.mapper.RetroRemoteToDomainMapper
import com.ibracero.retrum.data.remote.RemoteDataStore
import com.ibracero.retrum.domain.Failure
import com.ibracero.retrum.domain.RetroRepository
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