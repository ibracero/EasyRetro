package com.easyretro.data

import arrow.core.Either
import com.easyretro.CoroutineTestRule
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.Retro
import com.easyretro.data.mapper.RetroRemoteToDomainMapper
import com.easyretro.data.mapper.UserRemoteToDomainMapper
import com.easyretro.data.remote.AuthDataStore
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.data.remote.firestore.RetroRemote
import com.easyretro.domain.Failure
import com.easyretro.domain.domainRetro
import com.easyretro.test
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RetroRepositoryImplTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val localDataStore = mock<LocalDataStore>()
    private val remoteDataStore = mock<RemoteDataStore>()
    private val retroMapper = RetroRemoteToDomainMapper(userRemoteToDomainMapper = UserRemoteToDomainMapper())
    private val authDataStore = mock<AuthDataStore> {
        on { getCurrentUserEmail() }.thenReturn(userEmail)
    }

    private val repository: RetroRepositoryImpl = RetroRepositoryImpl(
        localDataStore = localDataStore,
        remoteDataStore = remoteDataStore,
        authDataStore = authDataStore,
        retroRemoteToDomainMapper = retroMapper,
        dispatchers = coroutinesTestRule.testDispatcherProvider
    )

    @Test
    fun `GIVEN a success response from Firebase WHEN trying to create a retro THEN return Right with the retro`() {
        runBlocking {
            val mockRetroTitle = "Retro title"
            whenever(remoteDataStore.createRetro(userEmail = userEmail, retroTitle = mockRetroTitle))
                .thenReturn(Either.right(remoteRetro))

            val actualValue = repository.createRetro(mockRetroTitle)
            val expectedValue = Either.right(mapRemoteToDomain(remoteRetro))

            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN a failed response from Firebase WHEN trying to create a retro THEN return Left with the failure`() {
        runBlocking {
            val mockRetroTitle = "Retro title"
            whenever(remoteDataStore.createRetro(userEmail = userEmail, retroTitle = mockRetroTitle))
                .thenReturn(Either.left(Failure.UnknownError))

            val actualValue = repository.createRetro(mockRetroTitle)
            val expectedValue = Either.left(Failure.UnknownError)

            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN a success response from Firebase WHEN trying to join a retro THEN return Right Unit`() {
        runBlocking {
            val mockRetroUuid = "retro-uuid"
            whenever(remoteDataStore.joinRetro(userEmail = userEmail, retroUuid = mockRetroUuid))
                .thenReturn(Either.right(Unit))

            val actualValue = repository.joinRetro(mockRetroUuid)
            val expectedValue = Either.right(Unit)

            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN a failed response from Firebase WHEN trying to join a retro THEN return Left with the failure`() {
        runBlocking {
            val mockRetroUuid = "retro-uuid"
            whenever(remoteDataStore.joinRetro(userEmail = userEmail, retroUuid = mockRetroUuid))
                .thenReturn(Either.left(Failure.UnknownError))

            val actualValue = repository.joinRetro(mockRetroUuid)
            val expectedValue = Either.left(Failure.UnknownError)

            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `GIVEN a valid retroUuid WHEN getting retro info THEN return Right with the retro`() {
        runBlocking {
            whenever(localDataStore.getRetro("valid-uuid")).thenReturn(domainRetro)

            val actualValue = repository.getRetro("valid-uuid")
            val expectedValue = Either.right(domainRetro)

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
            val localRetros = listOf(domainRetro)
            val remoteRetros = listOf(remoteRetro)
            whenever(localDataStore.getRetros()).thenReturn(localRetros)
            whenever(remoteDataStore.getUserRetros(userEmail)).thenReturn(Either.right(remoteRetros))

            val flow = repository.getRetros()

            flow.test {
                assertEquals(Either.Right(localRetros), expectItem())
                assertEquals(Either.Right(remoteRetros.map(::mapRemoteToDomain)), expectItem())
                expectComplete()
            }
        }
    }

    @Test
    fun `GIVEN no local data WHEN getting retro list THEN flow emits just remote data`() {
        runBlocking {
            val localRetros = emptyList<Retro>()
            val remoteRetros = listOf(remoteRetro)
            whenever(localDataStore.getRetros()).thenReturn(localRetros)
            whenever(remoteDataStore.getUserRetros(userEmail)).thenReturn(Either.right(remoteRetros))

            val flow = repository.getRetros()

            flow.test {
                assertEquals(Either.Right(remoteRetros.map(::mapRemoteToDomain)), expectItem())
                expectComplete()
            }
        }
    }


    @Test
    fun `GIVEN a remote error WHEN getting retro list THEN flow emits local data and the error`() {
        runBlocking {
            val localRetros = listOf(domainRetro)
            whenever(localDataStore.getRetros()).thenReturn(localRetros)
            whenever(remoteDataStore.getUserRetros(userEmail)).thenReturn(Either.left(Failure.UnknownError))

            val flow = repository.getRetros()

            flow.test {
                assertEquals(Either.Right(localRetros), expectItem())
                assertEquals(Either.Left(Failure.UnknownError), expectItem())
                expectComplete()
            }
        }
    }

    private fun mapRemoteToDomain(retro: RetroRemote) = retroMapper.map(retro)

}