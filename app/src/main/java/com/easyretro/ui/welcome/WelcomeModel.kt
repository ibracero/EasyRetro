package com.easyretro.ui.welcome

import androidx.annotation.StringRes
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed class WelcomeViewState {
    object Splash : WelcomeViewState()
    object LoginOptionsDisplayed : WelcomeViewState()
    object LoginInProgress : WelcomeViewState()
}

sealed class WelcomeViewEffect {
    object NavigateToGoogleSignIn : WelcomeViewEffect()
    object NavigateToEmailLogin : WelcomeViewEffect()
    object NavigateToSignUp : WelcomeViewEffect()
    object NavigateToRetros : WelcomeViewEffect()
    data class GoogleSignInError(@StringRes val errorRes: Int) : WelcomeViewEffect()
}

sealed class WelcomeViewEvent {
    object GoogleSignInClicked : WelcomeViewEvent()
    object EmailSignInClicked : WelcomeViewEvent()
    object SignUpClicked : WelcomeViewEvent()
    data class GoogleSignInResultReceived(val account: GoogleSignInAccount?) : WelcomeViewEvent()
}