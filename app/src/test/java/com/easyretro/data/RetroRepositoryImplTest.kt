package com.easyretro.data

import arrow.core.Either
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.common.UuidProvider
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.RetroDb
import com.easyretro.data.local.UserDb
import com.easyretro.data.local.mapper.RetroDbToDomainMapper
import com.easyretro.data.local.mapper.UserDbToDomainMapper
import com.easyretro.data.remote.AuthDataStore
import com.easyretro.data.remote.DeepLinkDataStore
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.data.remote.firestore.RetroRemote
import com.easyretro.data.remote.firestore.UserRemote
import com.easyretro.data.remote.mapper.RetroRemoteToDbMapper
import com.easyretro.data.remote.mapper.UserRemoteToDbMapper
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.Retro
import com.easyretro.domain.model.User
import com.easyretro.test
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class RetroRepositoryImplTest {

    private val testCoroutineScope = TestCoroutineScope()
    private val dispatchers = mock<CoroutineDispatcherProvider> {
        on { main() }.then { testCoroutineScope.coroutineContext }
        on { io() }.then { testCoroutineScope.coroutineContext }
    }

    private val retroUuid = "retro-uuid"
    private val retroTitle = "Retro title"
    private val retroDeepLink = "http://www.easyretro.com/join"
    private val userEmail = "email@email.com"

    private lateinit var domainRetro: Retro
    private lateinit var dbRetro: RetroDb
    private lateinit var remoteRetro: RetroRemote

    private val localDataStore = mock<LocalDataStore>()
    private val remoteDataStore = mock<RemoteDataStore>()
    private val deepLinkDataStore = mock<DeepLinkDataStore>()
    private val retroRemoteToDbMapper = RetroRemoteToDbMapper(userRemoteToDbMapper = UserRemoteToDbMapper())
    private val retroDbToDomainMapper = RetroDbToDomainMapper(userDbToDomainMapper = UserDbToDomainMapper())
    private val authDataStore = mock<AuthDataStore> {
        on { getCurrentUserEmail() }.thenReturn(userEmail)
    }
    private val uuidProvider = mock<UuidProvider> {
        on { generateUuid() }.thenReturn(retroUuid)
    }

    private val repository: RetroRepository = RetroRepositoryImpl(
        localDataStore = localDataStore,
        remoteDataStore = remoteDataStore,
        authDataStore = authDataStore,
        deepLinkDataStore = deepLinkDataStore,
        uuidProvider = uuidProvider,
        retroRemoteToDbMapper = retroRemoteToDbMapper,
        retroDbToDomainMapper = retroDbToDomainMapper,
        dispatchers = dispatchers
    )

    init {
        initModelMocks()
    }

    @Before
    fun `Set up`() {
        testCoroutineScope.launch {
            whenever(deepLinkDataStore.generateDeepLink(any()))
                .thenReturn(Either.right(retroDeepLink))
        }
    }

    //region create retro
    @Test
    fun `GIVEN a success response from Firebase WHEN trying to create a retro THEN return Right with the retro`() {
        testCoroutineScope.launch {
            whenever(
                remoteDataStore.createRetro(
                    retroUuid = retroUuid,
                    userEmail = userEmail,
                    retroTitle = retroTitle,
                    retroDeepLink = retroDeepLink
                )
            ).thenReturn(Either.right(remoteRetro))

            val actualValue = repository.createRetro(retroTitle)

            val expectedValue = Either.right(domainRetro)
            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN a failed response from Firebase WHEN trying to create a retro THEN return Left with the failure`() {
        testCoroutineScope.launch {
            whenever(
                remoteDataStore.createRetro(
                    retroUuid = retroUuid,
                    userEmail = userEmail,
                    retroTitle = retroTitle,
                    retroDeepLink = retroDeepLink
                )
            ).thenReturn(Either.left(Failure.UnknownError))

            val actualValue = repository.createRetro(retroTitle)

            val expectedValue = Either.left(Failure.UnknownError)
            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN a failed response from Firebase WHEN trying to create a deeplink for the new retro THEN return Left with the failure`() {
        testCoroutineScope.launch {
            val expectedValue = Either.left(Failure.UnknownError)
            whenever(deepLinkDataStore.generateDeepLink(any()))
                .thenReturn(expectedValue)

            val actualValue = repository.createRetro(retroTitle)

            verifyZeroInteractions(remoteDataStore)
            assertEquals(expectedValue, actualValue)
        }
    }
    //endregion

    //region join retro
    @Test
    fun `GIVEN a success response from Firebase WHEN trying to join a retro THEN return Right Unit`() {
        testCoroutineScope.launch {
            whenever(remoteDataStore.joinRetro(userEmail = userEmail, retroUuid = retroUuid))
                .thenReturn(Either.right(Unit))

            val actualValue = repository.joinRetro(retroUuid)

            val expectedValue = Either.right(Unit)
            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN a failed response from Firebase WHEN trying to join a retro THEN return Left with the failure`() {
        testCoroutineScope.launch {
            whenever(remoteDataStore.joinRetro(userEmail = userEmail, retroUuid = retroUuid))
                .thenReturn(Either.left(Failure.UnknownError))

            val actualValue = repository.joinRetro(retroUuid)

            val expectedValue = Either.left(Failure.UnknownError)
            assertEquals(expectedValue, actualValue)
        }
    }
    //endregion

    //region observe local retro
    @Test
    fun `GIVEN a valid retroUuid WHEN getting retro info THEN return Right with the retro`() {
        testCoroutineScope.launch {
            whenever(localDataStore.observeRetro("valid-uuid"))
                .thenReturn(flowOf(dbRetro))

            val flow = repository.observeRetro("valid-uuid")

            flow.test {
                val expectedValue = Either.right(retroDbToDomainMapper.map(dbRetro, userEmail))
                assertEquals(expectedValue, expectItem())
                expectComplete()
            }
        }
    }

    @Test
    fun `GIVEN an invalid retroUuid WHEN getting retro info THEN return Left with the failure`() {
        testCoroutineScope.launch {
            whenever(localDataStore.observeRetro("invalid-uuid"))
                .thenReturn(flowOf(null))

            val flow = repository.observeRetro("invalid-uuid")

            flow.test {
                val expectedValue = Either.left(Failure.RetroNotFoundError)
                assertEquals(expectedValue, expectItem())
                expectComplete()
            }
        }
    }
    //endregion

    //region get retros
    @Test
    fun `GIVEN local data WHEN getting retro list THEN flow emits first local data and then remote data`() {
        testCoroutineScope.launch {
            val localRetros = listOf(dbRetro)
            val remoteRetros = listOf(remoteRetro)
            whenever(localDataStore.getRetros()).thenReturn(localRetros)
            whenever(remoteDataStore.getUserRetros(userEmail)).thenReturn(Either.right(remoteRetros))

            val flow = repository.getRetros()

            flow.test {
                val remoteToDomainRetros =
                    remoteRetros.map(retroRemoteToDbMapper::map).map(retroDbToDomainMapper::map)
                assertEquals(
                    Either.Right(localRetros.map(retroDbToDomainMapper::map)),
                    expectItem()
                )
                assertEquals(Either.Right(remoteToDomainRetros), expectItem())
                expectComplete()
            }
        }
    }

    @Test
    fun `GIVEN no local data WHEN getting retro list THEN flow emits just remote data`() {
        testCoroutineScope.launch {
            val localRetros = emptyList<RetroDb>()
            val remoteRetros = listOf(remoteRetro)
            whenever(localDataStore.getRetros()).thenReturn(localRetros)
            whenever(remoteDataStore.getUserRetros(userEmail)).thenReturn(Either.right(remoteRetros))

            val flow = repository.getRetros()

            flow.test {
                val remoteToDomainRetros =
                    remoteRetros.map(retroRemoteToDbMapper::map).map(retroDbToDomainMapper::map)
                assertEquals(Either.Right(remoteToDomainRetros), expectItem())
                expectComplete()
            }
        }
    }

    @Test
    fun `GIVEN a remote error WHEN getting retro list THEN flow emits local data and the error`() {
        testCoroutineScope.launch {
            val localRetros = listOf(dbRetro)
            whenever(localDataStore.getRetros()).thenReturn(localRetros)
            whenever(remoteDataStore.getUserRetros(userEmail)).thenReturn(Either.left(Failure.UnknownError))

            val flow = repository.getRetros()

            flow.test {
                assertEquals(
                    Either.Right(localRetros.map(retroDbToDomainMapper::map)),
                    expectItem()
                )
                assertEquals(Either.Left(Failure.UnknownError), expectItem())
                expectComplete()
            }
        }
    }
    //endregion

    //region protect retro
    @Test
    fun `GIVEN a success response WHEN protecting a retro THEN return Unit`() {
        testCoroutineScope.launch {
            whenever(remoteDataStore.updateRetroProtection(retroUuid = retroUuid, protected = true))
                .thenReturn(Either.right(Unit))

            val actualValue = repository.protectRetro(retroUuid)

            val expectedValue = Either.right(Unit)
            verify(remoteDataStore).updateRetroProtection(retroUuid = retroUuid, protected = true)
            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN a failed response WHEN protecting a retro THEN return Failure`() {
        testCoroutineScope.launch {
            whenever(remoteDataStore.updateRetroProtection(retroUuid = retroUuid, protected = true))
                .thenReturn(Either.left(Failure.UnknownError))

            val actualValue = repository.protectRetro(retroUuid)

            val expectedValue = Either.left(Failure.UnknownError)
            verify(remoteDataStore).updateRetroProtection(retroUuid = retroUuid, protected = true)
            assertEquals(expectedValue, actualValue)
        }
    }
    //endregion

    //region unprotect retro
    @Test
    fun `GIVEN a success response WHEN unprotecting a retro THEN return Unit`() {
        testCoroutineScope.launch {
            whenever(
                remoteDataStore.updateRetroProtection(
                    retroUuid = retroUuid,
                    protected = false
                )
            )
                .thenReturn(Either.right(Unit))

            val actualValue = repository.unprotectRetro(retroUuid)

            val expectedValue = Either.right(Unit)
            verify(remoteDataStore).updateRetroProtection(retroUuid = retroUuid, protected = false)
            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN a failed response WHEN unprotecting a retro THEN return Failure`() {
        testCoroutineScope.launch {
            whenever(
                remoteDataStore.updateRetroProtection(
                    retroUuid = retroUuid,
                    protected = false
                )
            ).thenReturn(Either.left(Failure.UnknownError))

            val actualValue = repository.unprotectRetro(retroUuid)

            val expectedValue = Either.left(Failure.UnknownError)
            verify(remoteDataStore).updateRetroProtection(retroUuid = retroUuid, protected = false)
            assertEquals(expectedValue, actualValue)
        }
    }
    //endregion

    //region start observing remote retro
    @Test
    fun `GIVEN a success response WHEN start observing a remote retro THEN return Unit`() {
        testCoroutineScope.launch {
            whenever(remoteDataStore.observeRetro(retroUuid = retroUuid))
                .thenReturn(flowOf(Either.right(remoteRetro)))

            val flow = repository.startObservingRetroDetails(retroUuid)

            flow.test {
                assertEquals(Either.right(Unit), expectItem())
                expectComplete()
                verify(localDataStore).updateRetro(dbRetro)
            }
        }
    }

    @Test
    fun `GIVEN a failed response WHEN start observing a remote retro THEN return Failure`() {
        testCoroutineScope.launch {
            whenever(remoteDataStore.observeRetro(retroUuid = retroUuid))
                .thenReturn(flowOf(Either.left(Failure.UnknownError)))

            val flow = repository.startObservingRetroDetails(retroUuid)

            flow.test {
                assertEquals(Either.left(Failure.UnknownError), expectItem())
                expectComplete()
                verifyZeroInteractions(localDataStore)
            }
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
            deepLink = retroDeepLink,
            users = listOf(remoteUserOne),
            ownerEmail = userEmail,
            protected = true
        )

        val dbUserOne = UserDb(
            email = userEmail,
            firstName = userFirstName,
            lastName = userLastName,
            photoUrl = userPhotoUrl
        )

        dbRetro = RetroDb(
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