package com.easyretro.ui.board

import app.cash.turbine.test
import arrow.core.Either
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.Retro
import com.easyretro.domain.model.User
import com.easyretro.ui.CoroutinesTestRule
import com.easyretro.ui.FailureMessage
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    private val retroRepository = mock<RetroRepository>()
    private val boardRepository = mock<BoardRepository>()

    private val viewModel = BoardViewModel(retroRepository = retroRepository, boardRepository = boardRepository)


    @Before
    fun `Set up`() {
        initModelMocks()
    }

    @Test
    fun `GIVEN success response WHEN getting retro info THEN update viewstate with retro`() = runTest {
        whenever(retroRepository.observeRetro(retroUuid))
            .thenReturn(flowOf(Either.right(domainRetro)))

        viewModel.viewStates().test {
            viewModel.process(BoardContract.Event.GetRetroInfo(retroUuid))

            val state = expectMostRecentItem().retroState
            assertEquals(BoardContract.RetroState.RetroLoaded(domainRetro), state)
        }
    }

    @Test
    fun `GIVEN failed response WHEN getting retro info THEN update viewstate with retro`() = runTest {
        whenever(retroRepository.observeRetro(retroUuid))
            .thenReturn(flowOf(Either.left(Failure.UnknownError)))

        viewModel.viewEffects().test {
            viewModel.process(BoardContract.Event.GetRetroInfo(retroUuid))

            val effect = awaitItem()
            assertEquals(BoardContract.Effect.ShowSnackBar(FailureMessage.parse(Failure.UnknownError)), effect)
        }
    }

    @Test
    fun `GIVEN current retro with deeplink WHEN sharing retro THEN show sharesheet`() = runTest {
        whenever(retroRepository.observeRetro(retroUuid))
            .thenReturn(flowOf(Either.right(domainRetro)))
        viewModel.process(BoardContract.Event.GetRetroInfo(retroUuid))

        viewModel.viewEffects().test {
            viewModel.process(BoardContract.Event.ShareRetroLink)

            val effect = awaitItem()
            val expected = BoardContract.Effect.ShowShareSheet(retroTitle = retroTitle, deepLink = retroDeepLink)
            assertEquals(expected, effect)
        }
    }

    @Test
    fun `GIVEN retro without deeplink WHEN sharing retro THEN show snackbar`() = runTest {
        val retroWithoutDeepLink = domainRetro.copy(deepLink = "")
        whenever(retroRepository.observeRetro(retroUuid))
            .thenReturn(flowOf(Either.right(retroWithoutDeepLink)))
        viewModel.process(BoardContract.Event.GetRetroInfo(retroUuid))

        viewModel.viewEffects().test {
            viewModel.process(BoardContract.Event.ShareRetroLink)

            val effect = awaitItem()
            val expected = BoardContract.Effect.ShowSnackBar(FailureMessage.parse(Failure.UnknownError))
            assertEquals(expected, effect)
        }
    }

    @Test
    fun `GIVEN failed response WHEN protecting retro THEN show snackbar`() = runTest {
        whenever(retroRepository.protectRetro(retroUuid))
            .thenReturn(Either.left(Failure.UnavailableNetwork))

        viewModel.viewEffects().test {
            viewModel.process(BoardContract.Event.ProtectRetro(retroUuid))

            val effect = awaitItem()
            val expected = BoardContract.Effect.ShowSnackBar(FailureMessage.parse(Failure.UnavailableNetwork))
            assertEquals(expected, effect)
        }
    }

    @Test
    fun `GIVEN failed response WHEN unprotecting retro THEN show snackbar`() = runTest {
        whenever(retroRepository.unprotectRetro(retroUuid))
            .thenReturn(Either.left(Failure.UnavailableNetwork))

        viewModel.viewEffects().test {
            viewModel.process(BoardContract.Event.UnprotectRetro(retroUuid))

            val effect = awaitItem()
            val expected = BoardContract.Effect.ShowSnackBar(FailureMessage.parse(Failure.UnavailableNetwork))
            assertEquals(expected, effect)
        }
    }

    @Test
    fun `GIVEN a valid uuid WHEN subscribing retro details THEN call repository`() {
        return runTest {
            whenever(retroRepository.startObservingRetroDetails(retroUuid)).thenReturn(flowOf(Either.right(Unit)))
            whenever(boardRepository.startObservingStatements(retroUuid)).thenReturn(flowOf(Either.right(Unit)))

            viewModel.process(BoardContract.Event.SubscribeRetroDetails(retroUuid))

            verify(retroRepository).startObservingRetroDetails(retroUuid)
            verify(boardRepository).startObservingStatements(retroUuid)
        }
    }

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