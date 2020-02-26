package com.ibracero.retrum.ui.retros

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.common.RetrumConnectionManager
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.remote.ServerError
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.RetroRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RetroListViewModel(
    private val retroRepository: RetroRepository,
    private val accountRepository: AccountRepository,
    private val dispatchers: CoroutineDispatcherProvider,
    connectionManager: RetrumConnectionManager
) : ViewModel() {

    val retroLiveData: LiveData<List<Retro>> = retroRepository.getRetros()
    val connectivityLiveData = connectionManager.connectionLiveData
    val logoutLiveData = MutableLiveData<Either<ServerError, Unit>>()

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    fun createRetro(title: String) = retroRepository.createRetro(title)

    fun logout() {
        scope.launch {
            val logoutResult = accountRepository.logOut()
            withContext(dispatchers.main) {
                logoutLiveData.postValue(logoutResult)
            }
        }
    }

    fun startObservingRetros() {
        retroRepository.startObservingUserRetros()
    }

    fun stopObservingRetros() {
        retroRepository.stopObservingUserRetros()
    }

    override fun onCleared() {
        super.onCleared()
        retroRepository.dispose()
        job.cancel()
    }
}
