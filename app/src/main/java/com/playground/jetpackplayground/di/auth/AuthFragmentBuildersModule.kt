package com.playground.jetpackplayground.di.auth

import com.playground.jetpackplayground.ui.auth.ForgotPasswordFragment
import com.playground.jetpackplayground.ui.auth.LauncherFragment
import com.playground.jetpackplayground.ui.auth.LoginFragment
import com.playground.jetpackplayground.ui.auth.RegisterFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeLauncherFragment(): LauncherFragment

    @ContributesAndroidInjector()
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector()
    abstract fun contributeRegisterFragment(): RegisterFragment

    @ContributesAndroidInjector()
    abstract fun contributeForgotPasswordFragment(): ForgotPasswordFragment

}