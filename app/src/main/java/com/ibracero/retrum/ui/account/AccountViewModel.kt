package com.ibracero.retrum.ui.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.data.remote.ServerError
import com.ibracero.retrum.domain.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountViewModel(
    private val repository: AccountRepository,
    private val dispatchers: CoroutineDispatcherProvider
) : ViewModel() {

    val signInLiveData = MutableLiveData<Either<ServerError, Unit>>()
    val signUpLiveData = MutableLiveData<Either<ServerError, Unit>>()

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    fun signIn(email: String, password: String) {
        scope.launch {
            val signInResult = repository.loginUser(email, password)
            withContext(dispatchers.main) {
                signInLiveData.postValue(signInResult)
            }
        }
    }

    fun signUp(email: String, password: String) {
        scope.launch {
            val signUpResult = repository.createUser(email, password)
            withContext(dispatchers.main) {
                signUpLiveData.postValue(signUpResult)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}