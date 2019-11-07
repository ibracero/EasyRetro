package com.ibracero.retrum.di

import com.ibracero.retrum.data.RepositoryImpl
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.ui.positive.PositiveViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { PositiveViewModel(get()) }
    single<Repository> { RepositoryImpl() }
}