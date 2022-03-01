package com.easyretro.ui.welcome

import androidx.annotation.StringRes
import com.easyretro.common.UiEffect
import com.easyretro.common.UiEvent
import com.easyretro.common.UiState
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class WelcomeContract {

    data class State(
        val isLoadingShown: Boolean = false,
        val areLoginButtonsShown: Boolean = false
    ) : UiState

    sealed class Effect : UiEffect {
        object NavigateToGoogleSignIn : Effect()
        object NavigateToEmailLogin : Effect()
        object NavigateToSignUp : Effect()
        object NavigateToRetros : Effect()
        data class ShowError(@StringRes val errorRes: Int) : Effect()
    }

    sealed class Event : UiEvent {
        object ScreenLoaded : Event()
        object GoogleSignInClicked : Event()
        object EmailSignInClicked : Event()
        object SignUpClicked : Event()
        data class GoogleSignInResultReceived(val account: GoogleSignInAccount?) : Event()
    }
}