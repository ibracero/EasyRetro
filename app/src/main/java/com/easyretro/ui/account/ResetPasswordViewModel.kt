package com.easyretro.ui.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.Failure
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {

    val resetPasswordLiveData = MutableLiveData<Either<Failure, Unit>>()

    fun resetPassword(email: String) {
        viewModelScope.launch {
            val result = accountRepository.resetPassword(email)
            resetPasswordLiveData.postValue(result)
        }
    }
}