package com.easyretro.ui.board

import androidx.annotation.StringRes
import com.easyretro.domain.model.Retro

data class BoardViewState(val retro: Retro)

sealed class BoardViewEffect {
    data class ShowSnackBar(@StringRes val errorMessage: Int) : BoardViewEffect()
    data class ShowShareSheet(val retroTitle: String, val deepLink: String) : BoardViewEffect()
}

sealed class BoardViewEvent {
    data class GetRetroInfo(val retroUuid: String) : BoardViewEvent()
    data class JoinRetro(val retroUuid: String) : BoardViewEvent()
    data class SubscribeRetroDetails(val retroUuid: String) : BoardViewEvent()
    data class ProtectRetro(val retroUuid: String) : BoardViewEvent()
    data class UnprotectRetro(val retroUuid: String) : BoardViewEvent()
    object ShareRetroLink : BoardViewEvent()
    object UnsubscribeRetroDetails : BoardViewEvent()
}