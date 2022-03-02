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
            is Event.ScreenLoaded -> emitUiState {
                val formState = if (uiEvent.isNewAccount) FormState.SignUpForm else FormState.SignInForm
                copy(formState = formState)
            }
            is Event.SignInClicked -> signIn(email = uiEvent.email, password = uiEvent.password)
            is Event.SignUpClicked -> signUp(email = uiEvent.email, password = uiEvent.password)
            Event.ResetPasswordClicked -> emitUiEffect(Effect.OpenResetPassword)
            Event.SnackBarSignInClicked -> emitUiState { copy(formState = FormState.SignInForm) }
            Event.SnackBarSignUpClicked -> emitUiState { copy(formState = FormState.SignUpForm) }
        }
    }

    private fun signIn(email: String, password: String) {
        viewModelScope.launch {
            emitUiState { copy(formState = FormState.Loading) }
            val signInResult = repository.signInWithEmail(email, password)
            emitUiState { copy(formState = FormState.SignInForm) }
            signInResult.fold(
                { error -> handleSignInError(error) },
                { userStatus -> handleUserSignedIn(userStatus) })
        }
    }

    private fun signUp(email: String, password: String) {
        viewModelScope.launch {
            emitUiState { copy(formState = FormState.Loading) }
            val signInResult = repository.signUpWithEmail(email, password)
            emitUiState { copy(formState = FormState.SignUpForm) }
            signInResult.fold(
                { error -> handleSignUpError(error) },
                { emitUiEffect(Effect.OpenEmailVerification) })
        }
    }

    private fun handleSignUpError(error: Failure) {
        val effect = if (error is Failure.UserCollisionFailure) {
            Effect.ShowExistingUserError(FailureMessage.parse(error))
        } else Effect.ShowGenericError(FailureMessage.parse(error))
        emitUiEffect(effect)
    }

    private fun handleUserSignedIn(userStatus: UserStatus) {
        val effect = when (userStatus) {
            UserStatus.VERIFIED -> Effect.OpenRetroList
            UserStatus.NON_VERIFIED -> Effect.OpenEmailVerification
        }
        emitUiEffect(effect)
    }

    private fun handleSignInError(error: Failure) {
        val effect = if (error is Failure.InvalidUserFailure) {
            Effect.ShowUnknownUserError(FailureMessage.parse(error))
        } else Effect.ShowGenericError(FailureMessage.parse(error))
        emitUiEffect(effect)
    }
}