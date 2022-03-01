package com.easyretro.ui.account

import androidx.annotation.StringRes

class AccountContract {

    data class State(val formState: FormState)

    sealed class FormState {
        object Loading : FormState()
        object SignInForm : FormState()
        object SignUpForm : FormState()
    }

    sealed class Effect {
        object OpenResetPassword : Effect()
        object OpenRetroList : Effect()
        object OpenEmailVerification : Effect()
        data class ShowGenericError(@StringRes val errorMessage: Int) : Effect()
        data class ShowExistingUserError(@StringRes val errorMessage: Int) : Effect()
        data class ShowUnknownUserError(@StringRes val errorMessage: Int) : Effect()
    }

    sealed class Event {
        data class ScreenLoaded(val isNewAccount: Boolean) : Event()
        object ResetPasswordClicked : Event()
        object SnackBarSignInClicked : Event()
        object SnackBarSignUpClicked : Event()
        data class SignInClicked(val email: String, val password: String) : Event()
        data class SignUpClicked(val email: String, val password: String) : Event()
    }
}