package com.ibracero.retrum.ui.retros

import androidx.annotation.StringRes
import com.ibracero.retrum.data.local.Retro

data class RetroListViewState(val fetchRetrosStatus: FetchRetrosStatus, val retroCreationStatus: RetroCreationStatus)

sealed class RetroListViewEffect {
    data class ShowSnackBar(@StringRes val errorMessage: Int) : RetroListViewEffect()
    data class OpenRetroDetail(val retroUuid: String) : RetroListViewEffect()
}

sealed class RetroListViewEvent {
    object FetchRetros : RetroListViewEvent()
    data class RetroClicked(val retro: Retro) : RetroListViewEvent()
    data class CreateRetroClicked(val retroName: String) : RetroListViewEvent()
    object LogoutClicked : RetroListViewEvent()
}

sealed class FetchRetrosStatus {
    object Loading : FetchRetrosStatus()
    data class Fetched(val retros: List<Retro>) : FetchRetrosStatus()
    object NotFetched : FetchRetrosStatus()
}

sealed class RetroCreationStatus {
    object Loading : RetroCreationStatus()
    object Created : RetroCreationStatus()
    object NotCreated : RetroCreationStatus()
}