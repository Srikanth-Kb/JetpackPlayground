package com.playground.jetpackplayground.di.main

import androidx.lifecycle.ViewModel
import com.playground.jetpackplayground.di.ViewModelKey
import com.playground.jetpackplayground.ui.auth.AuthViewModel
import com.playground.jetpackplayground.ui.main.account.AccountViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accountViewModel: AccountViewModel): ViewModel

}