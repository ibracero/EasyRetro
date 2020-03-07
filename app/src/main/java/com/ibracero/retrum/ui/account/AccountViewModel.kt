package com.ibracero.retrum.ui.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.domain.Failure
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.UserStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountViewModel(
    private val repository: AccountRepository,
    private val dispatchers: CoroutineDispatcherProvider
) : ViewModel() {

    var signInLiveData: MutableLiveData<Either<Failure, UserStatus>>? = null
        private set
    var signUpLiveData: MutableLiveData<Either<Failure, Unit>>? = null
        private set

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    fun onStart() {
        signInLiveData = MutableLiveData<Either<Failure, UserStatus>>()
        signUpLiveData = MutableLiveData<Either<Failure, Unit>>()
    }

    fun onStop() {
        signInLiveData = null
        signUpLiveData = null
    }

    fun signIn(email: String, password: String) {
        scope.launch {
            val signInResult = repository.loginUser(email, password)
            withContext(dispatchers.main) {
                signInLiveData?.postValue(signInResult)
            }
        }
    }

    fun signUp(email: String, password: String) {
        scope.launch {
            val signUpResult = repository.createUser(email, password)
            withContext(dispatchers.main) {
                signUpLiveData?.postValue(signUpResult)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}