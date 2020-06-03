package com.easyretro.ui.account

import androidx.lifecycle.viewModelScope
import com.easyretro.common.BaseViewModel
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.UserStatus
import com.easyretro.ui.FailureMessage
import kotlinx.coroutines.launch

class AccountViewModel(
    private val repository: AccountRepository
) : BaseViewModel<AccountViewState, AccountViewEffect, AccountViewEvent>() {

    override fun process(viewEvent: AccountViewEvent) {
        super.process(viewEvent)
        when (viewEvent) {
            AccountViewEvent.ResetPassword -> viewEffect = AccountViewEffect.OpenResetPassword
            is AccountViewEvent.SignIn -> signIn(email = viewEvent.email, password = viewEvent.password)
            is AccountViewEvent.SignUp -> signUp(email = viewEvent.email, password = viewEvent.password)
        }
    }

    private fun signIn(email: String, password: String) {
        viewState = AccountViewState(signingState = SigningState.Loading)
        viewModelScope.launch {
            val signInResult = repository.signInWithEmail(email, password)
            viewState = AccountViewState(signingState = SigningState.RequestDone)
            signInResult.fold(
                { error ->
                    viewEffect = if (error is Failure.InvalidUserFailure) {
                        AccountViewEffect.ShowUnknownUserSnackBar(FailureMessage.parse(error))
                    } else AccountViewEffect.ShowGenericSnackBar(FailureMessage.parse(error))
                }, { userStatus ->
                    viewEffect = when (userStatus) {
                        UserStatus.VERIFIED -> AccountViewEffect.OpenRetroList
                        UserStatus.NON_VERIFIED -> AccountViewEffect.OpenEmailVerification
                    }
                })
        }
    }

    private fun signUp(email: String, password: String) {
        viewState = AccountViewState(signingState = SigningState.Loading)
        viewModelScope.launch {
            val signInResult = repository.signUpWithEmail(email, password)
            viewState = AccountViewState(signingState = SigningState.RequestDone)
            signInResult.fold(
                { error ->
                    viewEffect = if (error is Failure.UserCollisionFailure) {
                        AccountViewEffect.ShowExistingUserSnackBar(FailureMessage.parse(error))
                    } else AccountViewEffect.ShowGenericSnackBar(FailureMessage.parse(error))
                }, {
                    viewEffect = AccountViewEffect.OpenEmailVerification
                })
        }
    }
}