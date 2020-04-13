package com.easyretro.ui.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.UserStatus
import kotlinx.coroutines.launch

class AccountViewModel(
    private val repository: AccountRepository
) : ViewModel() {

    var signInLiveData: MutableLiveData<Either<Failure, UserStatus>>? = null
        private set
    var signUpLiveData: MutableLiveData<Either<Failure, Unit>>? = null
        private set

    fun onStart() {
        signInLiveData = MutableLiveData<Either<Failure, UserStatus>>()
        signUpLiveData = MutableLiveData<Either<Failure, Unit>>()
    }

    fun onStop() {
        signInLiveData = null
        signUpLiveData = null
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            val signInResult = repository.signWithEmail(email, password)
            signInLiveData?.postValue(signInResult)
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            val signUpResult = repository.signUpWithEmail(email, password)
            signUpLiveData?.postValue(signUpResult)
        }
    }
}