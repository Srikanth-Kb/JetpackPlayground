package com.playground.jetpackplayground.di.main

import com.playground.jetpackplayground.ui.main.account.AccountFragment
import com.playground.jetpackplayground.ui.main.account.ChangePasswordFragment
import com.playground.jetpackplayground.ui.main.account.UpdateAccountFragment
import com.playground.jetpackplayground.ui.main.blog.BlogFragment
import com.playground.jetpackplayground.ui.main.blog.UpdateBlogFragment
import com.playground.jetpackplayground.ui.main.blog.ViewBlogFragment
import com.playground.jetpackplayground.ui.main.create_blog.CreateBlogFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeBlogFragment(): BlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeAccountFragment(): AccountFragment

    @ContributesAndroidInjector()
    abstract fun contributeChangePasswordFragment(): ChangePasswordFragment

    @ContributesAndroidInjector()
    abstract fun contributeCreateBlogFragment(): CreateBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateBlogFragment(): UpdateBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeViewBlogFragment(): ViewBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateAccountFragment(): UpdateAccountFragment
}