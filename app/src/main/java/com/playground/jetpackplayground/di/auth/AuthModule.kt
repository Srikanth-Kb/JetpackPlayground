package com.playground.jetpackplayground.di.auth

import android.content.SharedPreferences
import com.playground.jetpackplayground.api.auth.OpenApiAuthService
import com.playground.jetpackplayground.persistence.AccountPropertiesDao
import com.playground.jetpackplayground.persistence.AuthTokenDao
import com.playground.jetpackplayground.repository.auth.AuthRepository
import com.playground.jetpackplayground.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class AuthModule{

    // TEMPORARY
    @AuthScope
    @Provides
    fun provideFakeApiService(retrofitBuilder : Retrofit.Builder): OpenApiAuthService {
        return retrofitBuilder
            .build()
            .create(OpenApiAuthService::class.java)
    }

    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        openApiAuthService: OpenApiAuthService,
        sharedPreferences: SharedPreferences,
        sharedPreferencesEditor: SharedPreferences.Editor
    ): AuthRepository {
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            openApiAuthService,
            sessionManager,
            sharedPreferences,
            sharedPreferencesEditor
        )
    }

}