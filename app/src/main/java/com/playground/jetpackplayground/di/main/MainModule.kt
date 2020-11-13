package com.playground.jetpackplayground.di.main

import com.playground.jetpackplayground.api.main.OpenApiMainService
import com.playground.jetpackplayground.persistence.AccountPropertiesDao
import com.playground.jetpackplayground.persistence.AppDatabase
import com.playground.jetpackplayground.persistence.BlogPostDao
import com.playground.jetpackplayground.repository.main.AccountRepository
import com.playground.jetpackplayground.repository.main.BlogRepository
import com.playground.jetpackplayground.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideOpenApiMainService(retrofitBuilder: Retrofit.Builder): OpenApiMainService {
        return retrofitBuilder
            .build()
            .create(OpenApiMainService::class.java)
    }

    @MainScope
    @Provides
    fun provideAccountRepository(
        openApiMainService: OpenApiMainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ) : AccountRepository {
        return AccountRepository(
            openApiMainService,
            accountPropertiesDao,
            sessionManager
        )
    }

    @MainScope
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    @MainScope
    @Provides
    fun provideBlogRepository(
        openApiMainService: OpenApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepository {
        return BlogRepository(openApiMainService, blogPostDao, sessionManager)
    }
}