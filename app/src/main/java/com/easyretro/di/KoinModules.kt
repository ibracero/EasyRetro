package com.easyretro.di

import android.content.Context
import android.net.ConnectivityManager
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.common.EasyRetroConnectionManager
import com.easyretro.data.AccountRepositoryImpl
import com.easyretro.data.BoardRepositoryImpl
import com.easyretro.data.RetroRepositoryImpl
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.EasyRetroDatabase
import com.easyretro.data.mapper.RetroRemoteToDomainMapper
import com.easyretro.data.mapper.StatementRemoteToDomainMapper
import com.easyretro.data.mapper.UserRemoteToDomainMapper
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.ui.account.AccountViewModel
import com.easyretro.ui.account.EmailVerificationViewModel
import com.easyretro.ui.account.ResetPasswordViewModel
import com.easyretro.ui.board.BoardViewModel
import com.easyretro.ui.board.StatementViewModel
import com.easyretro.ui.retros.RetroListViewModel
import com.easyretro.ui.welcome.WelcomeViewModel
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

    single { EasyRetroConnectionManager(androidApplication().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager) }

    single { LocalDataStore(get()) }

    single { EasyRetroDatabase.getDatabase(androidApplication()).retroDao() }

    single { CoroutineDispatcherProvider() }
}

val viewModelModule = module {

    viewModel { RetroListViewModel(get(), get()) }

    viewModel { StatementViewModel(get()) }

    viewModel { BoardViewModel(get()) }

    viewModel { AccountViewModel(get()) }

    viewModel { ResetPasswordViewModel(get()) }

    viewModel { WelcomeViewModel(get()) }

    viewModel { EmailVerificationViewModel(get()) }
}

val mapperModule = module {
    factory { RetroRemoteToDomainMapper(get()) }

    factory { StatementRemoteToDomainMapper() }

    factory { UserRemoteToDomainMapper() }
}

val accountModule = module {

    factory<AccountRepository> { AccountRepositoryImpl(get(), get(), get()) }

}