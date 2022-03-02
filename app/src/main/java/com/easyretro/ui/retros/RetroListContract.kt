package com.easyretro.ui.retros

import androidx.annotation.StringRes
import com.easyretro.domain.model.Retro

class RetroListContract {
    data class State(val retroListState: RetroListState, val newRetroState: NewRetroState)

    sealed class RetroListState {
        object Loading : RetroListState()
        data class RetroListShown(val retros: List<Retro>? = null) : RetroListState()
    }

    sealed class NewRetroState {
        object TextInputShown : NewRetroState()
        object AddRetroShown : NewRetroState()
        object Loading : NewRetroState()
    }

    sealed class Effect {
        data class OpenRetroDetail(val retroUuid: String) : Effect()
        data class ShowSnackBar(@StringRes val errorMessage: Int) : Effect()
    }

    sealed class Event {
        object ScreenLoaded : Event()
        object LogoutClicked : Event()
        data class CreateRetroClicked(val retroTitle: String) : Event()
        data class RetroClicked(val retroUuid: String) : Event()
    }

}