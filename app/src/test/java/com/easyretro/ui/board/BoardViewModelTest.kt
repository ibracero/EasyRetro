package com.easyretro.ui.board

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import arrow.core.Either
import com.easyretro.CoroutineTestRule
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.Retro
import com.easyretro.domain.model.User
import com.easyretro.ui.FailureMessage
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BoardViewModelTest {

    private val retroUuid = "retro-uuid"
    private val retroTitle = "Retro title"
    private val userEmail = "email@email.com"
    private val retroDeepLink = "http://www.easyretro.com/join"

    private lateinit var domainRetro: Retro

    @get:Rule
    val coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val retroRepository = mock<RetroRepository>()
    private val boardRepository = mock<BoardRepository>()

    private val viewModel = BoardViewModel(retroRepository = retroRepository, boardRepository = boardRepository)

    private val viewStateObserver = mock<Observer<BoardViewState>>()
    private val viewEffectsObserver = mock<Observer<BoardViewEffect>>()

    @Before
    fun `Set up`() {
        initModelMocks()
        viewModel.viewStates().observeForever(viewStateObserver)
        viewModel.viewEffects().observeForever(viewEffectsObserver)
    }

    @After
    fun `Tear down`() {
        viewModel.viewStates().removeObserver(viewStateObserver)
        viewModel.viewEffects().removeObserver(viewEffectsObserver)
    }

    //region get retro info
    @Test
    fun `GIVEN success response WHEN getting retro info THEN update viewstate with retro`() {
        runBlocking {
            whenever(retroRepository.observeRetro(retroUuid))
                .thenReturn(flowOf(Either.right(domainRetro)))

            viewModel.process(BoardViewEvent.GetRetroInfo(retroUuid))

            verifyZeroInteractions(viewEffectsObserver)
            verify(viewStateObserver).onChanged(BoardViewState(domainRetro))
        }
    }

    @Test
    fun `GIVEN failed response WHEN getting retro info THEN update viewstate with retro`() {
        runBlocking {
            whenever(retroRepository.observeRetro(retroUuid))
                .thenReturn(flowOf(Either.left(Failure.UnknownError)))

            viewModel.process(BoardViewEvent.GetRetroInfo(retroUuid))

            verifyZeroInteractions(viewStateObserver)
            verify(viewEffectsObserver).onChanged(BoardViewEffect.ShowSnackBar(FailureMessage.parse(Failure.UnknownError)))
        }
    }
    //endregion

    //region join retro
    @Test
    fun `GIVEN success response WHEN joining retro THEN do nothing`() {
        runBlocking {
            whenever(retroRepository.joinRetro(retroUuid))
                .thenReturn(Either.right(Unit))

            viewModel.process(BoardViewEvent.JoinRetro(retroUuid))

            verifyZeroInteractions(viewEffectsObserver)
            verifyZeroInteractions(viewStateObserver)
        }
    }

    @Test
    fun `GIVEN failed response WHEN joining retro THEN do nothing`() {
        runBlocking {
            whenever(retroRepository.joinRetro(retroUuid))
                .thenReturn(Either.left(Failure.UnavailableNetwork))

            viewModel.process(BoardViewEvent.JoinRetro(retroUuid))

            verifyZeroInteractions(viewStateObserver)
            verify(viewEffectsObserver)
                .onChanged(BoardViewEffect.ShowSnackBar(FailureMessage.parse(Failure.UnavailableNetwork)))
        }
    }
    //endregion

    //region share retro
    @Test
    fun `GIVEN current retro with deeplink WHEN sharing retro THEN show sharesheet`() {
        runBlocking {
            whenever(retroRepository.observeRetro(retroUuid))
                .thenReturn(flowOf(Either.right(domainRetro)))
            viewModel.process(BoardViewEvent.GetRetroInfo(retroUuid))

            viewModel.process(BoardViewEvent.ShareRetroLink)

            inOrder(viewStateObserver, viewEffectsObserver) {
                verify(viewStateObserver).onChanged(BoardViewState(domainRetro))
                verify(viewEffectsObserver).onChanged(
                    BoardViewEffect.ShowShareSheet(
                        retroTitle = retroTitle,
                        deepLink = retroDeepLink
                    )
                )
                verifyNoMoreInteractions(viewEffectsObserver)
            }
        }
    }

    @Test
    fun `GIVEN retro without deeplink WHEN sharing retro THEN show snackbar`() {
        runBlocking {
            val retroWithoutDeepLink = domainRetro.copy(deepLink = "")
            whenever(retroRepository.observeRetro(retroUuid))
                .thenReturn(flowOf(Either.right(retroWithoutDeepLink)))
            viewModel.process(BoardViewEvent.GetRetroInfo(retroUuid))

            viewModel.process(BoardViewEvent.ShareRetroLink)

            inOrder(viewStateObserver, viewEffectsObserver) {
                verify(viewStateObserver).onChanged(BoardViewState(retroWithoutDeepLink))
                verify(viewEffectsObserver)
                    .onChanged(BoardViewEffect.ShowSnackBar(FailureMessage.parse(Failure.UnknownError)))
                verifyNoMoreInteractions(viewEffectsObserver)
            }
        }
    }
    //endregion

    //region lock retro
    @Test
    fun `GIVEN success response WHEN protecting retro THEN do nothing`() {
        runBlocking {
            whenever(retroRepository.protectRetro(retroUuid))
                .thenReturn(Either.right(Unit))

            viewModel.process(BoardViewEvent.ProtectRetro(retroUuid))

            verifyZeroInteractions(viewEffectsObserver)
            verifyZeroInteractions(viewStateObserver)
        }
    }

    @Test
    fun `GIVEN failed response WHEN protecting retro THEN show snackbar`() {
        runBlocking {
            whenever(retroRepository.protectRetro(retroUuid))
                .thenReturn(Either.left(Failure.UnavailableNetwork))

            viewModel.process(BoardViewEvent.ProtectRetro(retroUuid))

            verify(viewEffectsObserver)
                .onChanged(BoardViewEffect.ShowSnackBar(FailureMessage.parse(Failure.UnavailableNetwork)))
        }
    }
    //endregion

    //region unlock retro
    @Test
    fun `GIVEN success response WHEN unprotecting retro THEN do nothing`() {
        runBlocking {
            whenever(retroRepository.unprotectRetro(retroUuid))
                .thenReturn(Either.right(Unit))

            viewModel.process(BoardViewEvent.UnprotectRetro(retroUuid))

            verifyZeroInteractions(viewEffectsObserver)
            verifyZeroInteractions(viewStateObserver)
        }
    }

    @Test
    fun `GIVEN failed response WHEN unprotecting retro THEN show snackbar`() {
        runBlocking {
            whenever(retroRepository.unprotectRetro(retroUuid))
                .thenReturn(Either.left(Failure.UnavailableNetwork))

            viewModel.process(BoardViewEvent.UnprotectRetro(retroUuid))

            verify(viewEffectsObserver)
                .onChanged(BoardViewEffect.ShowSnackBar(FailureMessage.parse(Failure.UnavailableNetwork)))
        }
    }
    //endregion

    //region start observing details
    @Test
    fun `GIVEN a valid uuid WHEN subscribing retro details THEN call repository`() {
        runBlocking {
            whenever(retroRepository.startObservingRetroDetails(retroUuid)).thenReturn(flowOf(Either.right(Unit)))
            whenever(boardRepository.startObservingStatements(retroUuid)).thenReturn(flowOf(Either.right(Unit)))

            viewModel.process(BoardViewEvent.SubscribeRetroDetails(retroUuid))

            verify(retroRepository).startObservingRetroDetails(retroUuid)
            verify(boardRepository).startObservingStatements(retroUuid)
        }
    }
    //endregion

    private fun initModelMocks() {
        val retroTimestamp = 1586705438L
        val userFirstName = "First name"
        val userLastName = "Last name"
        val userPhotoUrl = "photo.com/user1"

        val domainUserOne = User(
            email = userEmail,
            firstName = userFirstName,
            lastName = userLastName,
            photoUrl = userPhotoUrl
        )

        domainRetro = Retro(
            uuid = retroUuid,
            title = retroTitle,
            timestamp = retroTimestamp,
            deepLink = retroDeepLink,
            users = listOf(domainUserOne),
            lockingAllowed = true,
            protected = true
        )
    }
}