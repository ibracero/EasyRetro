package com.ibracero.retrum.ui.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.Failure
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