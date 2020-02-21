package com.ibracero.retrum.ui.account

import androidx.lifecycle.ViewModel
import com.ibracero.retrum.domain.AccountRepository

class ResetPasswordViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {

    fun resetPassword(email: String) {
        accountRepository.resetPassword(email)
    }
}