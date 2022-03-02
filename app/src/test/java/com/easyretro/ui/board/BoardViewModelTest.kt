package com.easyretro.ui.board

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import arrow.core.Either
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.Retro
import com.easyretro.domain.model.User
import com.easyretro.ui.FailureMessage
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class BoardViewModelTest {

    private val retroUuid = "retro-uuid"
    private val retroTitle = "Retro title"
    private val userEmail = "email@email.com"
    private val retroDeepLink = "http://www.easyretro.com/join"

    private lateinit var domainRetro: Retro

    private val testCoroutineScope = TestCoroutineScope()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val retroRepository = mock<RetroRepository>()
    private val boardRepository = mock<BoardRepository>()

    private val viewModel = BoardViewModel(retroRepository = retroRepository, boardRepository = boardRepository)

    private val viewStateObserver = mock<Observer<State>>()
    private val viewEffectsObserver = mock<Observer<Effect>>()

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
        testCoroutineScope.launch {
            whenever(retroRepository.observeRetro(retroUuid))
                .thenReturn(flowOf(Either.right(domainRetro)))

            viewModel.process(Event.GetRetroInfo(retroUuid))

            verifyZeroInteractions(viewEffectsObserver)
            verify(viewStateObserver).onChanged(State(domainRetro))
        }
    }

    @Test
    fun `GIVEN failed response WHEN getting retro info THEN update viewstate with retro`() {
        testCoroutineScope.launch {
            whenever(retroRepository.observeRetro(retroUuid))
                .thenReturn(flowOf(Either.left(Failure.UnknownError)))

            viewModel.process(Event.GetRetroInfo(retroUuid))

            verifyZeroInteractions(viewStateObserver)
            verify(viewEffectsObserver).onChanged(Effect.ShowSnackBar(FailureMessage.parse(Failure.UnknownError)))
        }
    }
    //endregion

    //region join retro
    @Test
    fun `GIVEN success response WHEN joining retro THEN do nothing`() {
        testCoroutineScope.launch {
            whenever(retroRepository.joinRetro(retroUuid))
                .thenReturn(Either.right(Unit))

            viewModel.process(Event.JoinRetro(retroUuid))

            verifyZeroInteractions(viewEffectsObserver)
            verifyZeroInteractions(viewStateObserver)
        }
    }

    @Test
    fun `GIVEN failed response WHEN joining retro THEN do nothing`() {
        testCoroutineScope.launch {
            whenever(retroRepository.joinRetro(retroUuid))
                .thenReturn(Either.left(Failure.UnavailableNetwork))

            viewModel.process(Event.JoinRetro(retroUuid))

            verifyZeroInteractions(viewStateObserver)
            verify(viewEffectsObserver)
                .onChanged(Effect.ShowSnackBar(FailureMessage.parse(Failure.UnavailableNetwork)))
        }
    }
    //endregion

    //region share retro
    @Test
    fun `GIVEN current retro with deeplink WHEN sharing retro THEN show sharesheet`() {
        testCoroutineScope.launch {
            whenever(retroRepository.observeRetro(retroUuid))
                .thenReturn(flowOf(Either.right(domainRetro)))
            viewModel.process(Event.GetRetroInfo(retroUuid))

            viewModel.process(Event.ShareRetroLink)

            inOrder(viewStateObserver, viewEffectsObserver) {
                verify(viewStateObserver).onChanged(State(domainRetro))
                verify(viewEffectsObserver).onChanged(
                    Effect.ShowShareSheet(
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
        testCoroutineScope.launch {
            val retroWithoutDeepLink = domainRetro.copy(deepLink = "")
            whenever(retroRepository.observeRetro(retroUuid))
                .thenReturn(flowOf(Either.right(retroWithoutDeepLink)))
            viewModel.process(Event.GetRetroInfo(retroUuid))

            viewModel.process(Event.ShareRetroLink)

            inOrder(viewStateObserver, viewEffectsObserver) {
                verify(viewStateObserver).onChanged(State(retroWithoutDeepLink))
                verify(viewEffectsObserver)
                    .onChanged(Effect.ShowSnackBar(FailureMessage.parse(Failure.UnknownError)))
                verifyNoMoreInteractions(viewEffectsObserver)
            }
        }
    }
    //endregion

    //region lock retro
    @Test
    fun `GIVEN success response WHEN protecting retro THEN do nothing`() {
        testCoroutineScope.launch {
            whenever(retroRepository.protectRetro(retroUuid))
                .thenReturn(Either.right(Unit))

            viewModel.process(Event.ProtectRetro(retroUuid))

            verifyZeroInteractions(viewEffectsObserver)
            verifyZeroInteractions(viewStateObserver)
        }
    }

    @Test
    fun `GIVEN failed response WHEN protecting retro THEN show snackbar`() {
        testCoroutineScope.launch {
            whenever(retroRepository.protectRetro(retroUuid))
                .thenReturn(Either.left(Failure.UnavailableNetwork))

            viewModel.process(Event.ProtectRetro(retroUuid))

            verify(viewEffectsObserver)
                .onChanged(Effect.ShowSnackBar(FailureMessage.parse(Failure.UnavailableNetwork)))
        }
    }
    //endregion

    //region unlock retro
    @Test
    fun `GIVEN success response WHEN unprotecting retro THEN do nothing`() {
        testCoroutineScope.launch {
            whenever(retroRepository.unprotectRetro(retroUuid))
                .thenReturn(Either.right(Unit))

            viewModel.process(Event.UnprotectRetro(retroUuid))

            verifyZeroInteractions(viewEffectsObserver)
            verifyZeroInteractions(viewStateObserver)
        }
    }

    @Test
    fun `GIVEN failed response WHEN unprotecting retro THEN show snackbar`() {
        testCoroutineScope.launch {
            whenever(retroRepository.unprotectRetro(retroUuid))
                .thenReturn(Either.left(Failure.UnavailableNetwork))

            viewModel.process(Event.UnprotectRetro(retroUuid))

            verify(viewEffectsObserver)
                .onChanged(Effect.ShowSnackBar(FailureMessage.parse(Failure.UnavailableNetwork)))
        }
    }
    //endregion

    //region start observing details
    @Test
    fun `GIVEN a valid uuid WHEN subscribing retro details THEN call repository`() {
        testCoroutineScope.launch {
            whenever(retroRepository.startObservingRetroDetails(retroUuid)).thenReturn(flowOf(Either.right(Unit)))
            whenever(boardRepository.startObservingStatements(retroUuid)).thenReturn(flowOf(Either.right(Unit)))

            viewModel.process(Event.SubscribeRetroDetails(retroUuid))

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