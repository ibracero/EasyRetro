package com.easyretro.ui.welcome

import androidx.lifecycle.viewModelScope
import com.easyretro.analytics.Screen
import com.easyretro.analytics.UiValue
import com.easyretro.analytics.events.TapEvent
import com.easyretro.analytics.events.UserGoogleSignedInEvent
import com.easyretro.analytics.reportAnalytics
import com.easyretro.common.BaseFlowViewModel
import com.easyretro.common.ViewModelFlowContract
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.User
import com.easyretro.domain.model.UserStatus
import com.easyretro.ui.FailureMessage
import com.easyretro.ui.welcome.WelcomeContract.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val repository: AccountRepository
) : BaseFlowViewModel<State, Effect, Event>(),
    ViewModelFlowContract<Event> {

    companion object {
        const val STARTUP_DELAY = 1500L
    }

    override fun createInitialState(): State = State(isLoadingShown = false, areLoginButtonsShown = false)

    override fun process(uiEvent: Event) {
        super.process(uiEvent)
        when (uiEvent) {
            Event.ScreenLoaded -> checkUserSession()
            Event.GoogleSignInClicked -> {
                reportAnalytics(TapEvent(screen = Screen.WELCOME, uiValue = UiValue.GOOGLE_SIGN_IN))
                emitUiEffect(Effect.NavigateToGoogleSignIn)
            }
            Event.EmailSignInClicked -> {
                reportAnalytics(TapEvent(screen = Screen.WELCOME, uiValue = UiValue.WELCOME_EMAIL_SIGN_IN))
                emitUiEffect(Effect.NavigateToEmailLogin)
            }
            Event.SignUpClicked -> {
                reportAnalytics(TapEvent(screen = Screen.WELCOME, uiValue = UiValue.WELCOME_EMAIL_SIGN_UP))
                emitUiEffect(Effect.NavigateToSignUp)
            }
            is Event.GoogleSignInResultReceived -> {
                emitUiState { copy(isLoadingShown = true, areLoginButtonsShown = false) }
                if (uiEvent.account != null) firebaseAuthWithGoogle(uiEvent.account)
                else emitUiEffect(Effect.ShowError(FailureMessage.parse(Failure.UnknownError)))
            }
        }
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            repository.getUserStatus().fold({
                emitUiState { copy(isLoadingShown = false, areLoginButtonsShown = true) }
            }, {
                delay(STARTUP_DELAY)
                if (it == UserStatus.VERIFIED) {
                    emitUiEffect(Effect.NavigateToRetros)
                } else {
                    emitUiState { copy(isLoadingShown = false, areLoginButtonsShown = true) }
                }
            })
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val safeTokenId = account.idToken ?: return

        viewModelScope.launch {
            repository.signWithGoogleAccount(
                idToken = safeTokenId,
                user = User(
                    email = account.email.orEmpty(),
                    firstName = account.givenName.orEmpty(),
                    lastName = account.familyName.orEmpty(),
                    photoUrl = account.photoUrl?.toString().orEmpty()
                )
            ).fold({
                emitUiEffect(Effect.ShowError(FailureMessage.parse(it)))
            }, {
                reportAnalytics(event = UserGoogleSignedInEvent)
                emitUiEffect(Effect.NavigateToRetros)
            })
        }
    }
}