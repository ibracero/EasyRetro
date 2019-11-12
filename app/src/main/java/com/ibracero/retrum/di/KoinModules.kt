package com.ibracero.retrum.di

import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.data.RepositoryImpl
import com.ibracero.retrum.data.local.LocalDataStore
import com.ibracero.retrum.data.local.RetrumDatabase
import com.ibracero.retrum.data.mapper.RetroRemoteToDomainMapper
import com.ibracero.retrum.data.mapper.StatementRemoteToDomainMapper
import com.ibracero.retrum.data.mapper.UserRemoteToDomainMapper
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.ui.board.positive.PositiveViewModel
import com.ibracero.retrum.ui.retros.RetroListFragment
import com.ibracero.retrum.ui.retros.RetroListViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single<Repository> {
        RepositoryImpl(
            localDataStore = get(),
            firebaseDataStore = get(),
            dispatchers = get(),
            retroRemoteToDomainMapper = get(),
            statementRemoteToDomainMapper = get(),
            userRemoteToDomainMapper = get()
        )
    }

    single { FirebaseDataStore() }

    single { LocalDataStore(get()) }

    single { RetrumDatabase.getDatabase(androidApplication()).retroDao() }

    single { CoroutineDispatcherProvider() }
}

val viewModelModule = module {

    viewModel { RetroListViewModel(get()) }

    viewModel { PositiveViewModel(get()) }
}

val mapperModule = module {
    factory { RetroRemoteToDomainMapper() }

    factory { StatementRemoteToDomainMapper() }

    factory { UserRemoteToDomainMapper() }
}