package com.easyretro.data

import arrow.core.Either
import com.easyretro.CoroutineTestRule
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.RetroDb
import com.easyretro.data.local.mapper.RetroDbToDomainMapper
import com.easyretro.data.local.mapper.UserDbToDomainMapper
import com.easyretro.data.remote.AuthDataStore
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.data.remote.firestore.RetroRemote
import com.easyretro.data.remote.firestore.UserRemote
import com.easyretro.data.remote.mapper.RetroRemoteToDbMapper
import com.easyretro.data.remote.mapper.UserRemoteToDbMapper
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.Retro
import com.easyretro.domain.model.User
import com.easyretro.test
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RetroRepositoryImplTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val retroUuid = "retro-uuid"
    private val retroTitle = "Retro title"
    private val userEmail = "email@email.com"
    private lateinit var domainRetro: Retro
    private lateinit var dbRetro: RetroDb
    private lateinit var remoteRetro: RetroRemote

    private val localDataStore = mock<LocalDataStore>()
    private val remoteDataStore = mock<RemoteDataStore>()
    private val retroRemoteToDbMapper = RetroRemoteToDbMapper(userRemoteToDbMapper = UserRemoteToDbMapper())
    private val retroDbToDomainMapper = RetroDbToDomainMapper(userDbToDomainMapper = UserDbToDomainMapper())
    private val authDataStore = mock<AuthDataStore> {
        on { getCurrentUserEmail() }.thenReturn(userEmail)
    }

    private val repository: RetroRepositoryImpl = RetroRepositoryImpl(
        localDataStore = localDataStore,
        remoteDataStore = remoteDataStore,
        authDataStore = authDataStore,
        retroRemoteToDbMapper = retroRemoteToDbMapper,
        retroDbToDomainMapper = retroDbToDomainMapper,
        dispatchers = coroutinesTestRule.testDispatcherProvider
    )

    init {
        initModelMocks()
    }

    @Test
    fun `GIVEN a success response from Firebase WHEN trying to create a retro THEN return Right with the retro`() {
        runBlocking {
            whenever(remoteDataStore.createRetro(userEmail = userEmail, retroTitle = retroTitle))
                .thenReturn(Either.right(remoteRetro))

            val actualValue = repository.createRetro(retroTitle)
            val expectedValue = Either.right(domainRetro)

            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN a failed response from Firebase WHEN trying to create a retro THEN return Left with the failure`() {
        runBlocking {
            whenever(remoteDataStore.createRetro(userEmail = userEmail, retroTitle = retroTitle))
                .thenReturn(Either.left(Failure.UnknownError))

            val actualValue = repository.createRetro(retroTitle)
            val expectedValue = Either.left(Failure.UnknownError)

            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN a success response from Firebase WHEN trying to join a retro THEN return Right Unit`() {
        runBlocking {
            whenever(remoteDataStore.joinRetro(userEmail = userEmail, retroUuid = retroUuid))
                .thenReturn(Either.right(Unit))

            val actualValue = repository.joinRetro(retroUuid)
            val expectedValue = Either.right(Unit)

            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN a failed response from Firebase WHEN trying to join a retro THEN return Left with the failure`() {
        runBlocking {
            whenever(remoteDataStore.joinRetro(userEmail = userEmail, retroUuid = retroUuid))
                .thenReturn(Either.left(Failure.UnknownError))

            val actualValue = repository.joinRetro(retroUuid)
            val expectedValue = Either.left(Failure.UnknownError)

            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN a valid retroUuid WHEN getting retro info THEN return Right with the retro`() {
        runBlocking {
            whenever(localDataStore.getRetro("valid-uuid")).thenReturn(dbRetro)

            val actualValue = repository.getRetro("valid-uuid")
            val expectedValue = Either.right(retroDbToDomainMapper.map(dbRetro))

            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN an invalid retroUuid WHEN getting retro info THEN return Left with the failure`() {
        runBlocking {
            whenever(localDataStore.getRetro("invalid-uuid")).thenReturn(null)

            val actualValue = repository.getRetro("invalid-uuid")
            val expectedValue = Either.left(Failure.RetroNotFoundError)

            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN local data WHEN getting retro list THEN flow emits first local data and then remote data`() {
        runBlocking {
            val localRetros = listOf(dbRetro)
            val remoteRetros = listOf(remoteRetro)
            whenever(localDataStore.getRetros()).thenReturn(localRetros)
            whenever(remoteDataStore.getUserRetros(userEmail)).thenReturn(Either.right(remoteRetros))

            val flow = repository.getRetros()

            flow.test {
                val remoteToDomainRetros = remoteRetros.map(retroRemoteToDbMapper::map).map(retroDbToDomainMapper::map)
                assertEquals(Either.Right(localRetros.map(retroDbToDomainMapper::map)), expectItem())
                assertEquals(Either.Right(remoteToDomainRetros), expectItem())
                expectComplete()
            }
        }
    }

    @Test
    fun `GIVEN no local data WHEN getting retro list THEN flow emits just remote data`() {
        runBlocking {
            val localRetros = emptyList<RetroDb>()
            val remoteRetros = listOf(remoteRetro)
            whenever(localDataStore.getRetros()).thenReturn(localRetros)
            whenever(remoteDataStore.getUserRetros(userEmail)).thenReturn(Either.right(remoteRetros))

            val flow = repository.getRetros()

            flow.test {
                val remoteToDomainRetros = remoteRetros.map(retroRemoteToDbMapper::map).map(retroDbToDomainMapper::map)
                assertEquals(Either.Right(remoteToDomainRetros), expectItem())
                expectComplete()
            }
        }
    }


    @Test
    fun `GIVEN a remote error WHEN getting retro list THEN flow emits local data and the error`() {
        runBlocking {
            val localRetros = listOf(dbRetro)
            whenever(localDataStore.getRetros()).thenReturn(localRetros)
            whenever(remoteDataStore.getUserRetros(userEmail)).thenReturn(Either.left(Failure.UnknownError))

            val flow = repository.getRetros()

            flow.test {
                assertEquals(Either.Right(localRetros.map(retroDbToDomainMapper::map)), expectItem())
                assertEquals(Either.Left(Failure.UnknownError), expectItem())
                expectComplete()
            }
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
            users = listOf(domainUserOne)
        )

        val remoteUserOne = UserRemote(
            email = userEmail,
            firstName = userFirstName,
            lastName = userLastName,
            photoUrl = userPhotoUrl
        )

        remoteRetro = RetroRemote(
            uuid = retroUuid,
            title = retroTitle,
            timestamp = retroTimestamp,
            users = listOf(remoteUserOne)
        )

        dbRetro = RetroDb(
            uuid = retroUuid,
            title = retroTitle,
            timestamp = 1000L,
            users = emptyList()
        )
    }
}