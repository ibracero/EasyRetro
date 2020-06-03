package com.easyretro.ui.board

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import arrow.core.Either
import com.easyretro.CoroutineTestRule
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.*
import com.easyretro.ui.FailureMessage
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StatementViewModelTest {

    @get:Rule
    val coroutinesTestRule = CoroutineTestRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val retroRepository = mock<RetroRepository>()
    private val boardRepository = mock<BoardRepository>()

    private val retroUuid = "retro-uuid"
    private val userEmail = "email@email.com"
    private lateinit var statementList: List<Statement>
    private lateinit var domainRetro: Retro

    private lateinit var viewModel: StatementViewModel

    private val viewStateObserver = mock<Observer<StatementListViewState>>()
    private val viewEffectsObserver = mock<Observer<StatementListViewEffect>>()

    @Before
    fun `Set up`() {
        initModelMocks()
        runBlocking {
            viewModel = StatementViewModel(retroRepository = retroRepository, boardRepository = boardRepository)
            viewModel.viewStates().observeForever(viewStateObserver)
            viewModel.viewEffects().observeForever(viewEffectsObserver)
        }
    }

    //region fetch statements
    @Test
    fun `GIVEN statement list saved to the database WHEN fetching statements THEN update viewState with statements`() {
        runBlocking {
            whenever(boardRepository.getStatements(retroUuid, StatementType.POSITIVE))
                .thenReturn(flowOf(statementList))

            viewModel.process(StatementListViewEvent.FetchStatements(retroUuid, StatementType.POSITIVE))

            val argumentCaptor = argumentCaptor<StatementListViewState>()
            verify(viewStateObserver, times(2)).onChanged(argumentCaptor.capture())
            assertEquals(emptyList<Statement>(), argumentCaptor.firstValue.statements)
            assertEquals(statementList, argumentCaptor.secondValue.statements)
        }
    }
    //endregion

    //region check retro protection
    @Test
    fun `GIVEN values WHEN checking retro protection THEN update the view`() {
        runBlocking {
            val protectedRetro = Either.right(domainRetro)
            val unprotectedRetro = Either.right(domainRetro.copy(protected = false))
            whenever(retroRepository.observeRetro(retroUuid))
                .thenReturn(flowOf(protectedRetro, unprotectedRetro, protectedRetro))

            viewModel.process(StatementListViewEvent.CheckRetroLock(retroUuid))

            val argumentCaptor = argumentCaptor<StatementListViewState>()
            verify(viewStateObserver, times(4)).onChanged(argumentCaptor.capture())
            assertEquals(StatementAddState.Hidden, argumentCaptor.firstValue.addState)
            assertEquals(StatementAddState.Hidden, argumentCaptor.secondValue.addState)
            assertEquals(StatementAddState.Shown, argumentCaptor.thirdValue.addState)
            assertEquals(StatementAddState.Hidden, argumentCaptor.allValues[3].addState)
        }
    }

    @Test
    fun `GIVEN error WHEN checking retro protection THEN show snackbar`() {
        runBlocking {
            whenever(retroRepository.observeRetro(retroUuid))
                .thenReturn(flowOf(Either.left(Failure.RetroNotFoundError)))

            viewModel.process(StatementListViewEvent.CheckRetroLock(retroUuid))

            verify(viewEffectsObserver)
                .onChanged(StatementListViewEffect.ShowSnackBar(FailureMessage.parse(Failure.RetroNotFoundError)))
        }
    }
    //endregion

    //region add statement
    @Test
    fun `GIVEN success response WHEN adding a statement THEN update viewState`() {
        runBlocking {
            val statementDescription = "Something new"
            whenever(
                boardRepository.addStatement(
                    retroUuid = retroUuid,
                    description = statementDescription,
                    type = StatementType.NEGATIVE
                )
            ).thenReturn(Either.right(Unit))

            viewModel.process(
                StatementListViewEvent.AddStatement(
                    retroUuid = retroUuid,
                    description = statementDescription,
                    type = StatementType.NEGATIVE
                )
            )
            verify(viewEffectsObserver).onChanged(StatementListViewEffect.CreateItemSuccess)
        }
    }

    @Test
    fun `GIVEN failed response WHEN adding a statement THEN update viewState`() {
        runBlocking {
            val statementDescription = "Something new"
            whenever(
                boardRepository.addStatement(
                    retroUuid = retroUuid,
                    description = statementDescription,
                    type = StatementType.NEGATIVE
                )
            ).thenReturn(Either.left(Failure.UnavailableNetwork))

            viewModel.process(
                StatementListViewEvent.AddStatement(
                    retroUuid = retroUuid,
                    description = statementDescription,
                    type = StatementType.NEGATIVE
                )
            )
            verify(viewEffectsObserver)
                .onChanged(StatementListViewEffect.ShowSnackBar(FailureMessage.parse(Failure.UnavailableNetwork)))
            verify(viewEffectsObserver)
                .onChanged(StatementListViewEffect.CreateItemFailed)
        }
    }
    //endregion

    //region remove statement
    @Test
    fun `GIVEN success response WHEN removing a statement THEN do nothing`() {
        runBlocking {
            val statementUuid = "statement-uuid"
            whenever(boardRepository.removeStatement(retroUuid = retroUuid, statementUuid = statementUuid))
                .thenReturn(Either.right(Unit))

            viewModel.process(
                StatementListViewEvent.RemoveStatement(
                    retroUuid = retroUuid,
                    statementUuid = statementUuid
                )
            )

            verifyZeroInteractions(viewEffectsObserver)
        }
    }

    @Test
    fun `GIVEN failed response WHEN removing a statement THEN show snackbar`() {
        runBlocking {
            val statementUuid = "statement-uuid"
            whenever(boardRepository.removeStatement(retroUuid = retroUuid, statementUuid = statementUuid))
                .thenReturn(Either.left(Failure.UnknownError))

            viewModel.process(
                StatementListViewEvent.RemoveStatement(
                    retroUuid = retroUuid,
                    statementUuid = statementUuid
                )
            )

            verify(viewEffectsObserver)
                .onChanged(StatementListViewEffect.ShowSnackBar(FailureMessage.parse(Failure.UnknownError)))
        }
    }
    //endregion

    private fun initModelMocks() {
        val statementUuid = "statement-uuid"
        val description = "statement description"
        val statementTimestamp = 1586705438L

        statementList = listOf(
            Statement(
                uuid = statementUuid,
                retroUuid = retroUuid,
                type = StatementType.POSITIVE,
                userEmail = userEmail,
                description = description,
                timestamp = statementTimestamp,
                removable = false
            )
        )

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

        val retroTitle = "Retro title"
        val retroDeepLink = "http://www.easyretro.com/join"

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