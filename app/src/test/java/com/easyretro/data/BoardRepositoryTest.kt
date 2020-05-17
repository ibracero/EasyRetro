package com.easyretro.data

import arrow.core.Either
import arrow.core.extensions.option.semiring.empty
import com.easyretro.CoroutineTestRule
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.mapper.StatementDbToDomainMapper
import com.easyretro.data.remote.AuthDataStore
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.data.remote.firestore.StatementRemote
import com.easyretro.data.remote.mapper.StatementRemoteToDbMapper
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.StatementType
import com.easyretro.test
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class BoardRepositoryTest {

    private val userEmail = "email@email.com"
    private val retroUuid = "retro-uuid"

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val localDataStore = mock<LocalDataStore>()
    private val remoteDataStore = mock<RemoteDataStore>()
    private val authDataStore = mock<AuthDataStore> {
        on { getCurrentUserEmail() }.thenReturn(userEmail)
    }

    private val statementRemoteToDbMapper = StatementRemoteToDbMapper()
    private val repository = BoardRepositoryImpl(
        localDataStore = localDataStore,
        remoteDataStore = remoteDataStore,
        authDataStore = authDataStore,
        statementRemoteToDbMapper = statementRemoteToDbMapper,
        statementDbToDomainMapper = StatementDbToDomainMapper(),
        dispatchers = coroutinesTestRule.testDispatcherProvider
    )

    //region add statement
    @Test
    fun `GIVEN success server response WHEN adding a new statement THEN return EitherRight`() {
        runBlocking {
            val statementDescription = "This is a positive description"
            val statementType = StatementType.POSITIVE
            val statementToAdd = StatementRemote(
                userEmail = userEmail,
                description = statementDescription,
                statementType = statementType.toString().toLowerCase()
            )
            whenever(remoteDataStore.addStatementToBoard(retroUuid, statementToAdd)).thenReturn(Either.right(Unit))

            val result =
                repository.addStatement(retroUuid = retroUuid, description = statementDescription, type = statementType)

            verify(remoteDataStore).addStatementToBoard(retroUuid = retroUuid, statementRemote = statementToAdd)
            verifyNoMoreInteractions(remoteDataStore)
            assertEquals(Either.right(Unit), result)
        }
    }

    @Test
    fun `GIVEN failed server response WHEN adding a new statement THEN return EitherLeft`() {
        runBlocking {
            val statementDescription = "This is a positive description"
            val statementType = StatementType.POSITIVE
            val statementToAdd = StatementRemote(
                userEmail = userEmail,
                description = statementDescription,
                statementType = statementType.toString().toLowerCase()
            )
            whenever(remoteDataStore.addStatementToBoard(retroUuid, statementToAdd))
                .thenReturn(Either.left(Failure.UnknownError))

            val result =
                repository.addStatement(retroUuid = retroUuid, description = statementDescription, type = statementType)

            verify(remoteDataStore).addStatementToBoard(retroUuid = retroUuid, statementRemote = statementToAdd)
            verifyNoMoreInteractions(remoteDataStore)
            assertEquals(Either.left(Failure.UnknownError), result)
        }
    }
    //endregion

    //region remove statement
    @Test
    fun `GIVEN success server response WHEN removing a statement THEN return EitherRight`() {
        runBlocking {
            val statementUuid = "statement-uuid"
            whenever(remoteDataStore.removeStatement(retroUuid, statementUuid)).thenReturn(Either.right(Unit))

            val result =
                repository.removeStatement(retroUuid = retroUuid, statementUuid = statementUuid)

            verify(remoteDataStore).removeStatement(retroUuid = retroUuid, statementUuid = statementUuid)
            verifyNoMoreInteractions(remoteDataStore)
            assertEquals(Either.right(Unit), result)
        }
    }

    @Test
    fun `GIVEN failed server response WHEN removing a statement THEN return EitherLeft`() {
        runBlocking {
            val statementUuid = "statement-uuid"
            whenever(remoteDataStore.removeStatement(retroUuid, statementUuid))
                .thenReturn(Either.left(Failure.UnknownError))

            val result =
                repository.removeStatement(retroUuid = retroUuid, statementUuid = statementUuid)

            verify(remoteDataStore).removeStatement(retroUuid = retroUuid, statementUuid = statementUuid)
            verifyNoMoreInteractions(remoteDataStore)
            assertEquals(Either.left(Failure.UnknownError), result)
        }
    }
    //endregion

    //region start observing statements
    @Test
    fun `GIVEN some statements coming from the server WHEN start observing them THEN store them locally`() {
        runBlocking {
            val serverFlow = flowOf(
                Either.right(emptyList()),
                Either.right(getMockStatementList())
            )
            whenever(remoteDataStore.observeStatements(userEmail, retroUuid))
                .thenReturn(serverFlow)

            val resultFlow = repository.startObservingStatements(retroUuid = retroUuid)

            resultFlow.test {
                assertEquals(Either.right(Unit), expectItem())
                assertEquals(Either.right(Unit), expectItem())
                verify(localDataStore).saveStatements(getMockStatementList().map(statementRemoteToDbMapper::map))
                verifyNoMoreInteractions(localDataStore)
                expectComplete()
            }
        }
    }

    @Test
    fun `GIVEN error coming from the server WHEN start observing statements THEN pass the error through`() {
        runBlocking {
            val serverFlow = flowOf<Either<Failure, List<StatementRemote>>>(
                Either.left(Failure.UnknownError)
            )
            whenever(remoteDataStore.observeStatements(userEmail, retroUuid))
                .thenReturn(serverFlow)

            val resultFlow = repository.startObservingStatements(retroUuid = retroUuid)

            resultFlow.test {
                assertEquals(Either.left(Failure.UnknownError), expectItem())
                verifyZeroInteractions(localDataStore)
                expectComplete()
            }
        }
    }
    //endregion

    private fun getMockStatementList(): List<StatementRemote> {
        return listOf(
            StatementRemote(
                uuid = "statementUuid",
                retroUuid = retroUuid,
                userEmail = userEmail,
                description = "This is the description of the mock statement",
                statementType = StatementType.POSITIVE.toString().toLowerCase(),
                timestamp = 100012038L,
                isRemovable = false
            )
        )
    }
}