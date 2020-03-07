package com.ibracero.retrum.ui.welcome

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.Failure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WelcomeViewModel(
    private val repository: AccountRepository,
    private val dispatchers: CoroutineDispatcherProvider
) : ViewModel() {

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    val googleSignInLiveData = MutableLiveData<Either<Failure, Unit>>()

    fun isSessionOpen(): Boolean = repository.isSessionOpen()

    fun handleSignInResult(account: GoogleSignInAccount?) {
        if (account != null) firebaseAuthWithGoogle(account)
        else googleSignInLiveData.postValue(Either.left(Failure.UnknownError))
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