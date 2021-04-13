package com.easyretro.ui.retros

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import arrow.core.Either
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.Retro
import com.easyretro.ui.FailureMessage
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class RetroListViewModelTest {


    private val testCoroutineScope = TestCoroutineScope()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val retroRepository = mock<RetroRepository>()
    private val accountRepository = mock<AccountRepository>()

    lateinit var viewModel: RetroListViewModel

    private val viewStateObserver = mock<Observer<RetroListViewState>>()
    private val viewEffectsObserver = mock<Observer<RetroListViewEffect>>()

    @Before
    fun `Set up`() {
        viewModel = RetroListViewModel(
            retroRepository = retroRepository,
            accountRepository = accountRepository
        )
    }

    @After
    fun `Tear down`() {
        viewModel.viewStates().removeObserver(viewStateObserver)
        viewModel.viewEffects().removeObserver(viewEffectsObserver)
    }

    //region fetch retros
    @Test
    fun `GIVEN success response WHEN FetchRetros view event THEN modify view state with retro list`() {
        testCoroutineScope.launch {
            whenever(retroRepository.getRetros()).thenReturn(flowOf(Either.right(getMockRetroList())))
            viewModel.viewStates().observeForever(viewStateObserver)

            viewModel.process(RetroListViewEvent.FetchRetros)

            inOrder(viewStateObserver) {
                val argumentCaptor = argumentCaptor<RetroListViewState>()
                verify(viewStateObserver, times(3)).onChanged(argumentCaptor.capture())
                assertEquals(FetchRetrosStatus.NotFetched, argumentCaptor.firstValue.fetchRetrosStatus)
                assertEquals(FetchRetrosStatus.Loading, argumentCaptor.secondValue.fetchRetrosStatus)
                assertEquals(FetchRetrosStatus.Fetched(getMockRetroList()), argumentCaptor.thirdValue.fetchRetrosStatus)
                verifyNoMoreInteractions(viewStateObserver)
            }
        }
    }

    @Test
    fun `GIVEN failed response WHEN FetchRetros view event THEN modify view state and view effect`() {
        testCoroutineScope.launch {
            whenever(retroRepository.getRetros()).thenReturn(flowOf(Either.left(Failure.UnavailableNetwork)))
            viewModel.viewStates().observeForever(viewStateObserver)
            viewModel.viewEffects().observeForever(viewEffectsObserver)

            viewModel.process(RetroListViewEvent.FetchRetros)

            inOrder(viewStateObserver, viewEffectsObserver) {
                val argumentCaptor = argumentCaptor<RetroListViewState>()
                verify(viewStateObserver, times(3)).onChanged(argumentCaptor.capture())
                assertEquals(FetchRetrosStatus.NotFetched, argumentCaptor.firstValue.fetchRetrosStatus)
                assertEquals(FetchRetrosStatus.Loading, argumentCaptor.secondValue.fetchRetrosStatus)
                assertEquals(FetchRetrosStatus.NotFetched, argumentCaptor.thirdValue.fetchRetrosStatus)
                verify(viewEffectsObserver)
                    .onChanged(RetroListViewEffect.ShowSnackBar(FailureMessage.parse(Failure.UnavailableNetwork)))
                verifyNoMoreInteractions(viewStateObserver)
                verifyNoMoreInteractions(viewEffectsObserver)
            }
        }
    }
    //endregion

    //region retro clicked
    @Test
    fun `GIVEN a retro uuid WHEN retro clicked THEN cause a viewEffect to open that retro`() {
        testCoroutineScope.launch {
            val retroUuid = "retro-uuid"
            viewModel.viewEffects().observeForever(viewEffectsObserver)

            viewModel.process(RetroListViewEvent.RetroClicked(retroUuid = retroUuid))

            verify(viewEffectsObserver).onChanged(RetroListViewEffect.OpenRetroDetail(retroUuid = retroUuid))
        }
    }
    //endregion

    //region create retro clicked
    @Test
    fun `GIVEN server response success WHEN create retro clicked THEN update the viewState and open the detail`() {
        testCoroutineScope.launch {
            val retroTitle = "Sample title"
            val retro = getMockRetroList()[0]
            whenever(retroRepository.createRetro(retroTitle)).thenReturn(Either.right(retro))
            viewModel.viewStates().observeForever(viewStateObserver)
            viewModel.viewEffects().observeForever(viewEffectsObserver)

            viewModel.process(RetroListViewEvent.CreateRetroClicked(retroTitle = retroTitle))

            inOrder(viewStateObserver, viewEffectsObserver) {
                val argumentCaptor = argumentCaptor<RetroListViewState>()
                verify(viewStateObserver, times(3)).onChanged(argumentCaptor.capture())
                verify(viewEffectsObserver).onChanged(RetroListViewEffect.OpenRetroDetail(retro.uuid))
                assertEquals(RetroCreationStatus.NotCreated, argumentCaptor.firstValue.retroCreationStatus)
                assertEquals(RetroCreationStatus.Loading, argumentCaptor.secondValue.retroCreationStatus)
                assertEquals(RetroCreationStatus.Created, argumentCaptor.thirdValue.retroCreationStatus)
            }
        }
    }

    @Test
    fun `GIVEN server response failed WHEN create retro clicked THEN update the viewState and open the detail`() {
        testCoroutineScope.launch {
            val retroTitle = "Sample title"
            whenever(retroRepository.createRetro(retroTitle)).thenReturn(Either.left(Failure.CreateRetroError))
            viewModel.viewStates().observeForever(viewStateObserver)
            viewModel.viewEffects().observeForever(viewEffectsObserver)

            viewModel.process(RetroListViewEvent.CreateRetroClicked(retroTitle = retroTitle))

            inOrder(viewStateObserver, viewEffectsObserver) {
                val argumentCaptor = argumentCaptor<RetroListViewState>()
                verify(viewStateObserver, times(3)).onChanged(argumentCaptor.capture())
                verify(viewEffectsObserver)
                    .onChanged(RetroListViewEffect.ShowSnackBar(FailureMessage.parse(Failure.CreateRetroError)))
                assertEquals(argumentCaptor.firstValue.retroCreationStatus, RetroCreationStatus.NotCreated)
                assertEquals(argumentCaptor.secondValue.retroCreationStatus, RetroCreationStatus.Loading)
                assertEquals(argumentCaptor.thirdValue.retroCreationStatus, RetroCreationStatus.NotCreated)
            }
        }
    }
    //endregion

    //region logout
    @Test
    fun `GIVEN viewEvent WHEN logout THEN calls account repository`() {
        testCoroutineScope.launch {
            whenever(accountRepository.logOut()).thenReturn(Unit)

            viewModel.process(RetroListViewEvent.LogoutClicked)

            verify(accountRepository).logOut()
            verifyNoMoreInteractions(accountRepository)
            verifyZeroInteractions(retroRepository)
        }
    }
    //endregion

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