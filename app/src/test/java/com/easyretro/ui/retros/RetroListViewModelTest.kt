package com.easyretro.ui.retros

import app.cash.turbine.test
import arrow.core.Either
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.Retro
import com.easyretro.ui.CoroutinesTestRule
import com.easyretro.ui.FailureMessage
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class RetroListViewModelTest {

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    private val retroRepository = mock<RetroRepository>()
    private val accountRepository = mock<AccountRepository>()

    private val viewModel = RetroListViewModel(retroRepository = retroRepository, accountRepository = accountRepository)

    @Test
    fun `GIVEN success response WHEN screen loaded ui event THEN modify view state with retro list`() = runTest {
        whenever(retroRepository.getRetros()).thenReturn(flowOf(Either.right(getMockRetroList())))

        viewModel.viewStates().test {
            viewModel.process(RetroListContract.Event.ScreenLoaded)

            val state = expectMostRecentItem()
            assertEquals(RetroListContract.RetroListState.RetroListShown(getMockRetroList()), state.retroListState)
            assertEquals(RetroListContract.NewRetroState.AddRetroShown, state.newRetroState)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN failed response WHEN screen loaded ui event THEN show retros null value`() = runTest {
        whenever(retroRepository.getRetros()).thenReturn(flowOf(Either.left(Failure.UnknownError)))

        viewModel.viewStates().test {
            viewModel.process(RetroListContract.Event.ScreenLoaded)

            val state = expectMostRecentItem()
            assertEquals(RetroListContract.RetroListState.RetroListShown(null), state.retroListState)
            assertEquals(RetroListContract.NewRetroState.AddRetroShown, state.newRetroState)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN failed response WHEN fetching retros THEN show error message`() = runTest {
        whenever(retroRepository.getRetros()).thenReturn(flowOf(Either.left(Failure.UnknownError)))

        viewModel.viewEffects().test {
            viewModel.process(RetroListContract.Event.ScreenLoaded)

            val effect = awaitItem()
            assertEquals(RetroListContract.Effect.ShowSnackBar(FailureMessage.parse(Failure.UnknownError)), effect)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN success response WHEN creating a retro THEN reset ui state`() = runTest {
        val title = "My retro"
        whenever(retroRepository.createRetro(title)).thenReturn(Either.right(getMockRetroList()[0]))

        viewModel.viewStates().test {
            viewModel.process(RetroListContract.Event.CreateRetroClicked(title))

            val state = expectMostRecentItem()
            assertEquals(RetroListContract.RetroListState.Loading, state.retroListState)
            assertEquals(RetroListContract.NewRetroState.AddRetroShown, state.newRetroState)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN success response WHEN creating a retro THEN open retro`() = runTest {
        val title = "My retro"
        whenever(retroRepository.createRetro(title)).thenReturn(Either.right(getMockRetroList()[0]))

        viewModel.viewEffects().test {
            viewModel.process(RetroListContract.Event.CreateRetroClicked(title))

            val effect = awaitItem()
            assertEquals(RetroListContract.Effect.OpenRetroDetail(getMockRetroList()[0].uuid), effect)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN failed response WHEN creating a retro THEN keep show input`() = runTest {
        val title = "My retro"
        whenever(retroRepository.createRetro(title)).thenReturn(Either.left(Failure.UnknownError))

        viewModel.viewStates().test {
            viewModel.process(RetroListContract.Event.CreateRetroClicked(title))

            val state = expectMostRecentItem()
            assertEquals(RetroListContract.RetroListState.Loading, state.retroListState)
            assertEquals(RetroListContract.NewRetroState.TextInputShown, state.newRetroState)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN failed response WHEN creating a retro THEN show error`() = runTest {
        val title = "My retro"
        whenever(retroRepository.createRetro(title)).thenReturn(Either.left(Failure.UnknownError))

        viewModel.viewEffects().test {
            viewModel.process(RetroListContract.Event.CreateRetroClicked(title))

            val effect = awaitItem()
            assertEquals(RetroListContract.Effect.ShowSnackBar(FailureMessage.parse(Failure.UnknownError)), effect)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN open retro ui event THEN open retro`() = runTest {
        val retroUuid = "retroUuid"

        viewModel.viewEffects().test {
            viewModel.process(RetroListContract.Event.RetroClicked(retroUuid))

            val effect = awaitItem()
            assertEquals(RetroListContract.Effect.OpenRetroDetail(retroUuid), effect)
        }
    }

    @Test
    fun `GIVEN logout ui event THEN execute logout`() = runTest {
        whenever(accountRepository.logOut()).thenReturn(Unit)

        viewModel.process(RetroListContract.Event.LogoutClicked)

        verify(accountRepository).logOut()
    }

    private fun getMockRetroList(): List<Retro> {
        return listOf(
            Retro(
                uuid = "uuid-1",
                title = "title 1",
                timestamp = 1000000L,
                deepLink = "deeplink.com/1",
                lockingAllowed = true,
                protected = true,
                users = emptyList()
            ),
            Retro(
                uuid = "uuid-2",
                title = "title 2",
                timestamp = 1000001L,
                deepLink = "deeplink.com/2",
                lockingAllowed = true,
                protected = true,
                users = emptyList()
            ),
            Retro(
                uuid = "uuid-3",
                title = "title 3",
                timestamp = 1000002L,
                deepLink = "deeplink.com/3",
                lockingAllowed = true,
                protected = true,
                users = emptyList()
            )
        )
    }
}