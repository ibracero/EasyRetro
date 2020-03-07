package com.ibracero.retrum.ui.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.Failure
import com.ibracero.retrum.domain.UserStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class EmailVerificationViewModel(
    private val repository: AccountRepository,
    private val dispatchers: CoroutineDispatcherProvider
) : ViewModel() {

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    val sendVerificationLiveData = MutableLiveData<Either<Failure, Unit>>()
    val userStatusLiveData = MutableLiveData<Either<Failure, UserStatus>>()

    fun resendVerificationEmail() {
        scope.launch {
            val result = repository.resendVerificationEmail()
            withContext(dispatchers.main) {
                sendVerificationLiveData.postValue(result)
            }
        }
    }

    fun refreshUserStatus() {
        scope.launch {
            val userStatus = repository.getUserStatus()
            withContext(dispatchers.main) {
                userStatusLiveData.postValue(userStatus)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

}