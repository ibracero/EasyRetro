package com.ibracero.retrum.di

import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.data.RepositoryImpl
import com.ibracero.retrum.data.local.LocalDataStore
import com.ibracero.retrum.data.local.RetroDao
import com.ibracero.retrum.data.local.RetrumDatabase
import com.ibracero.retrum.data.mapper.RetroMapper
import com.ibracero.retrum.data.mapper.StatementMapper
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.ui.positive.PositiveViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { PositiveViewModel(get()) }

    single<Repository> {
        RepositoryImpl(
            localDataStore = get(),
            firebaseDataStore = get(),
            dispatchers = get(),
            retroMapper = get(),
            statementMapper = get()
        )
    }

    single { FirebaseDataStore() }

    single { LocalDataStore(get()) }

    single { RetrumDatabase.getDatabase(androidApplication()).retroDao() }

    single { CoroutineDispatcherProvider() }

    factory { RetroMapper() }

    factory { StatementMapper() }
}