package com.easyretro.ui.account

import androidx.lifecycle.viewModelScope
import com.easyretro.common.BaseFlowViewModel
import com.easyretro.domain.AccountRepository
import com.easyretro.ui.account.AccountContract.*
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.UserStatus
import com.easyretro.ui.FailureMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: AccountRepository
) : BaseFlowViewModel<State, Effect, Event>() {

    override fun createInitialState(): State = State(FormState.SignInForm)

    override fun process(uiEvent: Event) {
        super.process(uiEvent)
        when (uiEvent) {
            is Event.ScreenLoaded -> emitViewState {
                val formState = if (uiEvent.isNewAccount) FormState.SignUpForm else FormState.SignInForm
                copy(formState = formState)
            }
            is Event.SignInClicked -> signIn(email = uiEvent.email, password = uiEvent.password)
            is Event.SignUpClicked -> signUp(email = uiEvent.email, password = uiEvent.password)
            Event.ResetPasswordClicked -> emitViewEffect(Effect.OpenResetPassword)
            Event.SnackBarSignInClicked -> emitViewState { copy(formState = FormState.SignInForm) }
            Event.SnackBarSignUpClicked -> emitViewState { copy(formState = FormState.SignUpForm) }
        }
    }

    private fun signIn(email: String, password: String) {
        emitViewState { copy(formState = FormState.Loading) }
        viewModelScope.launch {
            val signInResult = repository.signInWithEmail(email, password)
            emitViewState { copy(formState = FormState.SignInForm) }
            signInResult.fold(
                { error -> handleSignInError(error) },
                { userStatus -> handleUserSignedIn(userStatus) })
        }
    }

    private fun signUp(email: String, password: String) {
        emitViewState { copy(formState = FormState.Loading) }
        viewModelScope.launch {
            val signInResult = repository.signUpWithEmail(email, password)
            emitViewState { copy(formState = FormState.SignUpForm) }
            signInResult.fold(
                { error -> handleSignUpError(error) },
                { emitViewEffect(Effect.OpenEmailVerification) })
        }
    }

    private fun handleSignUpError(error: Failure) {
        val effect = if (error is Failure.UserCollisionFailure) {
            Effect.ShowExistingUserError(FailureMessage.parse(error))
        } else Effect.ShowGenericError(FailureMessage.parse(error))
        emitViewEffect(effect)
    }

    private fun handleUserSignedIn(userStatus: UserStatus) {
        val effect = when (userStatus) {
            UserStatus.VERIFIED -> Effect.OpenRetroList
            UserStatus.NON_VERIFIED -> Effect.OpenEmailVerification
        }
        emitViewEffect(effect)
    }

    private fun handleSignInError(error: Failure) {
        val effect = if (error is Failure.InvalidUserFailure) {
            Effect.ShowUnknownUserError(FailureMessage.parse(error))
        } else Effect.ShowGenericError(FailureMessage.parse(error))
        emitViewEffect(effect)
    }
}