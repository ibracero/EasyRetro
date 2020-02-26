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

class ResetPasswordViewModel(
    private val accountRepository: AccountRepository,
    private val dispatchers: CoroutineDispatcherProvider
) : ViewModel() {

    val resetPasswordLiveData = MutableLiveData<Either<ServerError, Unit>>()

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    fun resetPassword(email: String) {
        scope.launch {
            val result = accountRepository.resetPassword(email)
            withContext(dispatchers.main) {
                resetPasswordLiveData.postValue(result)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}