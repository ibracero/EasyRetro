package com.ibracero.retrum.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import arrow.core.Either
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.data.local.LocalDataStore
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.mapper.RetroRemoteToDomainMapper
import com.ibracero.retrum.data.remote.RemoteDataStore
import com.ibracero.retrum.data.remote.ServerError
import com.ibracero.retrum.domain.RetroRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class RetroRepositoryImpl(
    private val localDataStore: LocalDataStore,
    private val remoteDataStore: RemoteDataStore,
    private val retroRemoteToDomainMapper: RetroRemoteToDomainMapper,
    dispatchers: CoroutineDispatcherProvider
) : RetroRepository {

    private val userEmail: String
        get() = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    override fun createRetro(title: String): LiveData<Either<ServerError, Retro>> {
        val retroLiveData = MutableLiveData<Either<ServerError, Retro>>()
        scope.launch {
            val retroEither = remoteDataStore.createRetro(userEmail = userEmail, retroTitle = title)
                .map { retroRemoteToDomainMapper.map(it) }
            retroLiveData.postValue(retroEither)
        }
        return retroLiveData
    }

    override fun getRetros(): LiveData<List<Retro>> = localDataStore.getRetros()

    override fun startObservingUserRetros() {
        scope.launch {
            remoteDataStore.observeUserRetros(userEmail)
                .map { localDataStore.saveRetros(it.map(retroRemoteToDomainMapper::map)) }
        }
    }

    override fun stopObservingUserRetros() {
        remoteDataStore.stopObservingRetros()
    }

    override fun dispose() {
        scope.cancel()
    }
}