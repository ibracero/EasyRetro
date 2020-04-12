package com.easyretro.data

import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.mapper.RetroRemoteToDomainMapper
import com.easyretro.data.remote.RemoteDataStore
import com.nhaarman.mockitokotlin2.mock

class RetroRepositoryImplTest {

    private val localDataStore = mock<LocalDataStore>()
    private val remoteDataStore = mock<RemoteDataStore>()
    private val retroMapper = mock<RetroRemoteToDomainMapper>()
    private val coroutineDispatcherProvider = CoroutineDispatcherProvider()

    private val repository: RetroRepositoryImpl = RetroRepositoryImpl(
        localDataStore = localDataStore,
        remoteDataStore = remoteDataStore,
        retroRemoteToDomainMapper = retroMapper,
        dispatchers = coroutineDispatcherProvider
    )

}