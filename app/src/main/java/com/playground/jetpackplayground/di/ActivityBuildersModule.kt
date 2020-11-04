package com.playground.jetpackplayground.di

import com.playground.jetpackplayground.ui.main.MainActivity
import com.playground.jetpackplayground.di.auth.AuthFragmentBuildersModule
import com.playground.jetpackplayground.di.auth.AuthModule
import com.playground.jetpackplayground.di.auth.AuthScope
import com.playground.jetpackplayground.di.auth.AuthViewModelModule
import com.playground.jetpackplayground.di.main.MainFragmentBuildersModule
import com.playground.jetpackplayground.di.main.MainModule
import com.playground.jetpackplayground.di.main.MainScope
import com.playground.jetpackplayground.di.main.MainViewModelModule
import com.playground.jetpackplayground.ui.auth.AuthActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthModule::class, AuthFragmentBuildersModule::class, AuthViewModelModule::class]
    )
    abstract fun contributeAuthActivity(): AuthActivity

    @MainScope
    @ContributesAndroidInjector(
        modules = [MainModule::class, MainFragmentBuildersModule::class, MainViewModelModule::class]
    )
    abstract fun contributeMainActivity(): MainActivity

}