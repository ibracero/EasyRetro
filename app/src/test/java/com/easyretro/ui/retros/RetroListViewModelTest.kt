package com.easyretro.ui.retros

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import arrow.core.Either
import com.easyretro.CoroutineTestRule
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.Retro
import com.easyretro.ui.FailureMessage
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RetroListViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val retroRepository = mock<RetroRepository>()
    private val accountRepository = mock<AccountRepository>()

    lateinit var viewModel: RetroListViewModel

    private val viewStateObserver = mock<Observer<RetroListViewState>>()
    private val viewEffectsObserver = mock<Observer<RetroListViewEffect>>()

    @Before
    fun `Set up`() {
        runBlocking {
            viewModel = RetroListViewModel(
                retroRepository = retroRepository,
                accountRepository = accountRepository
            )
        }
    }

    //region fetch retros
    @Test
    fun `GIVEN success response WHEN FetchRetros view event THEN modify view state with retro list`() {
        runBlocking {
            whenever(retroRepository.getRetros()).thenReturn(flowOf(Either.right(getMockRetroList())))

            viewModel.viewStates().observeForever(viewStateObserver)
            viewModel.process(RetroListViewEvent.FetchRetros)

            val initialViewState = RetroListViewState(FetchRetrosStatus.NotFetched, RetroCreationStatus.NotCreated)
            val loadingViewState = RetroListViewState(FetchRetrosStatus.Loading, RetroCreationStatus.NotCreated)
            val fetchedViewState = RetroListViewState(
                FetchRetrosStatus.Fetched(retros = getMockRetroList()),
                RetroCreationStatus.NotCreated
            )
            inOrder(viewStateObserver) {
                verify(viewStateObserver).onChanged(initialViewState)
                verify(viewStateObserver).onChanged(loadingViewState)
                verify(viewStateObserver).onChanged(fetchedViewState)
                verifyNoMoreInteractions(viewStateObserver)
            }
        }
    }

    @Test
    fun `GIVEN failed response WHEN FetchRetros view event THEN modify view state and view effect`() {
        runBlocking {
            whenever(retroRepository.getRetros()).thenReturn(flowOf(Either.left(Failure.UnavailableNetwork)))

            viewModel.viewStates().observeForever(viewStateObserver)
            viewModel.viewEffects().observeForever(viewEffectsObserver)
            viewModel.process(RetroListViewEvent.FetchRetros)

            val initialViewState = RetroListViewState(FetchRetrosStatus.NotFetched, RetroCreationStatus.NotCreated)
            val loadingViewState = RetroListViewState(FetchRetrosStatus.Loading, RetroCreationStatus.NotCreated)
            val notFetchedViewState = RetroListViewState(FetchRetrosStatus.NotFetched, RetroCreationStatus.NotCreated)
            inOrder(viewStateObserver, viewEffectsObserver) {
                verify(viewStateObserver).onChanged(initialViewState)
                verify(viewStateObserver).onChanged(loadingViewState)
                verify(viewStateObserver).onChanged(notFetchedViewState)
                verify(viewEffectsObserver)
                    .onChanged(RetroListViewEffect.ShowSnackBar(FailureMessage.parse(Failure.UnavailableNetwork)))
                verifyNoMoreInteractions(viewStateObserver)
                verifyNoMoreInteractions(viewEffectsObserver)
            }
        }
    }
    //endregion

    private fun getMockRetroList(): List<Retro> {
        return listOf(
            Retro(
                uuid = "uuid-1",
                title = "title 1",
                timestamp = 1000000L,
                lockingAllowed = true,
                protected = true,
                users = emptyList()
            ),
            Retro(
                uuid = "uuid-2",
                title = "title 2",
                timestamp = 1000001L,
                lockingAllowed = true,
                protected = true,
                users = emptyList()
            ),
            Retro(
                uuid = "uuid-3",
                title = "title 3",
                timestamp = 1000002L,
                lockingAllowed = true,
                protected = true,
                users = emptyList()
            )

        )
    }
}