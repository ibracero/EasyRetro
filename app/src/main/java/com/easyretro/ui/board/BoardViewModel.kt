package com.easyretro.ui.board

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.easyretro.R
import com.easyretro.common.BaseViewModel
import com.easyretro.common.extensions.exhaustive
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.ui.FailureMessage
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class BoardViewModel(
    private val retroRepository: RetroRepository,
    private val boardRepository: BoardRepository
) : BaseViewModel<BoardViewState, BoardViewEffect, BoardViewEvent>() {

    companion object {
        private const val DEEPLINK_DOMAIN = "https://easyretro.page.link"
    }

    private var statementObserverJob: Job? = null
    private var retroObserverJob: Job? = null

    override fun process(viewEvent: BoardViewEvent) {
        super.process(viewEvent)
        when (viewEvent) {
            is BoardViewEvent.GetRetroInfo -> getRetroInfo(retroUuid = viewEvent.retroUuid)
            is BoardViewEvent.JoinRetro -> joinRetro(retroUuid = viewEvent.retroUuid)
            is BoardViewEvent.ShareRetroLink -> shareRetroLink(link = viewEvent.link)
            is BoardViewEvent.SubscribeRetroDetails -> startObservingRetro(retroUuid = viewEvent.retroUuid)
            is BoardViewEvent.LockRetro -> lockRetro(retroUuid = viewEvent.retroUuid)
            is BoardViewEvent.UnlockRetro -> unlockRetro(retroUuid = viewEvent.retroUuid)
            BoardViewEvent.UnsubscribeRetroDetails -> stopObservingRetro()
        }.exhaustive
    }

    private fun getRetroInfo(retroUuid: String) {
        viewModelScope.launch {
            retroRepository.observeRetro(retroUuid).collect { either ->
                either.fold(
                    {
                        viewEffect = BoardViewEffect.ShowSnackBar(errorMessage = FailureMessage.parse(it))
                    }, {
                        viewState = BoardViewState(retro = it)
                    }
                )
            }
        }
    }

    private fun joinRetro(retroUuid: String) {
        viewModelScope.launch {
            retroRepository.joinRetro(retroUuid).mapLeft {
                viewEffect = BoardViewEffect.ShowSnackBar(errorMessage = FailureMessage.parse(it))
            }
        }
    }

    private fun shareRetroLink(link: String) {
        generateDeepLink(link)
    }

    private fun generateDeepLink(link: String) {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse(link))
            .setDomainUriPrefix(DEEPLINK_DOMAIN)
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
            .buildShortDynamicLink()
            .addOnSuccessListener { shortLink ->
                val retro = viewStates().value?.retro
                if (retro != null) displayShareSheet(retroTitle = retro.title, link = shortLink)
                else viewEffect = BoardViewEffect.ShowSnackBar(R.string.error_generic)
            }
    }

    private fun displayShareSheet(retroTitle: String, link: ShortDynamicLink) {
        viewEffect = link.shortLink?.let { shortLink ->
            BoardViewEffect.ShowShareSheet(retroTitle = retroTitle, shortLink = shortLink)
        } ?: BoardViewEffect.ShowSnackBar(R.string.error_generic)
    }

    private fun unlockRetro(retroUuid: String) {
        viewModelScope.launch {
            retroRepository.unlockRetro(retroUuid = retroUuid)
                .mapLeft {
                    viewEffect = BoardViewEffect.ShowSnackBar(errorMessage = FailureMessage.parse(it))
                }
        }
    }

    private fun lockRetro(retroUuid: String) {
        viewModelScope.launch {
            retroRepository.lockRetro(retroUuid = retroUuid)
                .mapLeft {
                    viewEffect = BoardViewEffect.ShowSnackBar(errorMessage = FailureMessage.parse(it))
                }
        }
    }

    private fun startObservingRetro(retroUuid: String) {
        statementObserverJob?.cancel()
        retroObserverJob?.cancel()
        statementObserverJob = viewModelScope.launch {
            boardRepository.startObservingStatements(retroUuid).collect()//todo handle failure
        }
        retroObserverJob = viewModelScope.launch {
            retroRepository.startObservingRetroDetails(retroUuid).collect()//todo handle failure
        }
    }

    private fun stopObservingRetro() {
        statementObserverJob?.cancel()
        retroObserverJob?.cancel()
    }
}