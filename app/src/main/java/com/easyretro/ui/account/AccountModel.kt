package com.easyretro.ui.account

import androidx.annotation.StringRes

data class AccountViewState(val signingState: SigningState)

sealed class AccountViewEffect {
    object OpenResetPassword : AccountViewEffect()
    object OpenRetroList : AccountViewEffect()
    object OpenEmailVerification : AccountViewEffect()
    data class ShowGenericSnackBar(@StringRes val errorMessage: Int) : AccountViewEffect()
    data class ShowExistingUserSnackBar(@StringRes val errorMessage: Int) : AccountViewEffect()
    data class ShowUnknownUserSnackBar(@StringRes val errorMessage: Int) : AccountViewEffect()
}

sealed class AccountViewEvent {
    object ResetPassword : AccountViewEvent()
    data class SignIn(val email: String, val password: String) : AccountViewEvent()
    data class SignUp(val email: String, val password: String) : AccountViewEvent()
}

sealed class SigningState {
    object Loading : SigningState()
    object RequestDone : SigningState()
}