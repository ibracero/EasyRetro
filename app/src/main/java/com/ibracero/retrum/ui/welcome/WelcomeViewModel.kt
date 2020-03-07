package com.ibracero.retrum.ui.welcome

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.domain.Failure
import com.ibracero.retrum.domain.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class WelcomeViewModel(
    private val repository: AccountRepository,
    private val dispatchers: CoroutineDispatcherProvider
) : ViewModel() {

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    val googleSignInLiveData = MutableLiveData<Either<Failure, Unit>>()

    fun isSessionOpen(): Boolean = repository.isSessionOpen()

    fun handleSignInResult(task: Task<GoogleSignInAccount>?) {
        try {
            val account = task?.getResult(ApiException::class.java)
            if (account != null) firebaseAuthWithGoogle(account)
            else googleSignInLiveData.postValue(Either.left(Failure.UnknownError))
        } catch (e: ApiException) {
            Timber.e(e)
            googleSignInLiveData.postValue(Either.left(Failure.UnknownError))
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        scope.launch {
            val result = repository.firebaseAuthWithGoogle(account)
            withContext(dispatchers.main) {
                googleSignInLiveData.postValue(result)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}