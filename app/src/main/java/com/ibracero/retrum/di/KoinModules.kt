package com.ibracero.retrum.di

import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.data.RepositoryImpl
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.ui.positive.PositiveViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { PositiveViewModel(get(), get()) }

    single<Repository> { RepositoryImpl(get()) }

    single { FirebaseDataStore() }

    single { CoroutineDispatcherProvider() }
}