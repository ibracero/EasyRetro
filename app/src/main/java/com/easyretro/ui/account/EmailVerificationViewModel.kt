package com.easyretro.ui.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.Failure
import com.easyretro.domain.UserStatus
import kotlinx.coroutines.launch

class EmailVerificationViewModel(
    private val repository: AccountRepository
) : ViewModel() {

    val sendVerificationLiveData = MutableLiveData<Either<Failure, Unit>>()
    val userStatusLiveData = MutableLiveData<Either<Failure, UserStatus>>()

    fun resendVerificationEmail() {
        viewModelScope.launch {
            val result = repository.resendVerificationEmail()
            sendVerificationLiveData.postValue(result)
        }
    }

    fun refreshUserStatus() {
        viewModelScope.launch {
            val userStatus = repository.getUserStatus()
            userStatusLiveData.postValue(userStatus)
        }
    }
}