package com.easyretro.ui.board

import android.net.Uri
import androidx.annotation.StringRes
import com.easyretro.domain.model.Retro

data class BoardViewState(val retro: Retro)

sealed class BoardViewEffect {
    data class ShowSnackBar(@StringRes val errorMessage: Int) : BoardViewEffect()
    data class ShowShareSheet(val retroTitle: String, val shortLink: Uri) : BoardViewEffect()
}

sealed class BoardViewEvent {
    data class GetRetroInfo(val retroUuid: String) : BoardViewEvent()
    data class JoinRetro(val retroUuid: String) : BoardViewEvent()
    data class ShareRetroLink(val retroUuid: String, val link: String) : BoardViewEvent()
    data class SubscribeRetroDetails(val retroUuid: String) : BoardViewEvent()
    data class LockRetro(val retroUuid: String): BoardViewEvent()
    data class UnlockRetro(val retroUuid: String): BoardViewEvent()
    object UnsubscribeRetroDetails : BoardViewEvent()
}