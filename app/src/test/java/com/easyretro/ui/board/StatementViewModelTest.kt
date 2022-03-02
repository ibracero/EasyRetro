package com.easyretro.ui.board

import app.cash.turbine.test
import arrow.core.Either
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.*
import com.easyretro.ui.CoroutinesTestRule
import com.easyretro.ui.FailureMessage
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class StatementViewModelTest {

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    private val retroRepository = mock<RetroRepository>()
    private val boardRepository = mock<BoardRepository>()

    private val retroUuid = "retro-uuid"
    private val userEmail = "email@email.com"
    private lateinit var statementList: List<Statement>
    private lateinit var domainRetro: Retro

    private val viewModel = StatementViewModel(retroRepository = retroRepository, boardRepository = boardRepository)

    @Before
    fun `Set up`() {
        initModelMocks()
    }

    @Test
    fun `GIVEN statement list saved to the database WHEN fetching statements THEN update viewState with statements`() =
        runTest {
            whenever(boardRepository.getStatements(retroUuid, StatementType.POSITIVE))
                .thenReturn(flowOf(statementList))

            viewModel.viewStates().test {
                viewModel.process(StatementListContract.Event.FetchStatements(retroUuid, StatementType.POSITIVE))

                val state = expectMostRecentItem()
                assertEquals(statementList, state.statements)
                assertEquals(StatementListContract.StatementAddState.Hidden, state.addState)
            }
        }

    @Test
    fun `GIVEN values WHEN checking retro protection THEN update the view`() = runTest {
        val protectedRetro = Either.right(domainRetro)
        val unprotectedRetro = Either.right(domainRetro.copy(protected = false))
        whenever(retroRepository.observeRetro(retroUuid))
            .thenReturn(flowOf(protectedRetro, unprotectedRetro, protectedRetro))

        viewModel.viewStates().test {
            viewModel.process(StatementListContract.Event.CheckRetroLock(retroUuid))

            assertEquals(StatementListContract.StatementAddState.Hidden, expectMostRecentItem().addState)
        }
    }

    @Test
    fun `GIVEN error WHEN checking retro protection THEN show snackbar`() = runTest {
        whenever(retroRepository.observeRetro(retroUuid))
            .thenReturn(flowOf(Either.left(Failure.RetroNotFoundError)))

        viewModel.viewEffects().test {
            viewModel.process(StatementListContract.Event.CheckRetroLock(retroUuid))

            assertEquals(
                StatementListContract.Effect.ShowSnackBar(FailureMessage.parse(Failure.RetroNotFoundError)),
                awaitItem()
            )
        }
    }

    @Test
    fun `GIVEN success response WHEN adding a statement THEN update viewState`() = runTest {
        val statementDescription = "Something new"
        whenever(
            boardRepository.addStatement(
                retroUuid = retroUuid,
                description = statementDescription,
                type = StatementType.NEGATIVE
            )
        ).thenReturn(Either.right(Unit))

        viewModel.viewEffects().test {
            viewModel.process(
                StatementListContract.Event.AddStatement(
                    retroUuid = retroUuid,
                    description = statementDescription,
                    type = StatementType.NEGATIVE
                )
            )

            assertEquals(StatementListContract.Effect.CreateItemSuccess, awaitItem())
        }
    }

    @Test
    fun `GIVEN failed response WHEN adding a statement THEN update viewState`() = runTest {
        val statementDescription = "Something new"
        whenever(
            boardRepository.addStatement(
                retroUuid = retroUuid,
                description = statementDescription,
                type = StatementType.NEGATIVE
            )
        ).thenReturn(Either.left(Failure.UnavailableNetwork))

        viewModel.viewEffects().test {
            viewModel.process(
                StatementListContract.Event.AddStatement(
                    retroUuid = retroUuid,
                    description = statementDescription,
                    type = StatementType.NEGATIVE
                )
            )

            assertEquals(
                StatementListContract.Effect.ShowSnackBar(FailureMessage.parse(Failure.UnavailableNetwork)),
                awaitItem()
            )
            assertEquals(StatementListContract.Effect.CreateItemFailed, awaitItem())
        }
    }

    @Test
    fun `GIVEN failed response WHEN removing a statement THEN show snackbar`() = runTest {
        val statementUuid = "statement-uuid"
        whenever(boardRepository.removeStatement(retroUuid = retroUuid, statementUuid = statementUuid))
            .thenReturn(Either.left(Failure.UnknownError))

        viewModel.viewEffects().test {
            viewModel.process(
                StatementListContract.Event.RemoveStatement(
                    retroUuid = retroUuid,
                    statementUuid = statementUuid
                )
            )

            assertEquals(
                StatementListContract.Effect.ShowSnackBar(FailureMessage.parse(Failure.UnknownError)),
                awaitItem()
            )
        }
    }

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