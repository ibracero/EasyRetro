package com.ibracero.retrum.di

import android.content.Context
import android.net.ConnectivityManager
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.common.RetrumConnectionManager
import com.ibracero.retrum.data.AccountRepositoryImpl
import com.ibracero.retrum.data.BoardRepositoryImpl
import com.ibracero.retrum.data.RetroRepositoryImpl
import com.ibracero.retrum.data.local.LocalDataStore
import com.ibracero.retrum.data.local.RetrumDatabase
import com.ibracero.retrum.data.mapper.RetroRemoteToDomainMapper
import com.ibracero.retrum.data.mapper.StatementRemoteToDomainMapper
import com.ibracero.retrum.data.mapper.UserRemoteToDomainMapper
import com.ibracero.retrum.data.remote.RemoteDataStore
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.BoardRepository
import com.ibracero.retrum.domain.RetroRepository
import com.ibracero.retrum.ui.account.login.LoginPresenter
import com.ibracero.retrum.ui.board.StatementViewModel
import com.ibracero.retrum.ui.account.welcome.WelcomePresenter
import com.ibracero.retrum.ui.board.BoardViewModel
import com.ibracero.retrum.ui.retros.RetroListViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    factory<BoardRepository> {
        BoardRepositoryImpl(
            localDataStore = get(),
            remoteDataStore = get(),
            dispatchers = get(),
            statementRemoteToDomainMapper = get(),
            userRemoteToDomainMapper = get()
        )
    }

    factory<RetroRepository> {
        RetroRepositoryImpl(
            localDataStore = get(),
            remoteDataStore = get(),
            retroRemoteToDomainMapper = get(),
            dispatchers = get()
        )
    }

    single { RemoteDataStore() }

    single { RetrumConnectionManager(androidApplication().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager) }

    single { LocalDataStore(get()) }

    single { RetrumDatabase.getDatabase(androidApplication()).retroDao() }

    single { CoroutineDispatcherProvider() }
}

val viewModelModule = module {

    viewModel { RetroListViewModel(get(), get()) }

    viewModel { StatementViewModel(get()) }

    viewModel { BoardViewModel(get()) }
}

val mapperModule = module {
    factory { RetroRemoteToDomainMapper() }

    factory { StatementRemoteToDomainMapper() }

    factory { UserRemoteToDomainMapper() }
}

val accountModule = module {

    factory<AccountRepository> { AccountRepositoryImpl() }

    factory { WelcomePresenter(get()) }

    factory { LoginPresenter(get()) }
}