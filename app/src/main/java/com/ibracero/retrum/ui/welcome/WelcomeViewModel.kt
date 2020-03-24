package com.ibracero.retrum.ui.welcome

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.Failure
import com.ibracero.retrum.domain.UserStatus
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val repository: AccountRepository
) : ViewModel() {

    init {
        checkUserSession()
    }

    val googleSignInLiveData = MutableLiveData<Either<Failure, Unit>>()
    val userSessionLiveData = MutableLiveData<Either<Failure, UserStatus>>()

    fun handleSignInResult(account: GoogleSignInAccount?) {
        if (account != null) firebaseAuthWithGoogle(account)
        else googleSignInLiveData.postValue(Either.left(Failure.UnknownError))
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            userSessionLiveData.postValue(repository.getUserStatus())
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            val result = repository.signWithGoogleAccount(account)
            googleSignInLiveData.postValue(result)
        }
    }
}