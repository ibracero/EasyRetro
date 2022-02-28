package com.easyretro.ui.welcome

import androidx.lifecycle.viewModelScope
import com.easyretro.analytics.Screen
import com.easyretro.analytics.UiValue
import com.easyretro.analytics.events.TapEvent
import com.easyretro.analytics.events.UserGoogleSignedInEvent
import com.easyretro.analytics.reportAnalytics
import com.easyretro.common.BaseFlowViewModel
import com.easyretro.common.CoroutineDispatcherProvider
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
    private val repository: AccountRepository,
    override val dispatchers: CoroutineDispatcherProvider
) : BaseFlowViewModel<State, Effect, Event>(),
    ViewModelFlowContract<Event> {

    companion object {
        private const val STARTUP_DELAY = 1500L
    }

    override fun createInitialState(): State = State(isLoadingShown = false, areLoginButtonsShown = false)

    override fun process(viewEvent: Event) {
        super.process(viewEvent)
        when (viewEvent) {
            Event.ScreenLoaded -> checkUserSession()
            Event.GoogleSignInClicked -> {
                reportAnalytics(TapEvent(screen = Screen.WELCOME, uiValue = UiValue.GOOGLE_SIGN_IN))
                emitViewEffect(Effect.NavigateToGoogleSignIn)
            }
            Event.EmailSignInClicked -> {
                reportAnalytics(TapEvent(screen = Screen.WELCOME, uiValue = UiValue.WELCOME_EMAIL_SIGN_IN))
                emitViewEffect(Effect.NavigateToEmailLogin)
            }
            Event.SignUpClicked -> {
                reportAnalytics(TapEvent(screen = Screen.WELCOME, uiValue = UiValue.WELCOME_EMAIL_SIGN_UP))
                emitViewEffect(Effect.NavigateToSignUp)
            }
            is Event.GoogleSignInResultReceived -> {
                emitViewState { copy(isLoadingShown = true, areLoginButtonsShown = false) }
                if (viewEvent.account != null) firebaseAuthWithGoogle(viewEvent.account)
                else emitViewEffect(Effect.GoogleSignInError(FailureMessage.parse(Failure.UnknownError)))
            }
        }
    }

    private fun checkUserSession() {
        viewModelScope.launch(dispatchers.io()) {
            repository.getUserStatus().fold({
                emitViewState { copy(isLoadingShown = false, areLoginButtonsShown = true) }
            }, {
                delay(STARTUP_DELAY)
                if (it == UserStatus.VERIFIED) {
                    emitViewEffect(Effect.NavigateToRetros)
                } else {
                    emitViewState { copy(isLoadingShown = false, areLoginButtonsShown = true) }
                }
            })
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val safeTokenId = account.idToken ?: return

        viewModelScope.launch(dispatchers.io()) {
            repository.signWithGoogleAccount(
                idToken = safeTokenId,
                user = User(
                    email = account.email.orEmpty(),
                    firstName = account.givenName.orEmpty(),
                    lastName = account.familyName.orEmpty(),
                    photoUrl = account.photoUrl?.toString().orEmpty()
                )
            ).fold({
                emitViewEffect(Effect.GoogleSignInError(FailureMessage.parse(it)))
            }, {
                reportAnalytics(event = UserGoogleSignedInEvent)
                emitViewEffect(Effect.NavigateToRetros)
            })
        }
    }
}