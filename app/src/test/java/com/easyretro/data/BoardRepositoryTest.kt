package com.easyretro.data

import arrow.core.Either
import com.easyretro.CoroutineTestRule
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.RetroDb
import com.easyretro.data.local.StatementDb
import com.easyretro.data.local.UserDb
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

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val userEmail = "email@email.com"
    private val retroUuid = "retro-uuid"

    private val localDataStore = mock<LocalDataStore>()
    private val remoteDataStore = mock<RemoteDataStore>()
    private val authDataStore = mock<AuthDataStore> {
        on { getCurrentUserEmail() }.thenReturn(userEmail)
    }

    private val statementRemoteToDbMapper = StatementRemoteToDbMapper()
    private val statementDbToDomainMapper = StatementDbToDomainMapper()

    private val repository = BoardRepositoryImpl(
        localDataStore = localDataStore,
        remoteDataStore = remoteDataStore,
        authDataStore = authDataStore,
        statementRemoteToDbMapper = statementRemoteToDbMapper,
        statementDbToDomainMapper = statementDbToDomainMapper,
        dispatchers = coroutinesTestRule.testDispatcherProvider
    )

    //region get statements
    @Test
    fun `GIVEN protected retro WHEN getting statement list THEN return statements not removable`() {
        runBlocking {
            whenever(localDataStore.observeRetro(retroUuid = retroUuid))
                .thenReturn(flowOf(getMockLocalRetro().copy(isProtected = true)))
            whenever(localDataStore.observePositiveStatements(retroUuid = retroUuid))
                .thenReturn(flowOf(getMockLocalStatementList()))

            val resultFlow = repository.getStatements(retroUuid = retroUuid, statementType = StatementType.POSITIVE)

            resultFlow.test {
                val expected = getMockLocalStatementList()
                    .map { it.copy(removable = false) }
                    .map(statementDbToDomainMapper::map)
                assertEquals(expected, expectItem())
                expectComplete()
            }
        }
    }

    @Test
    fun `GIVEN non-protected retro WHEN getting statement list THEN return statements original removable value`() {
        runBlocking {
            whenever(localDataStore.observeRetro(retroUuid = retroUuid))
                .thenReturn(flowOf(getMockLocalRetro().copy(isProtected = false)))
            whenever(localDataStore.observePositiveStatements(retroUuid = retroUuid))
                .thenReturn(flowOf(getMockLocalStatementList()))

            val resultFlow = repository.getStatements(retroUuid = retroUuid, statementType = StatementType.POSITIVE)

            resultFlow.test {
                val expected = getMockLocalStatementList()
                    .map(statementDbToDomainMapper::map)
                assertEquals(expected, expectItem())
                expectComplete()
            }
        }
    }

    @Test
    fun `GIVEN positive type WHEN getting statement list THEN requests positive statements`() {
        runBlocking {
            whenever(localDataStore.observeRetro(retroUuid = retroUuid))
                .thenReturn(flowOf(getMockLocalRetro()))
            whenever(localDataStore.observePositiveStatements(retroUuid = retroUuid))
                .thenReturn(flowOf(emptyList()))

            repository.getStatements(retroUuid = retroUuid, statementType = StatementType.POSITIVE)

            verify(localDataStore).observePositiveStatements(retroUuid = retroUuid)
        }
    }

    @Test
    fun `GIVEN negative type WHEN getting statement list THEN requests negative statements`() {
        runBlocking {
            whenever(localDataStore.observeRetro(retroUuid = retroUuid))
                .thenReturn(flowOf(getMockLocalRetro()))
            whenever(localDataStore.observePositiveStatements(retroUuid = retroUuid))
                .thenReturn(flowOf(emptyList()))

            repository.getStatements(retroUuid = retroUuid, statementType = StatementType.NEGATIVE)

            verify(localDataStore).observeNegativeStatements(retroUuid = retroUuid)
        }
    }

    @Test
    fun `GIVEN actions type WHEN getting statement list THEN requests actions statements`() {
        runBlocking {
            whenever(localDataStore.observeRetro(retroUuid = retroUuid))
                .thenReturn(flowOf(getMockLocalRetro()))
            whenever(localDataStore.observePositiveStatements(retroUuid = retroUuid))
                .thenReturn(flowOf(emptyList()))

            repository.getStatements(retroUuid = retroUuid, statementType = StatementType.ACTION_POINT)

            verify(localDataStore).observeActionPoints(retroUuid = retroUuid)
        }
    }
    //endregion

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
                Either.right(getMockRemoteStatementList())
            )
            whenever(remoteDataStore.observeStatements(userEmail, retroUuid))
                .thenReturn(serverFlow)

            val resultFlow = repository.startObservingStatements(retroUuid = retroUuid)

            resultFlow.test {
                assertEquals(Either.right(Unit), expectItem())
                assertEquals(Either.right(Unit), expectItem())
                verify(localDataStore).saveStatements(getMockRemoteStatementList().map(statementRemoteToDbMapper::map))
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

    private fun getMockRemoteStatementList(): List<StatementRemote> {
        return listOf(
            StatementRemote(
                uuid = "statementUuid",
                retroUuid = retroUuid,
                userEmail = userEmail,
                description = "This is the description of the mock statement",
                statementType = StatementType.POSITIVE.toString().toLowerCase(),
                timestamp = 100012038L,
                isRemovable = true
            )
        )
    }

    private fun getMockLocalStatementList(): List<StatementDb> {
        return listOf(
            StatementDb(
                uuid = "statementUuid",
                retroUuid = retroUuid,
                userEmail = userEmail,
                description = "This is the description of the mock statement",
                type = StatementType.POSITIVE,
                timestamp = 100012038L,
                removable = true
            ),
            StatementDb(
                uuid = "statementUuid2",
                retroUuid = retroUuid,
                userEmail = userEmail,
                description = "This is the description of the mock statement (second)",
                type = StatementType.POSITIVE,
                timestamp = 100012038L,
                removable = false
            )
        )
    }

    private fun getMockLocalRetro(): RetroDb {
        val retroUuid = "retro-uuid"
        val retroTitle = "Retro title"
        val retroDeepLink = "http://www.easyretro.com/join"
        val userFirstName = "First name"
        val userLastName = "Last name"
        val userPhotoUrl = "photo.com/user1"
        val retroTimestamp = 1586705438L

        val dbUserOne = UserDb(
            email = userEmail,
            firstName = userFirstName,
            lastName = userLastName,
            photoUrl = userPhotoUrl
        )

        return RetroDb(
            uuid = retroUuid,
            title = retroTitle,
            timestamp = retroTimestamp,
            deepLink = retroDeepLink,
            users = listOf(dbUserOne),
            ownerEmail = userEmail,
            isProtected = true
        )
    }
}