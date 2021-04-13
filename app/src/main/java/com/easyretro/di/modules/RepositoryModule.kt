package com.easyretro.di.modules

import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.common.UuidProvider
import com.easyretro.data.AccountRepositoryImpl
import com.easyretro.data.BoardRepositoryImpl
import com.easyretro.data.RetroRepositoryImpl
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.SessionManager
import com.easyretro.data.local.mapper.RetroDbToDomainMapper
import com.easyretro.data.local.mapper.StatementDbToDomainMapper
import com.easyretro.data.remote.AuthDataStore
import com.easyretro.data.remote.DeepLinkDataStore
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.data.remote.mapper.RetroRemoteToDbMapper
import com.easyretro.data.remote.mapper.StatementRemoteToDbMapper
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.RetroRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

@Module
@InstallIn(ApplicationComponent::class)
object RepositoryModule {

    @Provides
    fun provideAccountRepository(
        localDataStore: LocalDataStore,
        remoteDataStore: RemoteDataStore,
        authDataStore: AuthDataStore,
        dispatchers: CoroutineDispatcherProvider,
        sessionManager: SessionManager
    ): AccountRepository = AccountRepositoryImpl(
        localDataStore = localDataStore,
        remoteDataStore = remoteDataStore,
        authDataStore = authDataStore,
        dispatchers = dispatchers,
        sessionManager = sessionManager
    )

    @Provides
    fun provideRetroRepository(
        localDataStore: LocalDataStore,
        remoteDataStore: RemoteDataStore,
        authDataStore: AuthDataStore,
        deepLinkDataStore: DeepLinkDataStore,
        uuidProvider: UuidProvider,
        retroRemoteToDbMapper: RetroRemoteToDbMapper,
        retroDbToDomainMapper: RetroDbToDomainMapper,
        dispatchers: CoroutineDispatcherProvider
    ): RetroRepository = RetroRepositoryImpl(
        localDataStore = localDataStore,
        remoteDataStore = remoteDataStore,
        authDataStore = authDataStore,
        deepLinkDataStore = deepLinkDataStore,
        uuidProvider = uuidProvider,
        retroRemoteToDbMapper = retroRemoteToDbMapper,
        retroDbToDomainMapper = retroDbToDomainMapper,
        dispatchers = dispatchers
    )

    @Provides
    fun provideBoardRepository(
        localDataStore: LocalDataStore,
        remoteDataStore: RemoteDataStore,
        authDataStore: AuthDataStore,
        statementRemoteToDbMapper: StatementRemoteToDbMapper,
        statementDbToDomainMapper: StatementDbToDomainMapper,
        dispatchers: CoroutineDispatcherProvider
    ): BoardRepository = BoardRepositoryImpl(
        localDataStore = localDataStore,
        remoteDataStore = remoteDataStore,
        authDataStore = authDataStore,
        statementRemoteToDbMapper = statementRemoteToDbMapper,
        statementDbToDomainMapper = statementDbToDomainMapper,
        dispatchers = dispatchers
    )
}