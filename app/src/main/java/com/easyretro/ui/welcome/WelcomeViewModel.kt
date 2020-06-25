package com.easyretro.ui.welcome

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.User
import com.easyretro.domain.model.UserStatus
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.launch

class WelcomeViewModel @ViewModelInject constructor(
    private val repository: AccountRepository
) : ViewModel() {

    val googleSignInLiveData = MutableLiveData<Either<Failure, Unit>>()
    val userSessionLiveData = MutableLiveData<Either<Failure, UserStatus>>()

    init {
        checkUserSession()
    }

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
        val safeTokenId = account.idToken ?: return

        viewModelScope.launch {
            val result = repository.signWithGoogleAccount(
                idToken = safeTokenId,
                user = User(
                    email = account.email.orEmpty(),
                    firstName = account.givenName.orEmpty(),
                    lastName = account.familyName.orEmpty(),
                    photoUrl = account.photoUrl?.toString().orEmpty()
                )
            )
            googleSignInLiveData.postValue(result)
        }
    }
}