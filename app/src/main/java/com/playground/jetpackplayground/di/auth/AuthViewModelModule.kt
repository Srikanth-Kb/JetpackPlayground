package com.playground.jetpackplayground.di.auth

import androidx.lifecycle.ViewModel
import com.playground.jetpackplayground.di.ViewModelKey
import com.playground.jetpackplayground.ui.auth.AuthViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

}