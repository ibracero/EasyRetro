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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val repository: AccountRepository
) : BaseFlowViewModel<WelcomeViewState, WelcomeViewEffect, WelcomeViewEvent>(WelcomeViewState.Splash),
    ViewModelFlowContract<WelcomeViewEvent> {

    companion object {
        private const val STARTUP_DELAY = 1500L
    }

    init {
        checkUserSession()
    }

    override fun process(viewEvent: WelcomeViewEvent) {
        super.process(viewEvent)
        when (viewEvent) {
            WelcomeViewEvent.GoogleSignInClicked -> {
                reportAnalytics(TapEvent(screen = Screen.WELCOME, uiValue = UiValue.GOOGLE_SIGN_IN))
                viewEffect = WelcomeViewEffect.NavigateToGoogleSignIn
            }
            WelcomeViewEvent.EmailSignInClicked -> {
                reportAnalytics(TapEvent(screen = Screen.WELCOME, uiValue = UiValue.WELCOME_EMAIL_SIGN_IN))
                viewEffect = WelcomeViewEffect.NavigateToEmailLogin
            }
            WelcomeViewEvent.SignUpClicked -> {
                reportAnalytics(TapEvent(screen = Screen.WELCOME, uiValue = UiValue.WELCOME_EMAIL_SIGN_UP))
                viewEffect = WelcomeViewEffect.NavigateToSignUp
            }
            is WelcomeViewEvent.GoogleSignInResultReceived -> {
                viewState = WelcomeViewState.LoginInProgress
                if (viewEvent.account != null) firebaseAuthWithGoogle(viewEvent.account)
                else viewEffect = WelcomeViewEffect.GoogleSignInError(FailureMessage.parse(Failure.UnknownError))
            }
        }
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            repository.getUserStatus().fold({
                viewState = WelcomeViewState.LoginOptionsDisplayed
            }, {
                delay(STARTUP_DELAY)
                if (it == UserStatus.VERIFIED) {
                    viewEffect = WelcomeViewEffect.NavigateToRetros
                } else {
                    viewState = WelcomeViewState.LoginOptionsDisplayed
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
                viewEffect = WelcomeViewEffect.GoogleSignInError(FailureMessage.parse(it))
            }, {
                reportAnalytics(event = UserGoogleSignedInEvent)
                viewEffect = WelcomeViewEffect.NavigateToRetros
            })
        }
    }
}