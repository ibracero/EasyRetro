package com.easyretro.di

import android.content.Context
import android.net.ConnectivityManager
import com.easyretro.common.ConnectionManager
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.common.DefaultCoroutineDispatcher
import com.easyretro.data.AccountRepositoryImpl
import com.easyretro.data.BoardRepositoryImpl
import com.easyretro.data.RetroRepositoryImpl
import com.easyretro.data.local.EasyRetroDatabase
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.SessionSharedPrefsManager
import com.easyretro.data.local.mapper.RetroDbToDomainMapper
import com.easyretro.data.local.mapper.StatementDbToDomainMapper
import com.easyretro.data.local.mapper.UserDbToDomainMapper
import com.easyretro.data.remote.AuthDataStore
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.data.remote.mapper.RetroRemoteToDbMapper
import com.easyretro.data.remote.mapper.StatementRemoteToDbMapper
import com.easyretro.data.remote.mapper.UserRemoteToDbMapper
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
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    factory<BoardRepository> {
        BoardRepositoryImpl(
            localDataStore = get(),
            remoteDataStore = get(),
            authDataStore = get(),
            dispatchers = get(),
            statementDbToDomainMapper = get(),
            statementRemoteToDbMapper = get()
        )
    }

    factory<RetroRepository> {
        RetroRepositoryImpl(
            localDataStore = get(),
            remoteDataStore = get(),
            authDataStore = get(),
            retroDbToDomainMapper = get(),
            retroRemoteToDbMapper = get(),
            dispatchers = get()
        )
    }

    single { RemoteDataStore(get()) }

    single { AuthDataStore(get()) }

    single { ConnectionManager(androidApplication().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager) }

    single { LocalDataStore(get()) }

    single { EasyRetroDatabase.getDatabase(androidApplication()).retroDao() }

    single<CoroutineDispatcherProvider> { DefaultCoroutineDispatcher() }

}

val viewModelModule = module {

    viewModel { RetroListViewModel(get(), get()) }

    viewModel { StatementViewModel(get(), get()) }

    viewModel { BoardViewModel(get(), get()) }

    viewModel { AccountViewModel(get()) }

    viewModel { ResetPasswordViewModel(get()) }

    viewModel { WelcomeViewModel(get()) }

    viewModel { EmailVerificationViewModel(get()) }
}

val mapperModule = module {
    factory { RetroRemoteToDbMapper(get()) }

    factory { RetroDbToDomainMapper(get()) }

    factory { StatementRemoteToDbMapper() }

    factory { StatementDbToDomainMapper() }

    factory { UserRemoteToDbMapper() }

    factory { UserDbToDomainMapper() }
}

val accountModule = module {

    factory<AccountRepository> { AccountRepositoryImpl(get(), get(), get(), get(), get()) }

    factory { SessionSharedPrefsManager(androidContext()) }
}