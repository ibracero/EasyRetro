package com.easyretro.ui.board

import androidx.annotation.StringRes
import com.easyretro.domain.model.Retro

class BoardContract {

    data class State(val retroState: RetroState)

    sealed class RetroState {
        object Loading : RetroState()
        data class RetroLoaded(val retro: Retro) : RetroState()
    }

    sealed class Effect {
        data class ShowSnackBar(@StringRes val errorMessage: Int) : Effect()
        data class ShowShareSheet(val retroTitle: String, val deepLink: String) : Effect()
    }

    sealed class Event {
        data class GetRetroInfo(val retroUuid: String) : Event()
        data class JoinRetro(val retroUuid: String) : Event()
        data class SubscribeRetroDetails(val retroUuid: String) : Event()
        data class ProtectRetro(val retroUuid: String) : Event()
        data class UnprotectRetro(val retroUuid: String) : Event()
        object ShareRetroLink : Event()
        object UnsubscribeRetroDetails : Event()
    }
}